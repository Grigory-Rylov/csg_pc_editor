package com.github.grishberg.viewer

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.github.grishberg.cad3d.R
import com.github.grishberg.cad3d.config.SceneConfigParser

class ScriptEditorActivity : Activity() {

    companion object {
        private const val EXTRA_SCRIPT = "extra_script"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_script_editor)

        val editor = findViewById<ScriptEditText>(R.id.script_editor)
        val defaultScript = intent.getStringExtra(EXTRA_SCRIPT)
            ?: ScriptStorage.loadScript(this)
            ?: SceneConfigParser().getDefaultScript()
        editor.setText(defaultScript)
        editor.setSelection(defaultScript.length)

        findViewById<Button>(R.id.script_cancel_button).setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }

        findViewById<Button>(R.id.script_ok_button).setOnClickListener {
            val script = editor.text.toString().trim()
            if (script.isEmpty()) {
                Toast.makeText(this, "Script is empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val parser = SceneConfigParser()
            val result = parser.parse(script)
            result.onSuccess {
                ScriptStorage.saveScript(this, script)
                val data = Intent().apply {
                    putExtra("result_script", script)
                }
                setResult(RESULT_OK, data)
                finish()
            }.onFailure { e ->
                Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }
}
