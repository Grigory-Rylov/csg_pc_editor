package com.github.grishberg.cad3d.pccase

import eu.printingin3d.javascad.coords.Angles3d
import eu.printingin3d.javascad.tranzitions.Union
import eu.printingin3d.javascad.vrl.ColorFacetGenerationContext
import eu.printingin3d.javascad.vrl.FacetGenerationContext
import eu.printingin3d.javascad.vrl.CSG
import java.io.File

object PcCaseModelFactory {
    const val MB_OFFSET_X = 90.0
    const val MB_OFFSET_Y = 0.0
    const val MB_OFFSET_Z = 20.8

    fun buildAll(): Map<String, CSG> {
        return buildAll(SceneConfig.DEFAULT)
    }

    fun buildAll(config: SceneConfig): Map<String, CSG> {
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

        val frameCfg = PcFrame(
            width = config.frameWidth,
            depth = config.frameDepth,
            height = config.frameHeight,
            levels = config.frameLevels
        )
        val frameVertical = frameCfg.buildVertical()
        val frameHorizontal = frameCfg.buildHorizontal()

        val results = mutableMapOf<String, CSG>()
        results["frame_vertical"] = frameVertical.toCSG(frameVertContext)
        results["frame_horizontal"] = frameHorizontal.toCSG(defaultContext)

        val modelBuilder = ComponentModelBuilder(config.components)
        modelBuilder.build().forEach { (name, model, context) ->
            results[name] = model.toCSG(context)
        }

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

        return results
    }

    private class ComponentModelBuilder(components: List<ComponentPlacement>) {
        private val result = mutableListOf<Triple<String, eu.printingin3d.javascad.models.Abstract3dModel, FacetGenerationContext>>()

        init {
            val mbContext = ColorFacetGenerationContext(eu.printingin3d.javascad.utils.Color.GREEN).apply { setFn(8) }
            val gpuContext = ColorFacetGenerationContext(eu.printingin3d.javascad.utils.Color(200, 30, 30)).apply { setFn(8) }
            val psuContext = ColorFacetGenerationContext(eu.printingin3d.javascad.utils.Color(60, 60, 60)).apply { setFn(8) }
            val coolerContext = ColorFacetGenerationContext(eu.printingin3d.javascad.utils.Color(180, 180, 180)).apply { setFn(30) }

            for (cp in components) {
                when (cp.type) {
                    "motherboard" -> {
                        val model = Motherboard().build().move(cp.x, cp.y, cp.z)
                        result.add(Triple("motherboard", model, mbContext))
                    }
                    "gpu" -> {
                        val parts = mutableListOf<eu.printingin3d.javascad.models.Abstract3dModel>()
                        for (i in 0 until cp.count) {
                            val offset = if (cp.count > 1) i * cp.spacing else 0.0
                            parts.add(Gpu().build().move(
                                cp.x + offset,
                                cp.y,
                                cp.z
                            ))
                        }
                        val model = if (parts.size == 1) parts[0] else Union(parts)
                        result.add(Triple("gpu", model, gpuContext))
                    }
                    "psu" -> {
                        var model = Psu().build()
                        if (cp.rotation != 0.0) {
                            model = model.rotate(Angles3d.xOnly(cp.rotation))
                        }
                        model = model.move(cp.x, cp.y, cp.z)
                        val name = "psu_${result.size}"
                        result.add(Triple(name, model, psuContext))
                    }
                    "cooler" -> {
                        val model = Cooler().build().move(cp.x, cp.y, cp.z)
                        result.add(Triple("cooler", model, coolerContext))
                    }
                }
            }
        }

        fun build(): List<Triple<String, eu.printingin3d.javascad.models.Abstract3dModel, FacetGenerationContext>> = result
    }
}
