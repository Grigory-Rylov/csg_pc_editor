package com.github.grishberg.viewer

import android.app.Activity
import android.app.ActivityManager
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.widget.CheckBox
import android.widget.CompoundButton
import com.github.grishberg.cad3d.R
import com.github.grishberg.cad3d.util.PcCaseSceneBuilder
import com.github.grishberg.cad3d.util.SceneBuilder

class Cad3dActivity : Activity() {

    private var mGLSurfaceView: CustomGLSurfaceView? = null
    private var mRenderer: MultipleObjectsRenderer? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_layout)

        mGLSurfaceView = findViewById<View>(R.id.gl_surface_view) as CustomGLSurfaceView

        val activityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val configurationInfo = activityManager.deviceConfigurationInfo
        val supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000

        if (supportsEs2) {
            mGLSurfaceView!!.setEGLContextClientVersion(2)

            val displayMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(displayMetrics)

            val builder: SceneBuilder = PcCaseSceneBuilder()

            mRenderer = MultipleObjectsRenderer(
                this, builder
            ) {
                mGLSurfaceView!!.requestRender()
            }

            mGLSurfaceView!!.setRenderer(mRenderer, displayMetrics.density)
            val wireframeCheckBox = findViewById<CheckBox>(R.id.wire_mode_checkbox)
            wireframeCheckBox.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
                mRenderer?.setWireframeOnly(isChecked)
            }

            builder.requestBuffers()
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
}
