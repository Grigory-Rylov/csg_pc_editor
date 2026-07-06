package com.github.grishberg.cad3d.pccase

import eu.printingin3d.javascad.coords.Angles3d
import eu.printingin3d.javascad.enums.Side
import eu.printingin3d.javascad.tranzitions.Union
import eu.printingin3d.javascad.vrl.ColorFacetGenerationContext
import eu.printingin3d.javascad.vrl.FacetGenerationContext
import eu.printingin3d.javascad.vrl.CSG
import java.io.File

object PcCaseModelFactory {
    private val gpuOffsetZ = 170

    const val MB_OFFSET_X = 90.0
    const val MB_OFFSET_Y = 0.0
    const val MB_OFFSET_Z = 20.8  // bottomZ + pcbThickness/2 = 20.0 + 0.8

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
        val mb = Motherboard().build().move(MB_OFFSET_X, MB_OFFSET_Y, MB_OFFSET_Z)

        // Building GPU (Gigabyte RTX 3090 Turbo)
        val gpuOffsetX = 50
        val gpu =
            Gpu().build().moveX(-gpuOffsetX)
                .addModel(Gpu().build().moveX(-gpuOffsetX * 2))
                .addModel(Gpu().build().moveX(-gpuOffsetX * 3))
                .addModel(Gpu().build().moveX(-gpuOffsetX * 4))
                .addModel(Gpu().build().moveX(-gpuOffsetX * 5))
                .align(Side.TOP_OUT, mb).move(80,0,10.0 + gpuOffsetZ)

        // Building PSUs (ATX)
        val hd = 165.0
        val pcuX = -240
        val psuBack = Psu().build().rotate(Angles3d.xOnly(90.0)).align(Side.TOP_OUT_CENTER, mb).move(pcuX, hd - 70.0, 0)
        val psuFront = Psu().build().rotate(Angles3d.xOnly(90.0)).align(Side.TOP_OUT_CENTER, mb).move(pcuX, -hd + 70.0, 0)

        // Building CPU cooler (ARCTIC Freezer 4U-M)
        val cooler = Cooler().build().align(Side.TOP_OUT_CENTER, mb).move(65.0, -20.0, 7.0)

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
