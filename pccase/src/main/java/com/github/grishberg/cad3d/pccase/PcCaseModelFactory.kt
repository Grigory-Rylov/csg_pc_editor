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

        // Building PC frame (aluminum profiles 20x20mm)
        val frameCfg = PcFrame(width = 530.0, depth = 330.0, height = 350.0)
        val frameVertical = frameCfg.buildVertical()
        val frameHorizontal = frameCfg.buildHorizontal()

        val p = AluminumProfile.PROFILE_SIZE
        val bottomZ = p / 2 + p / 2

        // Building motherboard (Tyan S8030)
        val mb = Motherboard().build().move(-40.0, 0.0, bottomZ + 1.6 / 2)

        // Building GPU (Gigabyte RTX 3090 Turbo)
        val gpu = Gpu().build().move(-50.0, 0.0, 71.0)

        // Building PSUs (ATX)
        val hd = 165.0
        val psuY = p / 2 + 150.0 / 2
        val psuBack = Psu().build().rotate(Angles3d.xOnly(90.0)).move(175.0, hd - 70.0, psuY)
        val psuFront = Psu().build().rotate(Angles3d.xOnly(90.0)).move(175.0, -hd + 70.0, psuY)

        // Building CPU cooler (ARCTIC Freezer 4U-M)
        val cooler = Cooler().build().move(50.0, -20.0, bottomZ + 1.6 + 80.0)

        val report = AluminumProfile.generateReport()
        println(report)
        try {
            File("stl_pccase").let { dir ->
                if (!dir.exists()) dir.mkdirs()
                File(dir, "profile_report.txt").writeText(report)
            }
        } catch (_: Exception) {
            // non-critical — may fail on Android
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
