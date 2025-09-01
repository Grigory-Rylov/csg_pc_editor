package com.github.grishberg.cad3d.plugins

import java.io.File
import java.util.concurrent.ConcurrentHashMap

class PluginFileWatcherImpl(
    private val pluginsDir: File
) : PluginFileWatcher {
    private var listener: PluginFileWatcher.OnNewPluginsFoundListener? = null

    override fun setListener(listener: PluginFileWatcher.OnNewPluginsFoundListener?) {
        this.listener = listener
    }

    private var watchingThread: Thread? = null
    private var isWatching = false
    private val fileHashes = ConcurrentHashMap<String, String>()


    override fun startWatching() {
        if (isWatching) return

        isWatching = true
        watchingThread = Thread {
            while (isWatching) {
                try {
                    checkForChanges()
                    Thread.sleep(2000) // Проверка каждые 2 секунды
                } catch (e: InterruptedException) {
                    Thread.currentThread().interrupt()
                    break
                } catch (e: Exception) {
                    println("File watcher error: ${e.message}")
                }
            }
        }.apply {
            isDaemon = true
            start()
        }

        println("FileWatcher started for: ${pluginsDir.absolutePath}")
    }

    override fun stopWatching() {
        isWatching = false
        watchingThread?.interrupt()
        watchingThread = null
        println("FileWatcher stopped")
    }

    private fun checkForChanges() {
        val jarFiles = pluginsDir.listFiles { file ->
            file.extension.equals("jar", ignoreCase = true) && file.isFile
        } ?: emptyArray()

        jarFiles.forEach { jarFile ->
            val currentHash = calculateFileHash(jarFile)
            val previousHash = fileHashes[jarFile.absolutePath]

            if (previousHash == null) {
                // Новый файл
                fileHashes[jarFile.absolutePath] = currentHash
                listener?.onNewPluginsFound(jarFile)
            } else if (currentHash != previousHash) {
                // Файл изменился
                fileHashes[jarFile.absolutePath] = currentHash
                listener?.onNewPluginsFound(jarFile)
            }
        }

        // Проверяем удаленные файлы
        val iterator = fileHashes.iterator()
        while (iterator.hasNext()) {
            val (filePath, _) = iterator.next()
            if (!File(filePath).exists()) {
                iterator.remove()
                listener?.onNewPluginsFound(File(filePath))
            }
        }
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
