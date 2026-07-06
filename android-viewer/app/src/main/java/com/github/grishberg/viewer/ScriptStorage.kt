package com.github.grishberg.viewer

import android.content.Context
import java.io.File

object ScriptStorage {

    private const val SCRIPT_FILE = "scene_script.txt"

    fun saveScript(context: Context, script: String) {
        context.openFileOutput(SCRIPT_FILE, Context.MODE_PRIVATE).use { os ->
            os.write(script.toByteArray())
        }
    }

    fun loadScript(context: Context): String? {
        return try {
            context.openFileInput(SCRIPT_FILE).use { is_ ->
                is_.bufferedReader().readText()
            }
        } catch (_: Exception) {
            null
        }
    }

    fun hasScript(context: Context): Boolean {
        return File(context.filesDir, SCRIPT_FILE).exists()
    }
}
