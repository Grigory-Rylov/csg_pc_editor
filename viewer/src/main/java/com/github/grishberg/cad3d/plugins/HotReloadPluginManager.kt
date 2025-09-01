package com.github.grishberg.cad3d.plugins

import com.github.grishberg.cad3d.plugin.Cad3dPlugin
import java.io.File
import java.io.FileFilter
import java.net.URLClassLoader
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HotReloadPluginManager(
    private val pluginsDir: File,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
) : PluginManager {

    private val activeLoaders = ConcurrentHashMap<String, DynamicReloadingClassLoader>()
    private val pluginInstances = ConcurrentHashMap<String, Cad3dPlugin>()
    private var fileWatcher: PluginFileWatcher? = PluginFileWatcherImpl(pluginsDir)
    private var updateTimer: Timer? = null

    @Volatile private var onPluginLoadedListener: PluginManager.OnPluginLoadedListener? = null

    override fun setOnPluginLoadedListener(listener: PluginManager.OnPluginLoadedListener?) {
        this.onPluginLoadedListener = listener
    }

    override fun start() {
        // Загружаем существующие плагины при старте
        loadAllPlugins()

        // Запускаем FileWatcher для отслеживания изменений файлов
        startFileWatcher()

        // Запускаем периодическую проверку обновлений
        startUpdateTimer()

        println("PluginManager started watching: ${pluginsDir.absolutePath}")
    }

    override fun stop() {
        // Останавливаем таймер
        updateTimer?.cancel()
        updateTimer = null

        // Останавливаем FileWatcher
        fileWatcher?.stopWatching()
        fileWatcher = null

        // Выгружаем все плагины
        unloadAllPlugins()

        // Отменяем корутины
        coroutineScope.cancel()

        println("PluginManager stopped")
    }

    /**
     * Загружает все плагины из директории.
     */
    private fun loadAllPlugins() {
        val loadedPlugins = mutableListOf<Cad3dPlugin>()

        val jarFiles = pluginsDir.listFiles { file ->
            file.extension.equals("jar", ignoreCase = true) && file.isFile
        } ?: emptyArray()

        jarFiles.forEach { jarFile ->
            try {
                val plugin = loadOrReloadPlugin(jarFile)
                plugin?.let { loadedPlugins.add(it) }
            } catch (e: Exception) {
                println("Failed to load plugin ${jarFile.name}: ${e.message}")
            }
        }

        notifyPluginsLoaded(loadedPlugins)
    }

    /**
     * Загружает или перезагружает плагин из JAR файла.
     */
    private fun loadOrReloadPlugin(jarFile: File): Cad3dPlugin? {
        val jarPath = jarFile.absolutePath

        return try {
            val loader = activeLoaders.compute(jarPath) { _, existingLoader ->
                if (existingLoader != null && existingLoader.checkForUpdates()) {
                    println("Reloading updated JAR: $jarPath")
                    existingLoader.reloadFromUpdatedJar()
                    existingLoader
                } else if (existingLoader == null) {
                    println("Loading new JAR: $jarPath")
                    DynamicReloadingClassLoader(jarFile, this::class.java.classLoader)
                } else {
                    existingLoader
                }
            }

            val pluginClassName = findPluginClassName(jarFile)
            val plugin = loader?.newInstance<Cad3dPlugin>(pluginClassName)

            plugin?.let {
                pluginInstances[jarPath] = it
                println("Loaded plugin: ${it.name} v${it.version} from $jarPath")
            }

            plugin
        } catch (e: Exception) {
            println("Failed to load plugin from $jarPath: ${e.message}")
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

    /**
     * Запускает FileWatcher для отслеживания изменений файлов.
     */
    private fun startFileWatcher() {
        fileWatcher = PluginFileWatcherImpl(pluginsDir).apply {
            setListener(object : PluginFileWatcher.OnNewPluginsFoundListener {
                override fun onNewPluginsFound(file: File) {
                    coroutineScope.launch {
                        handleJarChanged(file)
                    }
                }
            })
            startWatching()
        }
    }

    /**
     * Обрабатывает изменение JAR файла.
     */
    private suspend fun handleJarChanged(jarFile: File) = withContext(Dispatchers.IO) {
        try {
            val plugin = loadOrReloadPlugin(jarFile)
            if (plugin != null) {
                val currentPlugins = getLoadedPlugins()
                withContext(Dispatchers.Main) {
                    onPluginLoadedListener?.onPluginsLoaded(currentPlugins)
                }
            }
        } catch (e: Exception) {
            println("Failed to handle JAR change for ${jarFile.name}: ${e.message}")
        }
    }

    /**
     * Запускает таймер для периодической проверки обновлений.
     */
    private fun startUpdateTimer() {
        updateTimer = Timer().apply {
            scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    checkForUpdates()
                }
            }, 5000, 5000) // Проверка каждые 5 секунд
        }
    }

    /**
     * Проверяет обновления всех плагинов.
     */
    private fun checkForUpdates() {
        val updatedPlugins = mutableListOf<Cad3dPlugin>()
        var needsNotification = false

        activeLoaders.keys.forEach { jarPath ->
            try {
                val jarFile = File(jarPath)
                if (jarFile.exists()) {
                    val plugin = loadOrReloadPlugin(jarFile)
                    plugin?.let {
                        updatedPlugins.add(it)
                        needsNotification = true
                    }
                } else {
                    // JAR файл был удален
                    unloadPlugin(jarPath)
                    needsNotification = true
                }
            } catch (e: Exception) {
                println("Failed to update plugin $jarPath: ${e.message}")
            }
        }

        if (needsNotification) {
            notifyPluginsLoaded(getLoadedPlugins())
        }
    }

    /**
     * Уведомляет слушателя о изменении списка плагинов.
     */
    private fun notifyPluginsLoaded(plugins: List<Cad3dPlugin>) {
        // Вызываем в главном потоке (предполагая, что это Swing/JavaFX приложение)
        if (onPluginLoadedListener != null) {
            kotlin.runCatching {
                javax.swing.SwingUtilities.invokeLater {
                    onPluginLoadedListener?.onPluginsLoaded(plugins)
                }
            }.onFailure {
                // Fallback: вызываем в текущем потоке
                onPluginLoadedListener?.onPluginsLoaded(plugins)
            }
        }
    }

    /**
     * Выгружает конкретный плагин.
     */
    private fun unloadPlugin(jarPath: String) {
        pluginInstances.remove(jarPath)
        activeLoaders.remove(jarPath)?.clearAll()
        println("Unloaded plugin: $jarPath")
    }

    /**
     * Выгружает все плагины.
     */
    private fun unloadAllPlugins() {
        activeLoaders.values.forEach { it.clearAll() }
        activeLoaders.clear()
        pluginInstances.clear()
        System.gc()
        println("All plugins unloaded")
    }

    /**
     * Возвращает все загруженные плагины.
     */
    fun getLoadedPlugins(): List<Cad3dPlugin> {
        return pluginInstances.values.toList()
    }

    /**
     * Возвращает плагин по имени JAR файла.
     */
    fun getPluginByJarPath(jarPath: String): Cad3dPlugin? {
        return pluginInstances[jarPath]
    }

    /**
     * Очищает все ресурсы (более полная версия stop).
     */
    fun shutdown() {
        stop()
    }
}
