package com.github.grishberg.cad3d.plugins

import com.github.grishberg.cad3d.plugin.Cad3dPlugin
import java.io.File
import java.net.URLClassLoader
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PluginManagerImpl(
    private val pluginsDir: File,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
) : PluginManager {

    private val fileWatcher = PluginFileWatcherImpl(pluginsDir)

    // Храним ClassLoader'ы по уникальным ключам
    private val activeLoaders = ConcurrentHashMap<String, DynamicReloadingClassLoader>()
    private val pluginInstances = ConcurrentHashMap<String, Cad3dPlugin>()

    // Маппинг имени файла на последний загруженный ключ
    private val fileLastKeyMapping = ConcurrentHashMap<String, String>()

    // Маппинг ключа на метаданные файла
    private val keyToMetadataMapping = ConcurrentHashMap<String, FileMetadata>()

    private var onPluginLoadedListener: PluginManager.OnPluginLoadedListener? = null

    init {
        ensurePluginsDirectoryExists()
    }

    override fun setOnPluginLoadedListener(listener: PluginManager.OnPluginLoadedListener?) {
        onPluginLoadedListener = listener
    }

    override fun start() {
        fileWatcher.setListener(object : PluginFileWatcher.OnNewPluginsFoundListener {
            override fun onNewPluginsFound(file: File) {
                coroutineScope.launch {
                    handlePluginFileChanged(file)
                }
            }
        })
        fileWatcher.startWatching()
    }

    private suspend fun handlePluginFileChanged(file: File) {
        try {
            val newPlugin = loadOrReloadPlugin(file)
            if (newPlugin != null) {
                // Выгружаем старую версию этого же файла (если была)
                unloadOldVersionsOfFile(file)

                val currentPlugins = getLoadedPlugins()
                withContext(Dispatchers.Main) {
                    onPluginLoadedListener?.onPluginsLoaded(currentPlugins)
                }
            }
        } catch (e: Exception) {
            println("Failed to handle plugin file change for ${file.name}: ${e.message}")
        }
    }

    override fun stop() {
        fileWatcher.stopWatching()
        coroutineScope.cancel("PluginManager stopped")
        shutdown()
    }

    /**
     * Загружает или перезагружает плагин из JAR файла.
     * Автоматически выгружает старые версии того же файла.
     */
    private fun loadOrReloadPlugin(jarFile: File): Cad3dPlugin? {
        val filePath = jarFile.absolutePath
        val currentMetadata = FileMetadata.fromFile(jarFile)
        val uniqueKey = "${filePath}|${currentMetadata.lastModified}|${currentMetadata.fileSize}"

        return try {
            // Проверяем, есть ли уже загруженная версия этого файла
            val previousKey = fileLastKeyMapping[filePath]

            // Если файл изменился (новая версия), выгружаем старую
            if (previousKey != null && previousKey != uniqueKey) {
                println("New version detected for $filePath, unloading old version")
                unloadPlugin(previousKey)
            }

            val loader = activeLoaders.computeIfAbsent(uniqueKey) {
                println("Creating new ClassLoader for: $filePath (version: ${currentMetadata.lastModified})")
                DynamicReloadingClassLoader(jarFile, this::class.java.classLoader)
            }

            // Создаем экземпляр плагина
            val plugin = loader.newInstance<Cad3dPlugin>(findPluginClassName(jarFile))

            plugin?.let {
                pluginInstances[uniqueKey] = it
                fileLastKeyMapping[filePath] = uniqueKey
                keyToMetadataMapping[uniqueKey] = currentMetadata

                println("Loaded plugin: ${it.name} v${it.version} from $filePath")
                println("  Version timestamp: ${currentMetadata.lastModified}")
                println("  Loader key: ${uniqueKey.hashCode()}")
            }

            plugin
        } catch (e: Exception) {
            println("Failed to load plugin from $filePath: ${e.message}")
            null
        }
    }

    /**
     * Выгружает старые версии указанного файла.
     */
    private fun unloadOldVersionsOfFile(file: File) {
        val filePath = file.absolutePath
        val currentKey = fileLastKeyMapping[filePath]

        // Находим и выгружаем все ключи для этого файла, кроме текущего
        keyToMetadataMapping.keys.forEach { key ->
            if (key.startsWith("$filePath|") && key != currentKey) {
                println("Unloading old version of $filePath with key: $key")
                unloadPlugin(key)
            }
        }
    }

    /**
     * Выгружает конкретный плагин по ключу.
     */
    private fun unloadPlugin(key: String) {
        // Удаляем ClassLoader и экземпляр плагина
        activeLoaders.remove(key)?.clearAll()
        pluginInstances.remove(key)?.onUnload()
        keyToMetadataMapping.remove(key)

        // Очищаем маппинг файла, если это была последняя версия
        fileLastKeyMapping.entries.removeIf { (_, value) -> value == key }

        println("Unloaded plugin with key: $key")
        System.gc() // Подсказываем GC освободить память
    }

    /**
     * Находит имя класса плагина в JAR файле.
     */
    private fun findPluginClassName(jarFile: File): String {
        return java.util.jar.JarFile(jarFile).use { jar ->
            val serviceEntry = jar.getJarEntry("META-INF/services/com.github.grishberg.cad3d.plugin.Cad3dPlugin")
            if (serviceEntry != null) {
                return jar.getInputStream(serviceEntry).bufferedReader().useLines { lines ->
                    lines.firstOrNull()?.trim() ?: throw RuntimeException("Service file empty")
                }
            }

            jar.entries().asSequence().filter { it.name.endsWith(".class") }
                .map { it.name.removeSuffix(".class").replace('/', '.') }.filter { className ->
                    try {
                        val tempLoader = URLClassLoader(arrayOf(jarFile.toURI().toURL()))
                        val clazz = tempLoader.loadClass(className)
                        Cad3dPlugin::class.java.isAssignableFrom(clazz) && !clazz.isInterface && !java.lang.reflect.Modifier.isAbstract(
                            clazz.modifiers
                        )
                    } catch (e: Exception) {
                        false
                    }
                }.firstOrNull() ?: throw RuntimeException("No plugin class found in JAR: ${jarFile.name}")
        }
    }

    private fun ensurePluginsDirectoryExists() {
        if (!pluginsDir.exists()) {
            pluginsDir.mkdirs()
        }
    }

    /**
     * Возвращает все загруженные плагины.
     */
    fun getLoadedPlugins(): List<Cad3dPlugin> {
        return pluginInstances.values.toList()
    }

    /**
     * Очищает все ресурсы.
     */
    private fun shutdown() {
        // Выгружаем все плагины
        activeLoaders.keys.toList().forEach { unloadPlugin(it) }

        // Очищаем все коллекции
        activeLoaders.clear()
        pluginInstances.clear()
        fileLastKeyMapping.clear()
        keyToMetadataMapping.clear()

        println("All plugins and resources cleared")
    }
}

// Класс метаданных файла (должен быть в том же файле или вынесен отдельно)
private data class FileMetadata(
    val lastModified: Long, val fileSize: Long, val fileHash: String
) {

    companion object {

        fun fromFile(file: File): FileMetadata {
            return FileMetadata(
                lastModified = file.lastModified(), fileSize = file.length(), fileHash = calculateFileHash(file)
            )
        }

        private fun calculateFileHash(file: File): String {
            return file.inputStream().use { input ->
                val digest = java.security.MessageDigest.getInstance("SHA-256")
                val buffer = ByteArray(8192)
                var bytesRead: Int
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    digest.update(buffer, 0, bytesRead)
                }
                digest.digest().joinToString("") { "%02x".format(it) }
            }
        }
    }
}
