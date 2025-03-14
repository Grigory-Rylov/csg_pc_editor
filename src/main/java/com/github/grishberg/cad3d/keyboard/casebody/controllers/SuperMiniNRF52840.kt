package com.github.grishberg.cad3d.keyboard.casebody.controllers

import com.github.grishberg.cad3d.keyboard.KeyPlace
import com.github.grishberg.cad3d.keyboard.ModelHolder
import com.github.grishberg.cad3d.keyboard.cfg.KeyboardConfig
import eu.printingin3d.javascad.basic.Radius
import eu.printingin3d.javascad.coords.Angles3d
import eu.printingin3d.javascad.coords.V3d
import eu.printingin3d.javascad.models.Abstract3dModel
import eu.printingin3d.javascad.models.Cube
import eu.printingin3d.javascad.models.Cylinder
import eu.printingin3d.javascad.models.Hull
import eu.printingin3d.javascad.utils.Color
import eu.printingin3d.javascad.vrl.VertexHolder

class SuperMiniNRF52840(private val cfg: KeyboardConfig, private val keyPlace: KeyPlace) {

    fun create(): ModelHolder {
        val width = 17.78
        val depth = 33.0
        val height = 1.7
        val usbPort = createUsb().move(0.0, depth/2 - 1.0, height/2)
        val model = Cube(width, depth, height).moveZ(height/2)

        return ModelHolder(
            model.addModel(usbPort),
            VertexHolder.createVertexHolder(placeController(model), Color.CYAN, cfg.fn),
            VertexHolder.createVertexHolder(placeController(usbPort), Color.YELLOW, cfg.fn),
        )
    }

    private fun placeController(
        obj: Abstract3dModel,
    ): Abstract3dModel {
        val p = keyPlace.calculateCoordinates(0, 0)
        val targetPoint = V3d(p.x, p.y, 0.0)
        return obj.move(targetPoint.add(V3d(0.0, 10.0, 0.0)))
    }

    private fun createUsb(): Abstract3dModel {
        val diameter = 2.54
        val width = 8.34

        val cylinder = Cylinder(5.0, Radius.fromDiameter(diameter)).rotate(Angles3d.xOnly(90.0))
        return Hull(
            cylinder.moveX(-width / 2 + diameter / 2), cylinder.moveX(width / 2 - diameter / 2)
        ).moveZ(diameter/2)
    }
}
