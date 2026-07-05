package com.github.grishberg.cad3d.pccase

import eu.printingin3d.javascad.coords.Angles3d
import eu.printingin3d.javascad.tranzitions.Union
import eu.printingin3d.javascad.utils.Color
import eu.printingin3d.javascad.vrl.CSG
import eu.printingin3d.javascad.vrl.ColorFacetGenerationContext
import eu.printingin3d.javascad.models.Abstract3dModel

data class PcCaseModel(
    val name: String,
    val model: Abstract3dModel,
    val color: Color
)

class PcCaseBuilder {
    fun build(): List<PcCaseModel> {
        AluminumProfile.reset()

        val p = AluminumProfile.PROFILE_SIZE
        val bottomY = p / 2 + p / 2

        val frameCfg = PcFrame(
            width = 530.0, height = 350.0, depth = 330.0
        )
        val frameVertical = frameCfg.buildVertical()
        val frameHorizontal = frameCfg.buildHorizontal()

        val mb = Motherboard().build()
            .move(-40.0, bottomY + 1.6 / 2, 0.0)

        val gpu = Gpu().build()
            .move(-50.0, 71.0, 150.0)

        val hd = 165.0
        val psuY = p / 2 + 150.0 / 2
        val psuBack = Psu().build()
            .rotate(Angles3d.zOnly(90.0))
            .move(175.0, psuY, hd - 70.0)
        val psuFront = Psu().build()
            .rotate(Angles3d.zOnly(90.0))
            .move(175.0, psuY, -hd + 70.0)

        val cooler = Cooler().build()
            .rotate(Angles3d.zOnly(90.0))
            .move(50.0, bottomY + 1.6 + 80.0, -20.0)

        return listOf(
            PcCaseModel("frame_vertical", frameVertical, Color(100, 140, 200)),
            PcCaseModel("frame_horizontal", frameHorizontal, Color.GRAY),
            PcCaseModel("motherboard", mb, Color.GREEN),
            PcCaseModel("gpu", gpu, Color(200, 30, 30)),
            PcCaseModel("psu_back", psuBack, Color(60, 60, 60)),
            PcCaseModel("psu_front", psuFront, Color(60, 60, 60)),
            PcCaseModel("cooler", cooler, Color(180, 180, 180))
        )
    }

    fun buildCombinedModel(): Abstract3dModel {
        return Union(build().map { it.model })
    }

    fun buildCSG(): List<Pair<String, CSG>> {
        return build().map { pcCaseModel ->
            val context = ColorFacetGenerationContext(pcCaseModel.color)
            context.setFn(8)
            pcCaseModel.name to pcCaseModel.model.toCSG(context)
        }
    }

    fun generateProfileReport(): String {
        build()
        return AluminumProfile.generateReport()
    }
}
