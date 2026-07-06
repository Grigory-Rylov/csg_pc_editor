package com.github.grishberg.viewer

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.Toast
import com.github.grishberg.cad3d.R
import com.github.grishberg.cad3d.pccase.SceneConfig
import com.github.grishberg.cad3d.pccase.SceneConfigParser
import com.github.grishberg.cad3d.util.PcCaseSceneBuilder
import com.github.grishberg.cad3d.util.SceneBuilder

class Cad3dActivity : Activity() {

    private var mGLSurfaceView: CustomGLSurfaceView? = null
    private var mRenderer: MultipleObjectsRenderer? = null
    private var mSceneBuilder: PcCaseSceneBuilder? = null
    private var lastValidScript: String = ""

    companion object {
        private const val REQUEST_SCRIPT_EDITOR = 1
        private const val EXTRA_SCRIPT = "extra_script"
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_layout)

        mGLSurfaceView = findViewById<View>(R.id.gl_surface_view) as CustomGLSurfaceView
        lastValidScript = ScriptStorage.loadScript(this) ?: SceneConfigParser().getDefaultScript()

        val configurationInfo = (getSystemService(ACTIVITY_SERVICE) as android.app.ActivityManager).deviceConfigurationInfo
        val supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000

        if (supportsEs2) {
            mGLSurfaceView!!.setEGLContextClientVersion(2)

            val displayMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(displayMetrics)

            mSceneBuilder = PcCaseSceneBuilder()

            mRenderer = MultipleObjectsRenderer(
                this, mSceneBuilder!!
            ) {
                mGLSurfaceView!!.requestRender()
            }

            mGLSurfaceView!!.setRenderer(mRenderer, displayMetrics.density)
            val wireframeCheckBox = findViewById<CheckBox>(R.id.wire_mode_checkbox)
            wireframeCheckBox.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
                mRenderer?.setWireframeOnly(isChecked)
            }

            findViewById<Button>(R.id.edit_script_button).setOnClickListener {
                val intent = Intent(this, ScriptEditorActivity::class.java)
                intent.putExtra(EXTRA_SCRIPT, lastValidScript)
                startActivityForResult(intent, REQUEST_SCRIPT_EDITOR)
            }

            // Apply saved script on startup
            SceneConfigParser().parse(lastValidScript).onSuccess { config ->
                mSceneBuilder?.updateConfig(config)
            }

            mSceneBuilder!!.requestBuffers()
        }
    }

    override fun onResume() {
        super.onResume()
        mGLSurfaceView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        mGLSurfaceView?.onPause()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_SCRIPT_EDITOR && resultCode == RESULT_OK) {
            val script = data?.getStringExtra("result_script")
            if (script != null) {
                val parser = SceneConfigParser()
                val result = parser.parse(script)
                result.onSuccess { config ->
                    lastValidScript = script
                    ScriptStorage.saveScript(this, script)
                    mSceneBuilder?.updateConfig(config)
                    mRenderer?.requestRender()
                    Toast.makeText(this, "Scene updated", Toast.LENGTH_SHORT).show()
                }.onFailure { e ->
                    Toast.makeText(this, "Parse error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
