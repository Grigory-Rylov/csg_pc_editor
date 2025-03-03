package org.example.dialog

import com.github.grishberg.cad3d.keyboard.cfg.KeyboardSettings
import com.github.grishberg.cad3d.keyboard.cfg.SettingsContainer
import com.github.grishberg.cad3d.keyboard.cfg.ThumbClusterSettings
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
    private val initialSettingsContainer: SettingsContainer,
    private val onKeyboardSettingsChanged: (KeyboardSettings) -> Unit,
    private val onThumbClusterSettingsChanged: (ThumbClusterSettings) -> Unit,
) : JDialog() {

    private var currentKeyboardSettings = initialSettingsContainer.keyboardSettings
    private var currentThumbClusterSettings = initialSettingsContainer.thumbClusterSettings

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

        addIntSpinner(panel, "Рядов:", currentKeyboardSettings.rowsCount, 1..6) {
            currentKeyboardSettings = currentKeyboardSettings.copy(rowsCount = it)
            fireKeyboardSettingsChanges()
        }

        addIntSpinner(panel, "Колонок:", currentKeyboardSettings.columnsCount, 1..10) {
            currentKeyboardSettings = currentKeyboardSettings.copy(columnsCount = it)
            fireKeyboardSettingsChanges()
        }

        add(panel)
    }

    private fun createAnglesPanel() {
        val panel = JPanel().apply {
            border = BorderFactory.createTitledBorder("Углы")
            layout = GridLayout(0, 2)
        }

        addDoubleSpinner(panel, "Кривизна рядов:", currentKeyboardSettings.rowCurvature, 0.0..50.0, 0.5) {
            currentKeyboardSettings = currentKeyboardSettings.copy(rowCurvature = it)
            fireKeyboardSettingsChanges()
        }

        addDoubleSpinner(panel, "Кривизна колонок:", currentKeyboardSettings.columnCurvature, 0.0..50.0, 0.5) {
            currentKeyboardSettings = currentKeyboardSettings.copy(columnCurvature = it)
            fireKeyboardSettingsChanges()
        }

        addDoubleSpinner(panel, "Угол наклона:", currentKeyboardSettings.tentingAngle, 0.0..90.0, 1.0) {
            currentKeyboardSettings = currentKeyboardSettings.copy(tentingAngle = it)
            fireKeyboardSettingsChanges()
        }

        add(panel)
    }

    private fun createDimensionsPanel() {
        val panel = JPanel().apply {
            border = BorderFactory.createTitledBorder("Размеры и отступы")
            layout = GridLayout(0, 2)
        }

        addDoubleSpinner(panel, "Толщина пластины:", currentKeyboardSettings.plateThickness, 0.0..10.0, 0.1) {
            currentKeyboardSettings = currentKeyboardSettings.copy(plateThickness = it)
            fireKeyboardSettingsChanges()
        }

        addDoubleSpinner(panel, "Z-смещение пластины:", currentKeyboardSettings.plateZOffset, 0.0..50.0, 0.1) {
            currentKeyboardSettings = currentKeyboardSettings.copy(plateZOffset = it)
            fireKeyboardSettingsChanges()
        }

        addDoubleSpinner(panel, "Высота профиля SA:", currentKeyboardSettings.saProfileKeyHeight, 0.0..20.0, 0.1) {
            currentKeyboardSettings = currentKeyboardSettings.copy(saProfileKeyHeight = it)
            fireKeyboardSettingsChanges()
        }

        addDoubleSpinner(panel, "Смещение границ:", currentKeyboardSettings.bordersOffset, 0.0..50.0, 0.5) {
            currentKeyboardSettings = currentKeyboardSettings.copy(bordersOffset = it)
            fireKeyboardSettingsChanges()
        }

        addDoubleSpinner(panel, "Диаметр закладной гайки, мм:", currentKeyboardSettings.screwNutHoleDiameter, 2.0..10.0, 0.1) {
            currentKeyboardSettings = currentKeyboardSettings.copy(screwNutHoleDiameter = it)
            fireKeyboardSettingsChanges()
        }

        add(panel)
    }

    private fun createAdditionalConfigPanel() {
        val panel = JPanel().apply {
            border = BorderFactory.createTitledBorder("Дополнительная конфигурация")
            layout = GridLayout(0, 2)
        }

        addCheckbox(panel, "Низкий профиль:", currentKeyboardSettings.isLowProfile) {
            currentKeyboardSettings = currentKeyboardSettings.copy(isLowProfile = it)
            fireKeyboardSettingsChanges()
        }

        addCheckbox(panel, "Хотсвоп:", currentKeyboardSettings.isHasHotswap) {
            currentKeyboardSettings = currentKeyboardSettings.copy(isHasHotswap = it)
            fireKeyboardSettingsChanges()
        }

        addCheckbox(panel, "Магнитная площадка:", currentKeyboardSettings.isMagneticWristRestHolder) {
            currentKeyboardSettings = currentKeyboardSettings.copy(isMagneticWristRestHolder = it)
            fireKeyboardSettingsChanges()
        }

        addIntSpinner(panel, "FN клавиша:", currentKeyboardSettings.fn, 4..100) {
            currentKeyboardSettings = currentKeyboardSettings.copy(fn = it)
            fireKeyboardSettingsChanges()
        }

        add(panel)
    }

    private fun createThumbClusterConfigPanel() {
        val panel = JPanel().apply {
            border = BorderFactory.createTitledBorder("Кластер большого пальца")
            layout = GridLayout(0, 2)
        }

        addDoubleSpinner(panel, "Смещение по оси X:", currentThumbClusterSettings.xOffset, -100.0..100.0) {
            currentThumbClusterSettings = currentThumbClusterSettings.copy(xOffset = it)
            fireThumnClusterSettingsChanges()
        }

        addDoubleSpinner(panel, "Смещение по оси Y:", currentThumbClusterSettings.yOffset, -100.0..100.0) {
            currentThumbClusterSettings = currentThumbClusterSettings.copy(yOffset = it)
            fireThumnClusterSettingsChanges()
        }

        addDoubleSpinner(panel, "Смещение по оси Z:", currentThumbClusterSettings.zOffset, 0.0..100.0) {
            currentThumbClusterSettings = currentThumbClusterSettings.copy(zOffset = it)
            fireThumnClusterSettingsChanges()
        }


        addDoubleSpinner(panel, "Поворот вокруг оси Y:", currentThumbClusterSettings.rotateY, -180.0..180.0) {
            currentThumbClusterSettings = currentThumbClusterSettings.copy(rotateY = it)
            fireThumnClusterSettingsChanges()
        }

        addDoubleSpinner(panel, "Поворот вокруг оси Z:", currentThumbClusterSettings.rotateZ, -180.0..180.0) {
            currentThumbClusterSettings = currentThumbClusterSettings.copy(rotateZ = it)
            fireThumnClusterSettingsChanges()
        }

        addDoubleSpinner(
            panel, "Радиус дуги вокруг оси Z", currentThumbClusterSettings.arcRadiusZ, -1000.0..1000.0
        ) {
            currentThumbClusterSettings = currentThumbClusterSettings.copy(arcRadiusZ = it)
            fireThumnClusterSettingsChanges()
        }

        addDoubleSpinner(
            panel, "Радиус дуги вокруг оси Y", currentThumbClusterSettings.arcRadiusY, -1000.0..1000.0
        ) {
            currentThumbClusterSettings = currentThumbClusterSettings.copy(arcRadiusY = it)
            fireThumnClusterSettingsChanges()
        }

        /*
        addDoubleSpinner(panel, "Поворот вокруг оси X:", currentThumbClusterSettings.r, 0..100) {
            currentThumbClusterSettings = currentThumbClusterSettings.copy(fn = it)
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

    private fun fireKeyboardSettingsChanges() {
        onKeyboardSettingsChanged(currentKeyboardSettings)
    }

    private fun fireThumnClusterSettingsChanges() {
        onThumbClusterSettingsChanged(currentThumbClusterSettings)
    }
}
