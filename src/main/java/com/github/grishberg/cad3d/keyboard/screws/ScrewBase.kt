package com.github.grishberg.cad3d.keyboard.screws

import com.github.grishberg.cad3d.keyboard.cfg.KeyboardConfig
import eu.printingin3d.javascad.basic.Radius
import eu.printingin3d.javascad.models.Abstract3dModel
import eu.printingin3d.javascad.models.Cylinder
import eu.printingin3d.javascad.tranzitions.Union

class ScrewBase(private val cfg: KeyboardConfig) {

    private val holeHeight = 4.0;

    fun screwHolder(height: Double = 5.0): Abstract3dModel {
        val outerDiameter = cfg.screwNutHoleDiameter + cfg.screwHolderWallhickness * 2.0
        return Cylinder(height, Radius.fromRadius(outerDiameter / 2.0)).moveZ(height / 2.0).subtractModel(screwHole())
    }

    private fun screwHole(): Abstract3dModel {
        return Cylinder(holeHeight, Radius.fromRadius(cfg.screwNutHoleDiameter / 2.0)).moveZ(holeHeight / 2.0)
    }

    fun plateScrewHolde(): Abstract3dModel {
        return Union(
            Cylinder(holeHeight, Radius.fromRadius(cfg.screwNutHoleDiameter / 2.0)),
            // head diameter TODO:
            Cylinder(holeHeight, Radius.fromRadius(cfg.screwNutHoleDiameter / 2.0)),
        )
    }

}
