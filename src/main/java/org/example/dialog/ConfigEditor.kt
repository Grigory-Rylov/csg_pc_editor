package org.example.dialog

import com.github.grishberg.cad3d.keyboard.cfg.KeyboardConfig
import java.awt.GridLayout
import javax.swing.BorderFactory
import javax.swing.BoxLayout
import javax.swing.JCheckBox
import javax.swing.JDialog
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JSpinner
import javax.swing.SpinnerNumberModel

class ConfigEditor(
    private val initialConfig: KeyboardConfig, private val onConfigChanged: (KeyboardConfig) -> Unit
) : JDialog() {

    private var currentConfig = initialConfig

    init {
        title = "Конфигурация клавиатуры"
        isModal = false
        layout = BoxLayout(contentPane, BoxLayout.Y_AXIS)
        defaultCloseOperation = JDialog.DISPOSE_ON_CLOSE

        createKeyCountPanel()
        createAnglesPanel()
        createDimensionsPanel()
        createAdditionalConfigPanel()
        createThumbClusterConfigPanel()

        pack()
        setLocationRelativeTo(null)
    }

    private fun createKeyCountPanel() {
        val panel = JPanel().apply {
            border = BorderFactory.createTitledBorder("Количество клавиш")
            layout = GridLayout(0, 2)
        }

        addIntSpinner(panel, "Рядов:", currentConfig.rowsCount, 1..6) {
            currentConfig = currentConfig.copy(rowsCount = it)
            fireChanges()
        }

        addIntSpinner(panel, "Колонок:", currentConfig.columnsCount, 1..10) {
            currentConfig = currentConfig.copy(columnsCount = it)
            fireChanges()
        }

        add(panel)
    }

    private fun createAnglesPanel() {
        val panel = JPanel().apply {
            border = BorderFactory.createTitledBorder("Углы")
            layout = GridLayout(0, 2)
        }

        addDoubleSpinner(panel, "Кривизна рядов:", currentConfig.rowCurvature, 0.0..50.0, 0.5) {
            currentConfig = currentConfig.copy(rowCurvature = it)
            fireChanges()
        }

        addDoubleSpinner(panel, "Кривизна колонок:", currentConfig.columnCurvature, 0.0..50.0, 0.5) {
            currentConfig = currentConfig.copy(columnCurvature = it)
            fireChanges()
        }

        addDoubleSpinner(panel, "Угол наклона:", currentConfig.tentingAngle, 0.0..90.0, 1.0) {
            currentConfig = currentConfig.copy(tentingAngle = it)
            fireChanges()
        }

        add(panel)
    }

    private fun createDimensionsPanel() {
        val panel = JPanel().apply {
            border = BorderFactory.createTitledBorder("Размеры и отступы")
            layout = GridLayout(0, 2)
        }

        addDoubleSpinner(panel, "Толщина пластины:", currentConfig.plateThickness, 0.0..10.0, 0.1) {
            currentConfig = currentConfig.copy(plateThickness = it)
            fireChanges()
        }

        addDoubleSpinner(panel, "Z-смещение пластины:", currentConfig.plateZOffset, 0.0..50.0, 0.1) {
            currentConfig = currentConfig.copy(plateZOffset = it)
            fireChanges()
        }

        addDoubleSpinner(panel, "Высота профиля SA:", currentConfig.saProfileKeyHeight, 0.0..20.0, 0.1) {
            currentConfig = currentConfig.copy(saProfileKeyHeight = it)
            fireChanges()
        }

        addDoubleSpinner(panel, "Смещение границ:", currentConfig.bordersOffset, 0.0..50.0, 0.5) {
            currentConfig = currentConfig.copy(bordersOffset = it)
            fireChanges()
        }

        add(panel)
    }

    private fun createAdditionalConfigPanel() {
        val panel = JPanel().apply {
            border = BorderFactory.createTitledBorder("Дополнительная конфигурация")
            layout = GridLayout(0, 2)
        }

        addCheckbox(panel, "Низкий профиль:", currentConfig.isLowProfile) {
            currentConfig = currentConfig.copy(isLowProfile = it)
            fireChanges()
        }

        addCheckbox(panel, "Хотсвоп:", currentConfig.isHasHotswap) {
            currentConfig = currentConfig.copy(isHasHotswap = it)
            fireChanges()
        }

        addCheckbox(panel, "Магнитная площадка:", currentConfig.isMagneticWristRestHolder) {
            currentConfig = currentConfig.copy(isMagneticWristRestHolder = it)
            fireChanges()
        }

        addIntSpinner(panel, "FN клавиша:", currentConfig.fn, 4..100) {
            currentConfig = currentConfig.copy(fn = it)
            fireChanges()
        }

        add(panel)
    }

    private fun createThumbClusterConfigPanel() {
        val panel = JPanel().apply {
            border = BorderFactory.createTitledBorder("Кластер большого пальца")
            layout = GridLayout(0, 2)
        }

        addDoubleSpinner(panel, "Смещение по оси X:", currentConfig.thumbClusterSettings.xOffset, 0.0..100.0) {
            val thumbClusterSettings = currentConfig.thumbClusterSettings
            currentConfig = currentConfig.copy(thumbClusterSettings = thumbClusterSettings.copy(xOffset = it))
            fireChanges()
        }

        addDoubleSpinner(panel, "Смещение по оси Y:", currentConfig.thumbClusterSettings.yOffset, -100.0..100.0) {
            val thumbClusterSettings = currentConfig.thumbClusterSettings
            currentConfig = currentConfig.copy(thumbClusterSettings = thumbClusterSettings.copy(yOffset = it))
            fireChanges()
        }

        addDoubleSpinner(panel, "Смещение по оси Z:", currentConfig.thumbClusterSettings.zOffset, 0.0..100.0) {
            val thumbClusterSettings = currentConfig.thumbClusterSettings
            currentConfig = currentConfig.copy(thumbClusterSettings = thumbClusterSettings.copy(zOffset = it))
            fireChanges()
        }


        addDoubleSpinner(panel, "Поворот вокруг оси Y:", currentConfig.thumbClusterSettings.rotateY, 0.0..100.0) {
            val thumbClusterSettings = currentConfig.thumbClusterSettings
            currentConfig = currentConfig.copy(thumbClusterSettings = thumbClusterSettings.copy(rotateY = it))
            fireChanges()
        }

        addDoubleSpinner(panel, "Поворот вокруг оси Z:", currentConfig.thumbClusterSettings.rotateZ, 0.0..100.0) {
            val thumbClusterSettings = currentConfig.thumbClusterSettings
            currentConfig = currentConfig.copy(thumbClusterSettings = thumbClusterSettings.copy(rotateZ = it))
            fireChanges()
        }

        /*
        addDoubleSpinner(panel, "Поворот вокруг оси X:", currentConfig.thumbClusterSettings.r, 0..100) {
            currentConfig = currentConfig.copy(fn = it)
            fireChanges()
        }
        */
        add(panel)
    }

    private fun addIntSpinner(
        panel: JPanel, label: String, value: Int, range: IntRange, callback: (Int) -> Unit
    ) {
        panel.add(JLabel(label))
        val spinner = JSpinner(SpinnerNumberModel(value, range.first, range.last, 1)).apply {
            addChangeListener { callback(value as Int) }
        }
        panel.add(spinner)
    }

    private fun addDoubleSpinner(
        panel: JPanel,
        label: String,
        initialValue: Double,
        range: ClosedFloatingPointRange<Double>,
        step: Double = 0.1,
        callback: (Double) -> Unit
    ) {
        panel.add(JLabel(label))
        val spinner = JSpinner(SpinnerNumberModel(initialValue, range.start, range.endInclusive, step)).apply {
            addChangeListener { event ->
                callback(value as Double)
            }
        }
        panel.add(spinner)
    }

    private fun addCheckbox(
        panel: JPanel, label: String, checked: Boolean, callback: (Boolean) -> Unit
    ) {
        panel.add(JLabel(label))
        val checkbox = JCheckBox().apply {
            isSelected = checked
            addActionListener { callback(isSelected) }
        }
        panel.add(checkbox)
    }

    private fun fireChanges() {
        onConfigChanged(currentConfig)
    }
}
