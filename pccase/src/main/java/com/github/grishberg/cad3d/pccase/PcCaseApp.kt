package com.github.grishberg.cad3d.pccase

import eu.printingin3d.javascad.coords.Angles3d
import eu.printingin3d.javascad.coords.V3d
import eu.printingin3d.javascad.models.Abstract3dModel
import eu.printingin3d.javascad.models.Cube
import eu.printingin3d.javascad.tranzitions.Union
import eu.printingin3d.javascad.vrl.ColorFacetGenerationContext
import eu.printingin3d.javascad.vrl.FacetGenerationContext
import com.github.grishberg.javascad.StlExporter
import java.io.File

fun main() {
    println("=== PC Case STL Generator ===")

    val outDir = File("stl_pccase")
    if (!outDir.exists()) outDir.mkdirs()

    val context: FacetGenerationContext = ColorFacetGenerationContext(eu.printingin3d.javascad.utils.Color.GRAY)
    context.setFn(8)

    println("\nGenerating PC frame (aluminum profiles 20x20mm)...")
    val frame = PcFrame(
        width = 530.0, height = 350.0, depth = 330.0,
        profileSize = 20.0
    ).build()
    exportModel(frame, context, outDir, "frame.stl")

    println("\nGenerating motherboard (Supermicro H12SSL-i)...")
    val mb = Motherboard().build()
    exportModel(mb, context, outDir, "motherboard.stl")

    println("\nGenerating GPU (RTX 3090)...")
    val gpu = Gpu().build()
    exportModel(gpu, context, outDir, "gpu_rtx3090.stl")

    println("\nGenerating PSU...")
    val psu = Psu().build()
    exportModel(psu, context, outDir, "psu.stl")

    println("\n=== All models generated ===")
    println("Output directory: ${outDir.absolutePath}")
    outDir.listFiles()?.filter { it.extension == "stl" }?.sortedBy { it.name }?.forEach { f ->
        val sizeMB = f.length() / 1024.0 / 1024.0
        println("  ${f.name} (${String.format("%.2f", sizeMB)} MB)")
    }
}

private fun exportModel(
    model: Abstract3dModel,
    context: FacetGenerationContext,
    outDir: File,
    fileName: String
) {
    val targetPath = File(outDir, fileName).absolutePath
    try {
        println("  Exporting $fileName...")
        StlExporter.saveStl(model.toCSG(context).polygons, targetPath)
        val size = File(targetPath).length()
        println("  [OK] $fileName (${size} bytes)")
    } catch (e: Exception) {
        println("  [FAIL] $fileName: ${e.message}")
    }
}
