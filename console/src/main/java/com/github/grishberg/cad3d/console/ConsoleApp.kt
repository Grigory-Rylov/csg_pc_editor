package com.github.grishberg.cad3d.console

import com.github.grishberg.cad3d.KeyboardBuilderPlugin
import com.github.grishberg.cad3d.plugin.StlExportListener
import com.github.grishberg.cad3d.plugin.cfg.*
import java.util.concurrent.CountDownLatch
import kotlin.system.exitProcess

fun main() {
    println("=== STL Export Console App ===")

    val settings = createDefaultSettings()

    val latch = CountDownLatch(1)
    var successCount = 0
    var failCount = 0
    val synchronized = Any()

    val listener = object : StlExportListener {
        override fun onExportPlan(fileNames: List<String>) {
            println("\n[PLAN] Will export ${fileNames.size} files:")
            fileNames.forEachIndexed { i, name -> println("  ${(i + 1)}. $name") }
        }

        override fun onExportStart(fileName: String) {
            println("[START] Exporting: $fileName")
        }

        override fun onExportFinish(fileName: String, success: Boolean, errorMessage: String?) {
            synchronized(synchronized) {
                if (success) {
                    successCount++
                    println("[OK    ] $fileName (${java.io.File("stl/$fileName").length()} bytes)")
                } else {
                    failCount++
                    println("[FAIL  ] $fileName - ${errorMessage ?: "unknown error"}")
                }
            }
        }

        override fun onAllFinished() {
            latch.countDown()
        }
    }

    val plugin = KeyboardBuilderPlugin()
    try {
        println("\nStarting STL export...")
        plugin.exportStl(settings, listener)

        // Wait for completion with timeout (10 minutes max)
        if (!latch.await(600, java.util.concurrent.TimeUnit.SECONDS)) {
            println("[ERROR] Export timed out after 10 minutes!")
            exitProcess(1)
        }

        println("\n=== Export Complete ===")
        println("Success: $successCount | Failed: $failCount")

        val stlDir = java.io.File("stl")
        if (stlDir.exists()) {
            println("\nGenerated STL files:")
            stlDir.listFiles()?.filter { it.extension.equals("stl", ignoreCase = true) }?.sortedBy { it.name }?.forEach { f ->
                val sizeMB = f.length() / 1024.0 / 1024.0
                println("  ${f.name} (${String.format("%.2f", sizeMB)} MB)")
            }
        }

        exitProcess(if (failCount == 0) 0 else 1)
    } finally {
        plugin.onUnload()
    }
}

private fun createDefaultSettings(): SettingsContainer {
    return SettingsContainer(
        assemblySettings = AssemblySettings(
            settingsShowCaps = true,
            settingsShowCase = true,
            settingsShowMatrix = true,
            settingsShowPlate = true,
            settingsShowWristRest = false,
            settingsTrackball = false,
            showController = true,
            showTrackballSensor = false,
            showTrackbalSensorCap = false,
            showTrackballBall = false,
            showTrackballCase = false,
            showTrackballCasePlate = false,
            showControllerHolder = false,
            showAmoeba = false,
        ),
        viewerSettings = ViewerSettings(0f, 0f, 0f, 0f, 0f, -200f),
        keyboardSettings = KeyboardSettings(
            fn = 15,
            stlFn = 8,
            plateZOffset = 0.0,
            rowCurvature = 360.0,
            tentingAngle = 4.0,
            columnCurvature = 2700.0,
            plateThickness = 1.5,
            saProfileKeyHeight = 17.5,
            columnsCount = 6,
            rowsCount = 5,
            centerRow = 3,
            centerCol = 4,
            isLowProfile = false,
            powerSwitcherType = PowerSwitcherType.None,
            isHasHotswap = true,
            isMagneticWristRestHolder = false,
            bordersOffset = 0.5,
            screwNutHoleDiameter = 4.0,
            screwHolderWallhickness = 1.6,
            isSkeletonMode = false,
            keyPlaceholderType = KeyPlaceholderType.None,
            horizontalExtraSpace = 1.0,
            verticalExtraSpace = 1.0,
            controllerType = ControllerType.SuperMiniNRF52840,
            batteryType = BatteryType.None,
        ),
        thumbClusterSettings = ThumbClusterSettings(
            xOffset = -10.0,
            yOffset = -50.0,
            zOffset = 37.0,
            rotateY = -30.0,
            rotateZ = 10.0,
            arcRadiusZ = 0.0,
            arcRadiusY = 0.0,
            spaceBetweenKey = 6.5,
            type = ThumbClusterMode.SingleColumn3Buttons,
        ),
        trackballSettings = TrackballConfig(
            mode = TrackballMode.None,
            ballDiameter = 42.0,
            bearingDiameter = 18.0,
            controllerScrewDiameter = 3.5,
        )
    )
}
