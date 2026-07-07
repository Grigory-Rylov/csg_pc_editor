package com.github.grishberg.cad3d.pccase

import eu.printingin3d.javascad.coords.V3d
import eu.printingin3d.javascad.models.Abstract3dModel
import eu.printingin3d.javascad.tranform.TransformationFactory
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

        val radiatorContext: FacetGenerationContext = ColorFacetGenerationContext(eu.printingin3d.javascad.utils.Color(50, 50, 55))
        radiatorContext.setFn(8)

        AluminumProfile.reset()

        val frameCfg = PcFrame(
            width = config.frameWidth,
            depth = config.frameDepth,
            height = config.frameHeight,
            levels = config.frameLevels,
            bottomBeams = config.frameBottomBeams
        )
        val frameVertical = frameCfg.buildVertical()
        val frameHorizontal = frameCfg.buildHorizontal()

        val results = mutableMapOf<String, CSG>()
        results["frame_vertical"] = frameVertical.toCSG(frameVertContext)
        results["frame_horizontal"] = frameHorizontal.toCSG(defaultContext)

        val modelBuilder = ComponentModelBuilder(config.components)
        modelBuilder.build().forEach { entry ->
            var csg = entry.baseModel.toCSG(entry.context)
            var tx = 0.0; var ty = 0.0; var tz = 0.0
            for (op in entry.transforms) {
                when (op) {
                    is TransformOp.Move -> { tx += op.x; ty += op.y; tz += op.z }
                    is TransformOp.Rotate -> {
                        csg = csg.transformed(TransformationFactory.getRotationMatrix(op.angles))
                    }
                }
            }
            if (tx != 0.0 || ty != 0.0 || tz != 0.0) {
                csg = csg.transformed(TransformationFactory.getTranlationMatrix(V3d(tx, ty, tz)))
            }
            results[entry.name] = csg
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
        data class ModelWithTransforms(
            val name: String,
            val baseModel: Abstract3dModel,
            val context: FacetGenerationContext,
            val transforms: List<TransformOp>
        )
        private val result = mutableListOf<ModelWithTransforms>()
        private val nameCounters = mutableMapOf<String, Int>()

        init {
            val mbContext = ColorFacetGenerationContext(eu.printingin3d.javascad.utils.Color.GREEN).apply { setFn(8) }
            val gpuContext = ColorFacetGenerationContext(eu.printingin3d.javascad.utils.Color(200, 30, 30)).apply { setFn(8) }
            val psuContext = ColorFacetGenerationContext(eu.printingin3d.javascad.utils.Color(60, 60, 60)).apply { setFn(8) }
            val coolerContext = ColorFacetGenerationContext(eu.printingin3d.javascad.utils.Color(180, 180, 180)).apply { setFn(8) }
            val radiatorContext = ColorFacetGenerationContext(eu.printingin3d.javascad.utils.Color(50, 50, 55)).apply { setFn(8) }

            for (cp in components) {
                val name: String
                val context: FacetGenerationContext

                val baseName = cp.type

                val baseModel: Abstract3dModel = when (cp.type) {
                    "motherboard" -> {
                        context = mbContext
                        Motherboard().build()
                    }
                    "gpu" -> {
                        context = gpuContext
                        val parts = mutableListOf<Abstract3dModel>()
                        for (i in 0 until cp.count) {
                            val offset = if (cp.count > 1) i * cp.spacing else 0.0
                            var gpu = Gpu().build()
                            if (offset != 0.0) gpu = gpu.moveX(offset)
                            parts.add(gpu)
                        }
                        if (parts.size == 1) parts[0] else Union(parts)
                    }
                    "psu" -> {
                        context = psuContext
                        Psu().build()
                    }
                    "cooler" -> {
                        context = coolerContext
                        Cooler().build()
                    }
                    "radiator" -> {
                        context = radiatorContext
                        Radiator().build()
                    }
                    else -> throw IllegalArgumentException("unknown type: ${cp.type}")
                }

                val counter = nameCounters.getOrDefault(baseName, 0)
                nameCounters[baseName] = counter + 1
                name = if (counter == 0) baseName else "${baseName}_$counter"

                result.add(ModelWithTransforms(name, baseModel, context, cp.transforms))
            }
        }

        fun build(): List<ModelWithTransforms> = result
    }
}
