package com.github.grishberg.cad3d.plugins

import java.io.File

// Составной ключ для идентификации уникальных версий плагинов
internal data class PluginKey(
    val filePath: String,
    val lastModified: Long,
    val fileSize: Long
) {
    companion object {
        fun fromFile(file: File): PluginKey {
            return PluginKey(
                filePath = file.absolutePath,
                lastModified = file.lastModified(),
                fileSize = file.length()
            )
        }
    }

    // Строковое представление для использования в качестве ключа в Map
    fun toUniqueString(): String {
        return "$filePath|$lastModified|$fileSize"
    }
}
