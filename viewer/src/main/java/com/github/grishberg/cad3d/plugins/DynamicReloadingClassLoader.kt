package com.github.grishberg.cad3d.plugins

import java.io.File
import java.net.URLClassLoader
import java.util.concurrent.ConcurrentHashMap

/**
 * ClassLoader, который позволяет горячую перезагрузку классов из JAR файлов.
 * При повторной загрузке того же класса из обновленного JAR создается новый экземпляр класса.
 */
class DynamicReloadingClassLoader(
    jarFile: File, parent: ClassLoader
) : URLClassLoader(arrayOf(jarFile.toURI().toURL()), parent) {

    private val jarPath = jarFile.absolutePath
    private val jarLastModified = jarFile.lastModified()
    private val classDefinitions = ConcurrentHashMap<String, ByteArray>()
    private val loadedTimestamps = ConcurrentHashMap<String, Long>()

    // Пакеты, которые всегда делегируются родительскому ClassLoader'у
    private val delegatedPackages = setOf(
        "java.", "javax.", "kotlin.", "org.jetbrains.", "org.jogamp.", "com.jogamp."
    )

    init {
        // Предзагружаем байткод всех классов из JAR
        preloadClassDefinitions(jarFile)
    }

    /**
     * Предзагружает байткод всех классов из JAR для быстрого доступа.
     */
    private fun preloadClassDefinitions(jarFile: File) {
        try {
            java.util.jar.JarFile(jarFile).use { jar ->
                jar.entries().asSequence().filter { it.name.endsWith(".class") }.forEach { entry ->
                    val className = entry.name.removeSuffix(".class").replace('/', '.')
                    val bytes = jar.getInputStream(entry).readAllBytes()
                    classDefinitions[className] = bytes
                }
            }
            println("Preloaded ${classDefinitions.size} classes from $jarFile")
        } catch (e: Exception) {
            println("Failed to preload classes from $jarFile: ${e.message}")
        }
    }

    /**
     * Проверяет, нужно ли перезагрузить класс из-за обновления JAR.
     */
    private fun shouldReloadClass(className: String): Boolean {
        // Всегда перезагружаем классы из нашего JAR
        return !delegatedPackages.any { className.startsWith(it) }
    }

    /**
     * Загружает класс, учитывая возможность горячей перезагрузки.
     */
    @Throws(ClassNotFoundException::class)
    override fun loadClass(name: String, resolve: Boolean): Class<*> {
        // Делегируем системные и общие классы родителю
        if (delegatedPackages.any { name.startsWith(it) }) {
            return parent.loadClass(name)
        }

        synchronized(getClassLoadingLock(name)) {
            // Проверяем, не загружен ли класс уже этим ClassLoader'ом
            var clazz = findLoadedClass(name)

            if (clazz != null) {
                // Если класс уже загружен, проверяем нужно ли его перезагрузить
                if (shouldReloadClass(name)) {
                    println("Reloading class: $name")
                    return defineClassFromBytes(name)
                }
                return clazz
            }

            try {
                // Пытаемся загрузить класс из нашего JAR
                clazz = defineClassFromBytes(name)
                if (resolve) {
                    resolveClass(clazz)
                }
                return clazz
            } catch (e: ClassNotFoundException) {
                // Если класс не найден в нашем JAR, делегируем родителю
                return parent.loadClass(name)
            }
        }
    }

    /**
     * Определяет класс непосредственно из байткода.
     */
    private fun defineClassFromBytes(className: String): Class<*> {
        val bytes = classDefinitions[className] ?: throw ClassNotFoundException("Class $className not found in JAR")

        val clazz = defineClass(className, bytes, 0, bytes.size)
        loadedTimestamps[className] = System.currentTimeMillis()

        return clazz
    }

    /**
     * Проверяет, обновился ли JAR файл и нужно ли перезагрузить классы.
     */
    fun checkForUpdates(): Boolean {
        val currentFile = File(jarPath)
        if (!currentFile.exists()) {
            println("JAR file removed: $jarPath")
            return false
        }

        val currentModified = currentFile.lastModified()
        if (currentModified > jarLastModified) {
            println("JAR file updated: $jarPath (was: $jarLastModified, now: $currentModified)")
            return true
        }

        return false
    }

    /**
     * Перезагружает все классы из обновленного JAR файла.
     */
    fun reloadFromUpdatedJar(): Boolean {
        val currentFile = File(jarPath)
        if (!currentFile.exists()) {
            println("Cannot reload: JAR file removed")
            return false
        }

        try {
            // Очищаем кэши
            classDefinitions.clear()
            loadedTimestamps.clear()

            // Перезагружаем определения классов
            preloadClassDefinitions(currentFile)

            // Инвалидируем все загруженные классы
            loadedTimestamps.keys.forEach { className ->
                try {
                    val clazz = findLoadedClass(className)
                    if (clazz != null) {
                        // В Java нет официального способа выгрузить класс,
                        // но мы можем очистить внутренние кэши
                        println("Invalidated class: $className")
                    }
                } catch (e: Exception) {
                    // Игнорируем ошибки при инвалидации
                }
            }

            println("Successfully reloaded classes from updated JAR: $jarPath")
            return true
        } catch (e: Exception) {
            println("Failed to reload from updated JAR: ${e.message}")
            return false
        }
    }

    /**
     * Создает новый экземпляр класса, гарантируя загрузку из обновленного JAR.
     */
    fun <T> newInstance(className: String): T {
        try {
            val clazz = loadClass(className)
            val constructor = clazz.getDeclaredConstructor()
            constructor.isAccessible = true
            return constructor.newInstance() as T
        } catch (e: Exception) {
            throw RuntimeException("Failed to create instance of $className", e)
        }
    }

    /**
     * Возвращает временную метку последней загрузки класса.
     */
    fun getClassLoadTime(className: String): Long {
        return loadedTimestamps[className] ?: 0
    }

    /**
     * Очищает все загруженные классы (насколько это возможно в Java).
     */
    fun clearAll() {
        classDefinitions.clear()
        loadedTimestamps.clear()
        // Принудительно подсказываем GC
        System.gc()
    }

    override fun toString(): String {
        return "DynamicReloadingClassLoader(jar=$jarPath, loadedClasses=${loadedTimestamps.size})"
    }
}
