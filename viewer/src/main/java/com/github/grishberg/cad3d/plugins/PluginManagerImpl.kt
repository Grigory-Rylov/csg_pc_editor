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
    private val fileWatcher: FileWatcherImpl,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
) : PluginManager {

    // Используем составной ключ вместо просто пути к файлу
    private val activeLoaders = ConcurrentHashMap<String, DynamicReloadingClassLoader>()
    private val pluginInstances = ConcurrentHashMap<String, Cad3dPlugin>()
    private val fileToKeyMapping = ConcurrentHashMap<String, String>()

    private var onPluginLoadedListener: PluginManager.OnPluginLoadedListener? = null

    init {
        ensurePluginsDirectoryExists()
    }

    override fun start() {
        startFileWatcher()
    }

    override fun setOnPluginLoadedListener(listener: PluginManager.OnPluginLoadedListener?) {
        onPluginLoadedListener = listener
    }

    override fun stop() {
        fileWatcher.stopWatching()
        coroutineScope.cancel("PluginManager stopped")
        shutdown()
    }

    fun loadPlugins(): List<Cad3dPlugin> {
        val pluginsList = mutableListOf<Cad3dPlugin>()
        getPluginFiles().forEach { jarFile ->
            loadOrReloadPlugin(jarFile)?.let { plugin ->
                pluginsList.add(plugin)
            }
        }
        return pluginsList
    }

    private fun getPluginFiles(): Array<out File> {
        return pluginsDir.listFiles { file ->
            file.extension.equals("jar", ignoreCase = true) && file.isFile
        } ?: emptyArray()
    }

    /**
     * Загружает или перезагружает плагин из JAR файла.
     */
    fun loadOrReloadPlugin(jarFile: File): Cad3dPlugin? {
        val pluginKey = PluginKey.fromFile(jarFile)
        val uniqueKey = pluginKey.toUniqueString()
        val filePath = jarFile.absolutePath

        return try {
            // Проверяем, изменился ли файл по сравнению с предыдущей загрузкой
            val previousKey = fileToKeyMapping[filePath]
            val isFileChanged = previousKey != uniqueKey

            val loader = if (isFileChanged) {
                // Файл изменился - создаем новый ClassLoader
                println("========= File changed, creating new ClassLoader for: $filePath")
                fileToKeyMapping[filePath] = uniqueKey

                // Удаляем старый ClassLoader если он был
                previousKey?.let { activeLoaders.remove(it)?.clearAll() }

                DynamicReloadingClassLoader(jarFile, this::class.java.classLoader).also {
                    activeLoaders[uniqueKey] = it
                }
            } else {
                // Файл не изменился - используем существующий ClassLoader
                activeLoaders[uniqueKey] ?: run {
                    println("========= Creating new ClassLoader for unchanged file: $filePath")
                    DynamicReloadingClassLoader(jarFile, this::class.java.classLoader).also {
                        activeLoaders[uniqueKey] = it
                        fileToKeyMapping[filePath] = uniqueKey
                    }
                }
            }

            // Создаем новый экземпляр плагина
            val plugin = loader.newInstance<Cad3dPlugin>(findPluginClassName(jarFile))
            plugin?.let {
                pluginInstances[uniqueKey] = it
                println("=========== Loaded plugin: ${it.name} v${it.version} from $filePath (key: ${uniqueKey.hashCode()})")
            }

            plugin
        } catch (e: Exception) {
            println("Failed to load plugin from $filePath: ${e.message}")
            null
        }
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

            // Альтернативный поиск класса плагина
            jar.entries().asSequence()
                .filter { it.name.endsWith(".class") }
                .map { it.name.removeSuffix(".class").replace('/', '.') }
                .filter { className ->
                    try {
                        // Используем временный ClassLoader для проверки
                        val tempLoader = URLClassLoader(arrayOf(jarFile.toURI().toURL()))
                        val clazz = tempLoader.loadClass(className)
                        Cad3dPlugin::class.java.isAssignableFrom(clazz) &&
                            !clazz.isInterface &&
                            !java.lang.reflect.Modifier.isAbstract(clazz.modifiers)
                    } catch (e: Exception) {
                        false
                    }
                }
                .firstOrNull()
                ?: throw RuntimeException("No plugin class found in JAR: ${jarFile.name}")
        }
    }

    /**
     * Проверяет обновления всех плагинов.
     */
    fun checkForUpdates(): List<Cad3dPlugin> {
        val updatedPlugins = mutableListOf<Cad3dPlugin>()
        val currentFiles = getPluginFiles()

        // Проверяем существующие файлы
        currentFiles.forEach { jarFile ->
            try {
                val plugin = loadOrReloadPlugin(jarFile)
                plugin?.let { updatedPlugins.add(it) }
            } catch (e: Exception) {
                println("Failed to update plugin ${jarFile.name}: ${e.message}")
            }
        }

        // Удаляем плагины для файлов, которых больше нет
        cleanupRemovedPlugins(currentFiles)

        return updatedPlugins
    }

    /**
     * Очищает плагины для удаленных файлов.
     */
    private fun cleanupRemovedPlugins(currentFiles: Array<out File>) {
        val currentPaths = currentFiles.map { it.absolutePath }.toSet()

        // Находим ключи для удаленных файлов
        val keysToRemove = fileToKeyMapping.filter { (filePath, _) ->
            !currentPaths.contains(filePath)
        }.values.toSet()

        // Удаляем ClassLoader'ы и плагины
        keysToRemove.forEach { key ->
            activeLoaders.remove(key)?.clearAll()
            pluginInstances.remove(key)
            println("Removed plugin for deleted file (key: $key)")
        }

        // Очищаем маппинг файлов
        fileToKeyMapping.keys.removeAll { filePath -> !currentPaths.contains(filePath) }
    }

    /**
     * Выгружает плагин по ключу.
     */
    fun unloadPlugin(key: String) {
        pluginInstances.remove(key)
        activeLoaders.remove(key)?.clearAll()

        // Удаляем маппинг файла
        fileToKeyMapping.entries.removeIf { (_, value) -> value == key }

        println("Unloaded plugin with key: $key")
    }

    suspend fun reloadPlugins() {
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
        fileWatcher.onPluginFound = { changedFile ->
            coroutineScope.launch {
                try {
                    val plugin = loadOrReloadPlugin(changedFile)
                    if (plugin != null) {
                        val currentPlugins = getLoadedPlugins()
                        withContext(Dispatchers.Main) {
                            onPluginLoadedListener?.onPluginsLoaded(currentPlugins)
                        }
                    }
                } catch (e: Exception) {
                    println("Failed to reload plugin ${changedFile.name}: ${e.message}")
                }
            }
        }
        fileWatcher.startWatching()
    }

    private fun onPluginsReloaded(plugins: List<Cad3dPlugin>) {
        println("Plugins reloaded: ${plugins.size} plugins available")
        plugins.forEach { plugin ->
            println(" - ${plugin.name} v${plugin.version}")
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
    fun shutdown() {
        activeLoaders.values.forEach { it.clearAll() }
        activeLoaders.clear()
        pluginInstances.clear()
        fileToKeyMapping.clear()
        System.gc()
        println("All plugins and resources cleared")
    }
}
