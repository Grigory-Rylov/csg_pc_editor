package com.github.grishberg.cad3d.pccase

import eu.printingin3d.javascad.coords.Angles3d
import eu.printingin3d.javascad.tranzitions.Union
import eu.printingin3d.javascad.vrl.ColorFacetGenerationContext
import eu.printingin3d.javascad.vrl.FacetGenerationContext
import eu.printingin3d.javascad.vrl.CSG
import java.io.File

object PcCaseModelFactory {

    fun buildAll(): Map<String, CSG> {
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
        val frameCfg = PcFrame(width = 530.0, height = 350.0, depth = 330.0)
        val frameVertical = frameCfg.buildVertical()
        val frameHorizontal = frameCfg.buildHorizontal()

        val p = AluminumProfile.PROFILE_SIZE
        val bottomY = p / 2 + p / 2

        println("\nBuilding motherboard (Tyan S8030)...")
        val mb = Motherboard().build().move(-40.0, bottomY + 1.6 / 2, 0.0)

        println("\nBuilding GPU (Gigabyte RTX 3090 Turbo)...")
        val gpu = Gpu().build().move(-50.0, 71.0, 150.0)

        println("\nBuilding PSUs (ATX)...")
        val hd = 165.0
        val psuY = p / 2 + 150.0 / 2
        val psuBack = Psu().build().rotate(Angles3d.zOnly(90.0)).move(175.0, psuY, hd - 70.0)
        val psuFront = Psu().build().rotate(Angles3d.zOnly(90.0)).move(175.0, psuY, -hd + 70.0)

        println("\nBuilding CPU cooler (ARCTIC Freezer 4U-M)...")
        val cooler = Cooler().build().rotate(Angles3d.zOnly(90.0)).move(50.0, bottomY + 1.6 + 80.0, -20.0)

        val report = AluminumProfile.generateReport()
        println(report)
        File("stl_pccase").let { dir ->
            if (!dir.exists()) dir.mkdirs()
            File(dir, "profile_report.txt").writeText(report)
        }

        return mapOf(
            "frame_vertical" to frameVertical.toCSG(frameVertContext),
            "frame_horizontal" to frameHorizontal.toCSG(defaultContext),
            "motherboard" to mb.toCSG(mbContext),
            "gpu" to gpu.toCSG(gpuContext),
            "psu_back" to psuBack.toCSG(psuContext),
            "psu_front" to psuFront.toCSG(psuContext),
            "cooler" to cooler.toCSG(coolerContext)
        )
    }
}
