package com.github.grishberg.cad3d.pccase

import eu.printingin3d.javascad.coords.Angles3d
import eu.printingin3d.javascad.tranzitions.Union
import eu.printingin3d.javascad.vrl.ColorFacetGenerationContext
import eu.printingin3d.javascad.vrl.FacetGenerationContext
import eu.printingin3d.javascad.vrl.CSG
import com.github.grishberg.javascad.StlExporter
import java.io.File

fun main(args: Array<String>) {
    val renderMode = args.contains("--render")
    val guiMode = !renderMode

    if (guiMode) println("=== PC Case Viewer ===")
    else println("=== PC Case Render (headless) ===")

    val outDir = File("stl_pccase")
    if (!outDir.exists()) outDir.mkdirs()

    val defaultContext: FacetGenerationContext = ColorFacetGenerationContext(eu.printingin3d.javascad.utils.Color.GRAY)
    defaultContext.setFn(8)

    val frameVertContext: FacetGenerationContext = ColorFacetGenerationContext(eu.printingin3d.javascad.utils.Color(100, 140, 200))
    frameVertContext.setFn(8)

    val mbContext: FacetGenerationContext = ColorFacetGenerationContext(eu.printingin3d.javascad.utils.Color.GREEN)
    mbContext.setFn(8)

    val gpuContext: FacetGenerationContext = ColorFacetGenerationContext(eu.printingin3d.javascad.utils.Color(200, 30, 30))
    gpuContext.setFn(8)

    val psuContext: FacetGenerationContext = ColorFacetGenerationContext(eu.printingin3d.javascad.utils.Color(60, 60, 60))
    psuContext.setFn(8)

    val coolerContext: FacetGenerationContext = ColorFacetGenerationContext(eu.printingin3d.javascad.utils.Color(180, 180, 180))
    coolerContext.setFn(8)

    AluminumProfile.reset()

    println("\nBuilding PC frame (aluminum profiles 20x20mm)...")
    val frameCfg = PcFrame(
        width = 530.0, height = 350.0, depth = 330.0
    )
    val frameVertical = frameCfg.buildVertical()
    val frameHorizontal = frameCfg.buildHorizontal()

    val p = AluminumProfile.PROFILE_SIZE
    val bottomY = p / 2 + p / 2

    println("\nBuilding motherboard (Tyan S8030)...")
    val mb = Motherboard().build()
        .move(-40.0, bottomY + 1.6 / 2, 0.0)

   println("\nBuilding GPU (Gigabyte RTX 3090 Turbo)...")
     val gpu = Gpu().build()
         .move(-50.0, 71.0, 150.0)

    println("\nBuilding PSUs (ATX)...")
    val hd = 165.0
    val psuY = p / 2 + 150.0 / 2
    val psuBack = Psu().build()
        .rotate(Angles3d.zOnly(90.0))
        .move(175.0, psuY, hd - 70.0)
    val psuFront = Psu().build()
        .rotate(Angles3d.zOnly(90.0))
        .move(175.0, psuY, -hd + 70.0)

    println("\nBuilding CPU cooler (ARCTIC Freezer 4U-M)...")
    val cooler = Cooler().build()
        .rotate(Angles3d.zOnly(90.0))
        .move(50.0, bottomY + 1.6 + 80.0, -20.0)

    val frameVertCsg = frameVertical.toCSG(frameVertContext)
    val frameHorizCsg = frameHorizontal.toCSG(defaultContext)
    val frameCsg = Union(listOf(frameVertical, frameHorizontal)).toCSG(defaultContext)
    val mbCsg = mb.toCSG(mbContext)
    val gpuCsg = gpu.toCSG(gpuContext)
    val psuBackCsg = psuBack.toCSG(psuContext)
    val psuFrontCsg = psuFront.toCSG(psuContext)
    val coolerCsg = cooler.toCSG(coolerContext)

    val report = AluminumProfile.generateReport()
    println(report)
    File(outDir, "profile_report.txt").writeText(report)

    val sceneModels = mapOf(
        "frame_vertical" to frameVertCsg,
        "frame_horizontal" to frameHorizCsg,
        "motherboard" to mbCsg,
        "gpu" to gpuCsg,
        "psu_back" to psuBackCsg,
        "psu_front" to psuFrontCsg,
        "cooler" to coolerCsg
    )

    if (renderMode) {
        println("\nRendering scene...")
        val renderer = SceneRenderer(cameraAngleX = 245.0)
        renderer.renderScene(sceneModels.entries.map { it.key to it.value }, File(outDir, "scene.png"))
    } else if (guiMode) {
        println("\nStarting interactive viewer...")
        PcCaseViewer().show(sceneModels)
    } else {
        exportStl(frameCsg, outDir, "frame.stl")
        exportStl(mbCsg, outDir, "motherboard.stl")
        exportStl(gpuCsg, outDir, "gpu_rtx3090.stl")
        exportStl(psuBackCsg, outDir, "psu_back.stl")
        exportStl(psuFrontCsg, outDir, "psu_front.stl")
        exportStl(coolerCsg, outDir, "cooler.stl")

        println("\n=== All models generated ===")
        println("Output directory: ${outDir.absolutePath}")
        outDir.listFiles()?.filter { it.extension == "stl" }?.sortedBy { it.name }?.forEach { f ->
            val sizeMB = f.length() / 1024.0 / 1024.0
            println("  ${f.name} (${String.format("%.2f", sizeMB)} MB)")
        }
    }
}

private fun exportStl(csg: CSG, outDir: File, fileName: String) {
    val targetPath = File(outDir, fileName).absolutePath
    try {
        println("  Exporting $fileName...")
        StlExporter.saveStl(csg.polygons, targetPath)
        val size = File(targetPath).length()
        println("  [OK] $fileName (${size} bytes)")
    } catch (e: Exception) {
        println("  [FAIL] $fileName: ${e.message}")
    }
}

