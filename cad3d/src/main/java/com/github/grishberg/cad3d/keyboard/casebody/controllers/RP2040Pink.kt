package com.github.grishberg.cad3d.keyboard.casebody.controllers

import com.github.grishberg.cad3d.keyboard.ModelHolder
import com.github.grishberg.cad3d.keyboard.cfg.KeyboardConfig
import com.github.grishberg.cad3d.util.fromModel
import eu.printingin3d.javascad.basic.Radius
import eu.printingin3d.javascad.coords.Angles3d
import eu.printingin3d.javascad.models.Abstract3dModel
import eu.printingin3d.javascad.models.Cube
import eu.printingin3d.javascad.models.Cylinder
import eu.printingin3d.javascad.models.Hull
import eu.printingin3d.javascad.utils.Color

class RP2040Pink(
    private val cfg: KeyboardConfig,
    private val controllerPlace: ControllerPlace,
) : Controller {

    override val width = 20.73
    override val depth = 55.7
    override val height = 1.6

    override val isWireless: Boolean = false

    override fun create(): ModelHolder {
        val usbPort = placeUsbPort(createUsb())
        val model = Cube(width, depth, height).moveZ(height / 2)

        return ModelHolder(
            model.addModel(usbPort),
            fromModel(place(model), Color.CYAN, cfg.fn),
            fromModel(place(usbPort), Color.YELLOW, cfg.fn),
        )
    }

    override fun placeUsbPort(obj: Abstract3dModel): Abstract3dModel {
        return obj.move(0.0, depth / 2 - 1.0, height / 2 +0.5)
    }

    private fun createUsb(): Abstract3dModel {
        val diameter = 3.2
        val width = 8.34

        val cylinder = Cylinder(5.0, Radius.fromDiameter(diameter)).rotate(Angles3d.xOnly(90.0))
        return Hull(
            cylinder.moveX(-width / 2 + diameter / 2), cylinder.moveX(width / 2 - diameter / 2)
        ).moveZ(diameter / 2)
    }

    private fun place(o: Abstract3dModel): Abstract3dModel {
        return controllerPlace.place(this, o).moveZ(1.5)
    }
}
