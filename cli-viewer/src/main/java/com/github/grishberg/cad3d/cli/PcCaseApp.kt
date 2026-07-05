package com.github.grishberg.cad3d.cli

import com.github.grishberg.cad3d.pccase.PcCaseModelFactory
import com.github.grishberg.javascad.StlExporter
import java.io.File
import javax.imageio.ImageIO

fun main(args: Array<String>) {
    val renderMode = args.contains("--render")
    val guiMode = !renderMode

    if (guiMode) println("=== PC Case Viewer ===")
    else println("=== PC Case Render (headless) ===")

    val outDir = File("stl_pccase")
    if (!outDir.exists()) outDir.mkdirs()

    println("\nBuilding all models...")
    val sceneModels = PcCaseModelFactory.buildAll()

    val textureOverlays = loadMotherboardTexture(sceneModels)
    println("  Texture overlays: ${textureOverlays.size} (${textureOverlays.values.sumOf { it.polygons.size }} polygons)")

    if (renderMode) {
        println("\nRendering scene...")
        val renderer = SceneRenderer(
            cameraAngleX = 155.0, cameraAngleY = 35.0,
            textureOverlays = textureOverlays
        )
        renderer.renderScene(sceneModels.entries.map { it.key to it.value }, File(outDir, "scene.png"))
    } else if (guiMode) {
        println("\nStarting interactive viewer...")
        PcCaseViewer(textureOverlays = textureOverlays).show(sceneModels)
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

private fun loadMotherboardTexture(
    sceneModels: Map<String, eu.printingin3d.javascad.vrl.CSG>
): Map<String, TextureOverlay> {
    val mbCsg = sceneModels["motherboard"] ?: return emptyMap()
    val overlays = mutableMapOf<String, TextureOverlay>()

    // Top texture
    val topFile = listOf(File("motherboard.png"), File("../motherboard.png"))
        .firstOrNull { it.exists() }
    if (topFile != null) {
        val image = ImageIO.read(topFile)
        println("  Loaded top texture: ${topFile.name} (${image.width}x${image.height})")
        val mbTopZ = PcCaseModelFactory.MB_OFFSET_Z
        val topPolygons = mbCsg.polygons.filter { polygon ->
            polygon.normal.z > 0.99 &&
                (polygon.vertices.firstOrNull()?.z ?: 0.0) > mbTopZ - 0.1
        }
        println("  Top face polygons: ${topPolygons.size}")
        if (topPolygons.isNotEmpty()) {
            overlays["motherboard_top"] = TextureOverlay(image, topPolygons)
        }
    }

    // Bottom texture
    val bottomFile = listOf(File("motherboard_down.png"), File("../motherboard_down.png"))
        .firstOrNull { it.exists() }
    if (bottomFile != null) {
        val image = ImageIO.read(bottomFile)
        println("  Loaded bottom texture: ${bottomFile.name} (${image.width}x${image.height})")
        val mbBottomZ = PcCaseModelFactory.MB_OFFSET_Z - 1.6
        val bottomPolygons = mbCsg.polygons.filter { polygon ->
            polygon.normal.z < -0.99 &&
                (polygon.vertices.firstOrNull()?.z ?: 0.0) < mbBottomZ + 0.1
        }
        println("  Bottom face polygons: ${bottomPolygons.size}")
        if (bottomPolygons.isNotEmpty()) {
            overlays["motherboard_bottom"] = TextureOverlay(image, bottomPolygons)
        }
    }

    return overlays
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
