package com.github.grishberg.cad3d.cli

import com.github.grishberg.cad3d.pccase.PcCaseModelFactory
import com.github.grishberg.javascad.StlExporter
import java.io.File

fun main(args: Array<String>) {
    val renderMode = args.contains("--render")
    val guiMode = !renderMode

    if (guiMode) println("=== PC Case Viewer ===")
    else println("=== PC Case Render (headless) ===")

    val outDir = File("stl_pccase")
    if (!outDir.exists()) outDir.mkdirs()

    println("\nBuilding all models...")
    val sceneModels = PcCaseModelFactory.buildAll()

    if (renderMode) {
        println("\nRendering scene...")
        val renderer = SceneRenderer(cameraAngleX = 35.0, cameraAngleY = 25.0)
        renderer.renderScene(sceneModels.entries.map { it.key to it.value }, File(outDir, "scene.png"))
    } else if (guiMode) {
        println("\nStarting interactive viewer...")
        PcCaseViewer().show(sceneModels)
    } else {
        sceneModels.forEach { (name, csg) ->
            exportStl(csg, outDir, "$name.stl")
        }

        println("\n=== All models generated ===")
        println("Output directory: ${outDir.absolutePath}")
        outDir.listFiles()?.filter { it.extension == "stl" }?.sortedBy { it.name }?.forEach { f ->
            val sizeMB = f.length() / 1024.0 / 1024.0
            println("  ${f.name} (${String.format("%.2f", sizeMB)} MB)")
        }
    }
}

private fun exportStl(csg: eu.printingin3d.javascad.vrl.CSG, outDir: File, fileName: String) {
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
