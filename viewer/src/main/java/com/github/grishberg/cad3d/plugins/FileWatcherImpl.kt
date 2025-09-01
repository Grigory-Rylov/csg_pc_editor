package com.github.grishberg.cad3d.plugins

import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FileWatcherImpl(
    private val pluginsDir: File,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
) {

    private var lastModified = pluginsDir.lastModified()
    private var job: Job? = null

    var onPluginFound: () -> Unit = {}

    fun startWatching() {
        job = scope.launch {
            while (isActive) {
                delay(2000) // Проверка каждые 2 секунды
                checkForChanges()
            }
        }
    }

    fun stopWatching() {
        job?.cancel()
        job = null
    }

    private suspend fun checkForChanges() {
        if (pluginsDir.lastModified() > lastModified) {
            lastModified = pluginsDir.lastModified()
            withContext(Dispatchers.Main) {
                onPluginFound.invoke()
            }
        }
    }
}
