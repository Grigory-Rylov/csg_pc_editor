package com.github.grishberg.cad3d.plugins

import com.github.grishberg.cad3d.plugin.Cad3dPlugin
import java.io.File
import java.net.URL
import java.net.URLClassLoader
import java.util.jar.JarFile
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class PluginManager(
    private val pluginsDir: File,
    private val fileWatcher: FileWatcher,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
) {

    private val loadedJars = mutableSetOf<String>()
    private val plugins = mutableListOf<Cad3dPlugin>()
    private val mutex = Mutex()

    var onPluginLoadedListener: OnPluginLoadedListener? = null

    private val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        println("PluginManager error: ${exception.message}")
        exception.printStackTrace()
    }

    init {
        ensurePluginsDirectoryExists()
        startFileWatcher()
    }

    suspend fun loadPlugins(): List<Cad3dPlugin> = mutex.withLock {
        try {
            val newPlugins = loadPluginsFromDirectory()
            plugins.clear()
            plugins.addAll(newPlugins)
            println("--------------------- Loaded ${plugins.size} plugins")
            plugins.toList()
        } catch (e: Exception) {
            println("--------------------- Failed to load plugins: ${e.message}")
            emptyList()
        }
    }

    fun stop() {
        fileWatcher.stopWatching()
        coroutineScope.cancel("PluginManager stopped")
    }

    private suspend fun loadPluginsFromDirectory(): List<Cad3dPlugin> = withContext(Dispatchers.IO) {
        if (!pluginsDir.exists() || !pluginsDir.isDirectory) return@withContext emptyList()

        val jarFiles = pluginsDir.listFiles { file ->
            file.extension.equals("jar", ignoreCase = true) && file.isFile
        } ?: emptyArray()

        val newPlugins = mutableListOf<Cad3dPlugin>()

        jarFiles.forEach { jarFile ->
            try {
                // Проверяем, не загружали ли мы уже этот JAR
                if (!loadedJars.contains(jarFile.absolutePath)) {
                    val plugin = loadPluginFromJar(jarFile)
                    plugin?.let {
                        newPlugins.add(it)
                        loadedJars.add(jarFile.absolutePath)
                    }
                }
            } catch (e: Exception) {
                println("Failed to load plugin ${jarFile.name}: ${e.message}")
            }
        }

        newPlugins
    }

    private suspend fun loadPluginFromJar(jarFile: File): Cad3dPlugin? = withContext(Dispatchers.IO) {
        try {
            // Создаем полностью изолированный ClassLoader
            val jarUrl = jarFile.toURI().toURL()
            val classLoader = SafePluginClassLoader(arrayOf(jarUrl), this::class.java.classLoader)

            JarFile(jarFile).use { jar ->
                val serviceEntry = jar.getJarEntry("META-INF/services/com.github.grishberg.cad3d.plugin.Cad3dPlugin")
                val className = if (serviceEntry != null) {
                    jar.getInputStream(serviceEntry).bufferedReader().useLines { lines ->
                        lines.firstOrNull()?.trim()
                    }
                } else {
                    findPluginClasses(jar, classLoader).firstOrNull()
                }

                className?.let { loadPluginClass(classLoader, it, jarFile.name) }
            }
        } catch (e: Exception) {
            println("Failed to load plugin from ${jarFile.name}: ${e.message}")
            null
        }
    }

    private suspend fun findPluginClasses(jar: JarFile, classLoader: ClassLoader): List<String> =
        withContext(Dispatchers.IO) {
            jar.entries().asSequence().filter { it.name.endsWith(".class") }
                .map { it.name.removeSuffix(".class").replace('/', '.') }.filter { className ->
                    try {
                        val clazz = classLoader.loadClass(className)
                        Cad3dPlugin::class.java.isAssignableFrom(clazz) && !clazz.isInterface && !java.lang.reflect.Modifier.isAbstract(
                            clazz.modifiers
                        )
                    } catch (e: Exception) {
                        false
                    }
                }.toList()
        }

    private suspend fun loadPluginClass(
        classLoader: ClassLoader, className: String, jarName: String
    ): Cad3dPlugin? = withContext(Dispatchers.IO) {
        try {
            println("Loading class: $className from $jarName")

            val pluginClass = classLoader.loadClass(className)
            if (Cad3dPlugin::class.java.isAssignableFrom(pluginClass)) {
                val constructor = pluginClass.getDeclaredConstructor()
                constructor.isAccessible = true
                val plugin = constructor.newInstance() as Cad3dPlugin

                // Добавляем идентификатор для отладки
                println("Successfully loaded: ${plugin.name} v${plugin.version} from $jarName")
                println("ClassLoader: ${classLoader.hashCode()}, Instance: ${plugin.hashCode()}")

                plugin
            } else {
                println("Class $className does not implement Cad3dPlugin")
                null
            }
        } catch (e: Exception) {
            println("Failed to instantiate plugin $className from $jarName: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    // Кастомный ClassLoader для полной изоляции
    private class SafePluginClassLoader(
        urls: Array<URL>,
        parent: ClassLoader
    ) : URLClassLoader(urls, parent) {

        private val sharedPackages = setOf(
            "com.github.grishberg.cad3d.",
            "kotlin.",
            "java.",
            "javax.",
            "org.jetbrains.",
            "org.jogamp.",
            "com.jogamp."
        )

        override fun loadClass(name: String, resolve: Boolean): Class<*> {
            // Все классы из общих пакетов делегируем родителю
            if (sharedPackages.any { name.startsWith(it) }) {
                return parent.loadClass(name)
            }

            synchronized(getClassLoadingLock(name)) {
                // Проверяем, не загружен ли уже класс
                var c = findLoadedClass(name)
                if (c == null) {
                    try {
                        c = findClass(name)
                    } catch (e: ClassNotFoundException) {
                        c = super.loadClass(name, resolve)
                    }
                }
                if (resolve) {
                    resolveClass(c)
                }
                return c
            }
        }
    }

    suspend fun reloadPlugins() {
        // Очищаем кэш загруженных JAR при перезагрузке
        loadedJars.clear()
        val loadedPlugins = loadPlugins()
        onPluginsReloaded(loadedPlugins)
        withContext(Dispatchers.Main) {
            onPluginLoadedListener?.onPluginsLoaded(loadedPlugins)
        }
    }

    private fun ensurePluginsDirectoryExists() {
        if (!pluginsDir.exists()) {
            pluginsDir.mkdirs()
        }
    }

    private fun startFileWatcher() {
        fileWatcher.onPluginFound = {
            coroutineScope.launch {
                reloadPlugins()
            }
        }
        fileWatcher.startWatching()
    }

    private fun onPluginsReloaded(plugins: List<Cad3dPlugin>) {
        // Здесь можно добавить логику уведомления UI
        println("Plugins reloaded: ${plugins.size} plugins available")
        plugins.forEach { plugin ->
            println(" - ${plugin.name} v${plugin.version}")
        }
    }

    interface OnPluginLoadedListener {

        fun onPluginsLoaded(plugins: List<Cad3dPlugin>)
    }
}
