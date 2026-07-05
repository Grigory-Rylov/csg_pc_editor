package com.github.grishberg.viewer

import android.app.Activity
import android.app.ActivityManager
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.TextView
import com.github.grishberg.cad3d.R
import com.github.grishberg.cad3d.common.DebugRecorder
import com.github.grishberg.cad3d.common.Logger
import com.github.grishberg.cad3d.keyboard.KeyPlace
import com.github.grishberg.cad3d.keyboard.cfg.AssemblySettings
import com.github.grishberg.cad3d.keyboard.cfg.KeyboardConfig
import com.github.grishberg.cad3d.ui.DebugRecorderImpl
import com.github.grishberg.cad3d.ui.DebugVisualizer
import com.github.grishberg.cad3d.ui.DebugVisualizerImpl
import com.github.grishberg.cad3d.ui.LogListViewLogger
import com.github.grishberg.cad3d.util.DebugGenerator
import com.github.grishberg.cad3d.util.SceneBuilder
import com.github.grishberg.cad3d.util.SceneBuilderKeyboard
import com.github.grishberg.cad3d.util.PcCaseSceneBuilder
import com.github.grishberg.cad3d.util.SceneBuilderTest

class Cad3dActivity : Activity() {

    /**
     * Hold a reference to our GLSurfaceView
     */
    private var mGLSurfaceView: CustomGLSurfaceView? = null
    private var mRenderer: MultipleObjectsRenderer? = null
    private val rowsCount = 3
    private val colsCount = 5
    private val isWireMode = false

    // Компоненты для логирования
    private lateinit var logsRecyclerView: LogListView
    private lateinit var logger: LogListViewLogger
    private lateinit var toggleLogsButton: Button
    private lateinit var clearLogsButton: Button
    private var isLogsVisible = false

    // Компоненты для отладки
    private lateinit var debugRecorder: DebugRecorder
    private lateinit var debugVisualizer: DebugVisualizer
    private lateinit var prevDebugButton: Button
    private lateinit var nextDebugButton: Button
    private lateinit var debugInfoText: TextView
    private lateinit var debugOverlay: DebugOverlayView
    private var currentDebugIndex = -1

    private val cfg = KeyboardConfig(
        fn = 6, stlFn = 6, rowsCount = 3, columnsCount = 3, assemblySettings = AssemblySettings(
            settingsShowCaps = false,
            settingsShowMatrix = true,
            settingsShowCase = false,
            settingsShowPlate = false,
        )
    )

