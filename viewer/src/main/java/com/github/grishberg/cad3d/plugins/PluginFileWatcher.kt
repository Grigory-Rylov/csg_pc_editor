package com.github.grishberg.cad3d.plugins

import java.io.File

interface PluginFileWatcher {
    fun setListener(listener: OnNewPluginsFoundListener?)
    fun startWatching()
    fun stopWatching()
    interface OnNewPluginsFoundListener{
        fun onNewPluginsFound(file: File)
    }
}
