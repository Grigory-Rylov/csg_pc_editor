package com.github.grishberg.viewer

import android.app.AlertDialog
import android.os.Bundle
import android.widget.FrameLayout
import android.util.DisplayMetrics
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.github.grishberg.cad3d.R
import com.github.grishberg.cad3d.config.SceneConfigParser
import com.github.grishberg.cad3d.pccase.AluminumProfile
import com.github.grishberg.cad3d.pccase.PcCaseModelFactory
import com.github.grishberg.cad3d.pccase.SceneConfig
import com.github.grishberg.cad3d.util.PcCaseSceneBuilder

class Cad3dActivity : AppCompatActivity() {

    private var mGLSurfaceView: CustomGLSurfaceView? = null
    private var mRenderer: MultipleObjectsRenderer? = null
    private var mSceneBuilder: PcCaseSceneBuilder? = null
    private var lastValidScript: String = ""
    private var editorBehavior: BottomSheetBehavior<FrameLayout>? = null
    private var editorView: FrameLayout? = null
    private var scriptEditor: ScriptEditText? = null
    private var fabApply: FloatingActionButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_layout)

        mGLSurfaceView = findViewById<View>(R.id.gl_surface_view) as CustomGLSurfaceView
        lastValidScript = ScriptStorage.loadScript(this) ?: SceneConfigParser().getDefaultScript()

        val configurationInfo =
            (getSystemService(ACTIVITY_SERVICE) as android.app.ActivityManager).deviceConfigurationInfo
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

            val toolbar = findViewById<Toolbar>(R.id.toolbar)
            setSupportActionBar(toolbar)
            supportActionBar?.title = "PC Case Editor"

            // Setup bottom sheet editor
            setupEditorBottomSheet()

            SceneConfigParser().parse(lastValidScript).onSuccess { config ->
                mSceneBuilder?.updateConfig(config)
            }

            mSceneBuilder!!.requestBuffers()
        }
    }

    private fun applyScript() {
        val script = scriptEditor?.text?.toString()?.trim() ?: ""
        if (script.isEmpty()) {
            Toast.makeText(this, "Script is empty", Toast.LENGTH_SHORT).show()
            return
        }

        val parser = SceneConfigParser()
        val result = parser.parse(script)
        result.onSuccess { config ->
            lastValidScript = script
            ScriptStorage.saveScript(this, script)
            mSceneBuilder?.updateConfig(config)
            mRenderer?.requestRender()
            Toast.makeText(this, "Scene updated", Toast.LENGTH_SHORT).show()
        }.onFailure { e ->
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
        }
    }

    private fun setupEditorBottomSheet() {
        editorView = findViewById(R.id.editor_bottom_sheet)
        scriptEditor = findViewById(R.id.script_editor)
        scriptEditor?.setText(lastValidScript)
        scriptEditor?.setSelection(lastValidScript.length)
        fabApply = findViewById(R.id.fab_apply)

        fabApply?.setOnClickListener {
            applyScript()
        }

        editorBehavior = BottomSheetBehavior.from(editorView!!).apply {
            isHideable = false
            addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    when (newState) {
                        BottomSheetBehavior.STATE_COLLAPSED -> {
                            scriptEditor?.setText(lastValidScript)
                            fabApply?.visibility = View.GONE
                        }
                        BottomSheetBehavior.STATE_EXPANDED -> {
                            fabApply?.visibility = View.VISIBLE
                        }
                    }
                }
                override fun onSlide(bottomSheet: View, slideOffset: Float) {}
            })
        }

        // Set COLLAPSED after first layout pass so BottomSheetBehavior can position correctly
        editorView?.post {
            editorBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }

    override fun onResume() {
        super.onResume()
        mGLSurfaceView?.onResume()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_editor -> {
                when (editorBehavior?.state) {
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        editorBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
                    }
                    else -> {
                        editorBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
                    }
                }
                true
            }
            R.id.action_report -> {
                showProfileReport()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onPause() {
        super.onPause()
        mGLSurfaceView?.onPause()
    }

    private fun showProfileReport() {
        val config = SceneConfigParser().parse(lastValidScript).getOrNull() ?: SceneConfig.DEFAULT
        val models = PcCaseModelFactory.buildAll(config)
        models.forEach { (_, csg) -> csg.getVerticesAndColorsAsFloatArray() }
        val report = AluminumProfile.generateReport()
        AlertDialog.Builder(this)
            .setTitle("Отчет по профилям")
            .setMessage(report)
            .setPositiveButton("OK") { _, _ -> }
            .setCancelable(true)
            .show()
    }
}