    private val keyPlace = KeyPlace(cfg)

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.main_layout)

        debugRecorder = DebugRecorderImpl()

        //eu.printingin3d.javascad.vrl.Polygon.setDebugRecorder(debugRecorder);
        mGLSurfaceView = findViewById<View>(R.id.gl_surface_view) as CustomGLSurfaceView
        debugOverlay = findViewById(R.id.debug_overlay)

        // Инициализация компонентов логирования
        initLoggingComponents()

        // Check if the system supports OpenGL ES 2.0.
        val activityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val configurationInfo = activityManager.deviceConfigurationInfo
        val supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000

        if (supportsEs2) {
            // Request an OpenGL ES 2.0 compatible context.
            mGLSurfaceView!!.setEGLContextClientVersion(2)

            val displayMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(displayMetrics)

            // Set the renderer to our demo renderer, defined below.
            var builder: SceneBuilder = SceneBuilderTest(debugRecorder)
            builder = PcCaseSceneBuilder(debugRecorder)
            //DebugGenerator(debugRecorder).generate()

            mRenderer = MultipleObjectsRenderer(
                this, builder
            ) {
                mGLSurfaceView!!.requestRender()
            }

            debugOverlay.setVisibility(View.VISIBLE)
            mRenderer!!.setDebugPointsRenderer(debugOverlay)

            debugVisualizer = DebugVisualizerImpl().also {
                mRenderer!!.setDebugVisualizer(it)
            }


            mGLSurfaceView!!.setRenderer(mRenderer, displayMetrics.density)
            val wireframeCheckBox = findViewById<CheckBox>(R.id.wire_mode_checkbox)
            wireframeCheckBox.setOnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
                setWireframeOnly(isChecked)
            }

            builder.requestBuffers()
        } else {
            // This is where you could create an OpenGL ES 1.x compatible
            // renderer if you wanted to support both ES 1 and ES 2.
            return
        }
    }

    override fun onResume() {
        // The activity must call the GL surface view's onResume() on activity
        // onResume().
        super.onResume()
        mGLSurfaceView!!.onResume()
    }

    override fun onPause() {
        // The activity must call the GL surface view's onPause() on activity
        // onPause().
        super.onPause()
        mGLSurfaceView!!.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        //eu.printingin3d.javascad.vrl.Polygon.setDebugRecorder(null);
    }

    fun setWireframeOnly(wireframeOnly: Boolean) {
        if (mRenderer != null) {
            mRenderer!!.setWireframeOnly(wireframeOnly)
        }
    }

    /**
     * Инициализирует компоненты для логирования
     */
    private fun initLoggingComponents() {
        // Инициализация RecyclerView
        logsRecyclerView = findViewById(R.id.logs_recycler_view)
        logger = LogListViewLogger(logsRecyclerView)

        // Инициализация кнопок
        toggleLogsButton = findViewById(R.id.toggle_logs_button)
        clearLogsButton = findViewById(R.id.clear_logs_button)
        prevDebugButton = findViewById(R.id.prev_debug_button)
        nextDebugButton = findViewById(R.id.next_debug_button)
        debugInfoText = findViewById(R.id.debug_info_text)

        val prev10Button = findViewById<Button>(R.id.prev_10_debug_button)
        val next10Button = findViewById<Button>(R.id.next_10_debug_button)

        // Настройка обработчиков событий
        toggleLogsButton.setOnClickListener(View.OnClickListener { v: View? -> toggleLogsVisibility() })
        clearLogsButton.setOnClickListener(View.OnClickListener { v: View? -> clearLogs() })
        prevDebugButton.setOnClickListener(View.OnClickListener { previousDebugStep(1) })
        nextDebugButton.setOnClickListener(View.OnClickListener { nextDebugStep(1) })

        prev10Button.setOnClickListener(View.OnClickListener { previousDebugStep(10) })

        next10Button.setOnClickListener(View.OnClickListener { nextDebugStep(10) })

        // Инициализируем состояние кнопок отладки
        updateDebugButtons()
    }

    /**
     * Переключает видимость панели логов
     */
    private fun toggleLogsVisibility() {
        if (isLogsVisible) {
            logsRecyclerView!!.visibility = View.GONE
            isLogsVisible = false
        } else {
            logsRecyclerView!!.visibility = View.VISIBLE
            isLogsVisible = true
        }
    }

    /**
     * Очищает все логи
     */
    private fun clearLogs() {
        logger!!.clear()
    }

    /**
     * Возвращает экземпляр Logger для использования в других компонентах
     */
    fun getLogger(): Logger? {
        return logger
    }

    val debugInfo: String
        /**
         * Возвращает информацию о текущем состоянии отладки
         */
        get() {
            if (debugRecorder == null) {
                return "Debug recorder not initialized"
            }

            val totalCommands = debugRecorder!!.commandCount
            if (totalCommands == 0) {
                return "No debug commands recorded"
            }

            val currentCmd = debugRecorder!!.getCommand(currentDebugIndex)
                ?: return "Invalid debug index: $currentDebugIndex"

            return String.format(
                "Step %d/%d: %s", currentDebugIndex + 1, totalCommands, currentCmd.description
            )
        }

    /**
     * Переход к предыдущему отладочному шагу
     */
    private fun previousDebugStep(delta: Int) {
        currentDebugIndex -= delta
        if (currentDebugIndex < 0) {
            currentDebugIndex = debugRecorder!!.commandCount - 1
        }
        applyDebugStep(currentDebugIndex)
        updateDebugButtons()
    }

    /**
     * Переход к следующему отладочному шагу
     */
    private fun nextDebugStep(delta: Int) {
        currentDebugIndex += delta
        if (currentDebugIndex > debugRecorder!!.commandCount - 1) {
            currentDebugIndex = 0
        }
        applyDebugStep(currentDebugIndex)
        updateDebugButtons()
    }

    /**
     * Переход к первому отладочному шагу
     */
    private fun firstDebugStep() {
        if (debugRecorder!!.commandCount > 0) {
            currentDebugIndex = 0
            applyDebugStep(currentDebugIndex)
            updateDebugButtons()
        }
    }

    /**
     * Очищает визуализацию при сбросе
     */
    private fun clearDebugVisualization() {
        if (debugVisualizer != null) {
            debugVisualizer!!.clearVisualization()
        }
    }

    /**
     * Применяет отладочный шаг по индексу
     */
    private fun applyDebugStep(index: Int) {
        val cmd = debugRecorder!!.getCommand(index)
        if (cmd != null) {
            logger!!.d("Step: " + (index + 1) + "/" + debugRecorder!!.commandCount)
            logger!!.d("Description: " + cmd.description)

            // Применяем визуализацию для текущего шага
            if (debugVisualizer != null) {
                // Очищаем предыдущую визуализацию
                debugVisualizer!!.clearVisualization()

                // Применяем визуализацию команды
                debugVisualizer!!.applyDebugVisualization(cmd)
            }

            // Обновляем информацию на экране
            updateDebugInfo()

            // Запрашиваем перерисовку
            if (mRenderer != null) {
                mRenderer!!.requestRender()
            }
        }
    }

    /**
     * Обновляет состояние кнопок навигации
     */
    private fun updateDebugButtons() {
        //        prevDebugButton.setEnabled(currentDebugIndex > 0);
        //        nextDebugButton.setEnabled(currentDebugIndex < debugRecorder.getCommandCount()
        //        - 1);

        if (debugRecorder!!.commandCount > 0) {
            prevDebugButton!!.text = String.format(
                "%s %d",
                getString(R.string.prev_debug),
                currentDebugIndex - 1,
                debugRecorder!!.commandCount
            )
            nextDebugButton!!.text = String.format(
                "%s %d",
                getString(R.string.next_debug),
                currentDebugIndex + 1,
                debugRecorder!!.commandCount
            )
        } else {
            prevDebugButton!!.text = getString(R.string.prev_debug)
            nextDebugButton!!.text = getString(R.string.next_debug)
        }

        updateDebugInfo()
    }

    /**
     * Обновляет отображаемую отладочную информацию
     */
    private fun updateDebugInfo() {
        if (debugInfoText != null && isLogsVisible) {
            val info = debugInfo
            debugInfoText!!.text = info
        }
    }

    companion object {

        private const val LOW_PROFILE_KEYCAP_HEIGHT = 4.5
        private const val STANDART_KEYCAP_HEIGHT = 12.7
    }
}
