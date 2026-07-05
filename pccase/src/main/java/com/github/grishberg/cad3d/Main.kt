package com.github.grishberg.cad3d

import com.github.grishberg.cad3d.pccase.AluminumProfile
import com.github.grishberg.cad3d.pccase.Gpu
import com.github.grishberg.cad3d.pccase.Motherboard
import com.github.grishberg.cad3d.pccase.PcCaseViewer
import com.github.grishberg.cad3d.pccase.PcFrame
import com.github.grishberg.cad3d.pccase.Psu
import com.github.grishberg.cad3d.pccase.SceneRenderer
import eu.printingin3d.javascad.utils.Color
import eu.printingin3d.javascad.vrl.ColorFacetGenerationContext
import eu.printingin3d.javascad.vrl.FacetGenerationContext
import eu.printingin3d.javascad.vrl.CSG
import java.io.File

fun main(args: Array<String>) {
    val renderMode = args.contains("--render")
    val guiMode = !renderMode

    if (guiMode) println("=== PC Case Viewer ===")
    else println("=== PC Case Render (headless) ===")

    val outDir = File("stl_pccase")
    if (!outDir.exists()) outDir.mkdirs()

    val defaultContext: FacetGenerationContext = ColorFacetGenerationContext(Color.GRAY).apply { setFn(8) }
    val frameVertContext: FacetGenerationContext = ColorFacetGenerationContext(Color(100, 140, 200)).apply { setFn(8) }
    val mbContext: FacetGenerationContext = ColorFacetGenerationContext(Color.GREEN).apply { setFn(8) }
    val gpuContext: FacetGenerationContext = ColorFacetGenerationContext(Color(200, 30, 30)).apply { setFn(8) }
    val psuContext: FacetGenerationContext = ColorFacetGenerationContext(Color(60, 60, 60)).apply { setFn(8) }

    AluminumProfile.reset()

    println("\nBuilding PC frame...")
    val frameCfg = PcFrame(width = 530.0, height = 350.0, depth = 330.0)
    val frameVertical = frameCfg.buildVertical()
    val frameHorizontal = frameCfg.buildHorizontal()

    val p = AluminumProfile.PROFILE_SIZE
    val bottomY = p / 2 + p / 2

    println("Building motherboard (Tyan S8030)...")
    val mb = Motherboard().build().move(-40.0, bottomY + 1.6 / 2, 0.0)

    println("Building GPU (Gigabyte RTX 3090 Turbo)...")
    val gpu = Gpu().build().move(-50.0, bottomY + 1.6 + 112.0 / 2, -65.0)

    println("Building PSU (ATX)...")
    val psu = Psu().build().move(175.0, bottomY + 86.0 / 2, 0.0)

    val frameVertCsg = frameVertical.toCSG(frameVertContext)
    val frameHorizCsg = frameHorizontal.toCSG(defaultContext)
    val mbCsg = mb.toCSG(mbContext)
    val gpuCsg = gpu.toCSG(gpuContext)
    val psuCsg = psu.toCSG(psuContext)

    val report = AluminumProfile.generateReport()
    println(report)
    File(outDir, "profile_report.txt").writeText(report)

    val models = mapOf(
        "frame_vertical" to frameVertCsg,
        "frame_horizontal" to frameHorizCsg,
        "motherboard" to mbCsg,
        "gpu" to gpuCsg,
        "psu" to psuCsg
    )

    if (renderMode) {
        println("\nRendering scene...")
        SceneRenderer(cameraAngleX = 245.0).renderScene(
            models.entries.map { it.key to it.value },
            File(outDir, "scene.png")
        )
    } else {
        println("\nStarting interactive viewer...")
        PcCaseViewer().show(models)
    }
}
