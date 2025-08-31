package com.github.grishberg.cad3d.plugins

import com.github.grishberg.cad3d.plugin.Cad3dPlugin
import com.github.grishberg.cad3d.plugin.VertexHolder
import java.io.File
import java.net.URLClassLoader
import java.util.jar.JarFile
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class PluginManager(
    private val pluginsDir: File,
    private val fileWatcher: FileWatcher,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
) {

    private val plugins = mutableListOf<Cad3dPlugin>()
    private val mutex = Mutex()


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
            println("Loaded ${plugins.size} plugins")
            plugins.toList()
        } catch (e: Exception) {
            println("Failed to load plugins: ${e.message}")
            emptyList()
        }
    }

    suspend fun getVertexHolders(): List<VertexHolder> = mutex.withLock {
        plugins.flatMap { it.getVertexHolders() }
    }

    fun stop() {
        fileWatcher.stopWatching()
        coroutineScope.cancel("PluginManager stopped")
    }

    fun start() {

    }

    private suspend fun loadPluginsFromDirectory(): List<Cad3dPlugin> = withContext(Dispatchers.IO) {
        if (!pluginsDir.exists() || !pluginsDir.isDirectory) return@withContext emptyList()

        val jarFiles = pluginsDir.listFiles { file ->
            file.extension.equals("jar", ignoreCase = true) && file.isFile
        } ?: emptyArray()

        jarFiles.mapNotNull { jarFile ->
            try {
                loadPluginFromJar(jarFile)
            } catch (e: Exception) {
                println("Failed to load plugin ${jarFile.name}: ${e.message}")
                null
            }
        }
    }

    private suspend fun loadPluginFromJar(jarFile: File): Cad3dPlugin? = withContext(Dispatchers.IO) {
        val jarUrl = jarFile.toURI().toURL()
        val classLoader = URLClassLoader(arrayOf(jarUrl), this::class.java.classLoader)

        JarFile(jarFile).use { jar ->
            val serviceEntry = jar.getJarEntry("META-INF/services/GeometryProvider")
            if (serviceEntry != null) {
                jar.getInputStream(serviceEntry).bufferedReader().useLines { lines ->
                    lines.firstOrNull()?.trim()?.let { className ->
                        loadPluginClass(classLoader, className)
                    }
                }
            } else {
                // Попробуем найти классы, реализующие интерфейс Plugin
                findPluginClasses(jar, classLoader).firstOrNull()?.let { className ->
                    loadPluginClass(classLoader, className)
                }
            }
        }
    }

    private suspend fun loadPluginClass(classLoader: ClassLoader, className: String): Cad3dPlugin? =
        withContext(Dispatchers.IO) {
            try {
                val pluginClass = classLoader.loadClass(className)
                if (Cad3dPlugin::class.java.isAssignableFrom(pluginClass)) {
                    val constructor = pluginClass.getDeclaredConstructor()
                    constructor.isAccessible = true
                    constructor.newInstance() as Cad3dPlugin
                } else {
                    null
                }
            } catch (e: Exception) {
                println("Failed to instantiate plugin $className: ${e.message}")
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

    suspend fun reloadPlugins() {
        val loadedPlugins = loadPlugins()
        withContext(Dispatchers.Main) {
            // Уведомляем UI о новых плагинах
            onPluginsReloaded(loadedPlugins)
        }
    }

    private fun onPluginsReloaded(plugins: List<Cad3dPlugin>) {
        // Здесь можно добавить логику уведомления UI
        println("Plugins reloaded: ${plugins.size} plugins available")
        plugins.forEach { plugin ->
            println(" - ${plugin.getName()} v${plugin.getVersion()}")
        }
    }

    fun getLoadedPlugins(): List<Cad3dPlugin> = plugins.toList()
}
