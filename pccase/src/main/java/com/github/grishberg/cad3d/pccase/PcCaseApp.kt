package com.github.grishberg.cad3d.pccase

import eu.printingin3d.javascad.tranzitions.Union
import eu.printingin3d.javascad.vrl.ColorFacetGenerationContext
import eu.printingin3d.javascad.vrl.FacetGenerationContext
import eu.printingin3d.javascad.vrl.CSG
import com.github.grishberg.javascad.StlExporter
import java.io.File

fun main(args: Array<String>) {
    println("=== PC Case STL Generator ===")

    val renderMode = args.contains("--render")

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

    AluminumProfile.reset()

    println("\nBuilding PC frame (aluminum profiles 20x20mm)...")
    val frameCfg = PcFrame(
        width = 530.0, height = 350.0, depth = 330.0
    )
    val frameVertical = frameCfg.buildVertical()
    val frameHorizontal = frameCfg.buildHorizontal()

    println("\nBuilding motherboard (Supermicro H12SSL-i)...")
    val mb = Motherboard().build()

    println("\nBuilding GPU (RTX 3090)...")
    val gpu = Gpu().build()

    println("\nBuilding PSU...")
    val psu = Psu().build()

    val frameVertCsg = frameVertical.toCSG(frameVertContext)
    val frameHorizCsg = frameHorizontal.toCSG(defaultContext)
    val frameCsg = Union(listOf(frameVertical, frameHorizontal)).toCSG(defaultContext)
    val mbCsg = mb.toCSG(mbContext)
    val gpuCsg = gpu.toCSG(gpuContext)
    val psuCsg = psu.toCSG(psuContext)

    val report = AluminumProfile.generateReport()
    println(report)
    File(outDir, "profile_report.txt").writeText(report)

    if (renderMode) {
        println("\nRendering scene...")
        val renderer = SceneRenderer(cameraAngleX = 245.0)
        renderer.renderScene(
            listOf(
                "frame_vertical" to frameVertCsg,
                "frame_horizontal" to frameHorizCsg,
                "motherboard" to mbCsg,
                "gpu" to gpuCsg,
                "psu" to psuCsg
            ),
            File(outDir, "scene.png")
        )
    } else {
        exportStl(frameCsg, outDir, "frame.stl")
        exportStl(mbCsg, outDir, "motherboard.stl")
        exportStl(gpuCsg, outDir, "gpu_rtx3090.stl")
        exportStl(psuCsg, outDir, "psu.stl")

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

