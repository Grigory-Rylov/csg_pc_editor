package com.github.grishberg.cad3d.keyboard.casebody.controllers.switcher

import com.github.grishberg.cad3d.keyboard.ModelHolder
import eu.printingin3d.javascad.basic.Radius
import eu.printingin3d.javascad.coords.Angles3d
import eu.printingin3d.javascad.models.Abstract3dModel
import eu.printingin3d.javascad.models.Cylinder
import eu.printingin3d.javascad.utils.Color
import eu.printingin3d.javascad.vrl.VertexHolder

class RoundSwitcher : Switcher {

    private val holeDiameter = 6.0

    override fun createSwitcher(): ModelHolder {
        val cylinder = Cylinder(5.0, Radius.fromDiameter(holeDiameter)).rotate(Angles3d.xOnly(90.0))
        return ModelHolder(cylinder, VertexHolder.fromModel(cylinder, Color.PINK, 20))
    }

    override fun createSwitcherHole(): Abstract3dModel {
        return Cylinder(5.0, Radius.fromDiameter(holeDiameter)).rotate(Angles3d.xOnly(90.0))
    }
}
