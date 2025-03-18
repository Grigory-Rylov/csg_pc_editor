package com.github.grishberg.cad3d.keyboard.casebody.controllers

import com.github.grishberg.cad3d.keyboard.ModelHolder
import com.github.grishberg.cad3d.keyboard.casebody.controllers.battery.Battery
import com.github.grishberg.cad3d.keyboard.casebody.controllers.switcher.Switcher
import com.github.grishberg.cad3d.keyboard.cfg.KeyboardConfig
import com.github.grishberg.cad3d.keyboard.screws.ScrewWallPlaces
import eu.printingin3d.javascad.basic.Radius
import eu.printingin3d.javascad.coords.Angles3d
import eu.printingin3d.javascad.models.Abstract3dModel
import eu.printingin3d.javascad.models.Cube
import eu.printingin3d.javascad.models.Cylinder
import eu.printingin3d.javascad.models.Hull
import eu.printingin3d.javascad.tranzitions.Union
import eu.printingin3d.javascad.utils.Color
import eu.printingin3d.javascad.vrl.VertexHolder

/**
 * Creates controller holder.
 */
class ControllerHolderBuilder(
    private val cfg: KeyboardConfig,
    private val controller: Controller,
    private val controllerPlace: ControllerPlace,
    private val controllerHolderDimensions: ControllerHolderDimensions,
    private val screwWallPlaces: ScrewWallPlaces,
    private val switcher: Switcher,
    private val battery: Battery,
) {

    fun create(): ModelHolder {
        var model = createControllerModel()

        val vertexHolders = mutableListOf(
            VertexHolder.createVertexHolder(
                model, Color.ORANGE, 20
            )
        )

        if (controller.isWireless) {
            val battery = placeBatteryHolder(battery.create())
            model = model.addModel(battery)
            vertexHolders.add(
                VertexHolder.createVertexHolder(
                    model, Color.YELLOW, 20
                )
            )
        }
        return ModelHolder(model, vertexHolders)
    }

    private fun placeBatteryHolder(o: Abstract3dModel): Abstract3dModel {
        return controllerPlace.place(o.rotate(Angles3d.zOnly(25.0))).move(5.0, -battery.depth + 20.7, -4.0)
    }

    private fun createControllerModel(): Abstract3dModel {
        val width = controller.width
        val depth = controller.depth
        val model = createBase()
        val height = 10.0
        val verticalWall = Cube(33.0, 1.5, 10.0).subtractModel(
            placeSwitcher(switcher.createSwitcherHole()).addModel(usbHole())
        ).move(6.5, 17.5, 2.5)
        return model.addModel(controllerPlace.place(verticalWall)).addModel(controllerPlace.place(createHolder()))
    }

    private fun placeSwitcher(o: Abstract3dModel): Abstract3dModel {
        return o.move(10.0, 0.0, 1.0)
    }

    private fun usbHole(): Abstract3dModel {
        val diameter = 3.5
        val width = 10.0

        val cylinder = Cylinder(5.0, Radius.fromDiameter(diameter)).rotate(Angles3d.xOnly(90.0))
        return Hull(
            cylinder.moveX(-width / 2 + diameter / 2), cylinder.moveX(width / 2 - diameter / 2)
        ).move(-6.5, 0.0, diameter / 2 - 0.3)
    }

    private fun createHolder(): Abstract3dModel {

        val holeWidth = controller.width + 0.2
        val holeDepth = controller.depth + 0.2
        val holeHeight = 4.0
        val height = controller.height + 8.3
        val body = Cube(controller.width + 3.0, controller.depth + 1, height).moveX(-0.5).subtractModel(
            Cube(holeWidth, holeDepth, holeHeight).move(0.0, 1.0, 3.0)
        ).subtractModel(Cube(30.0, 20.0, 5.0).moveZ(3.0))
            .subtractModel(Cube(controller.width - 3.0, controller.depth - 3.0, 20.0))

        return body.move(0.0, 0.0, 1.0)
    }

    private fun createBase(): Abstract3dModel {
        val cylinder = Cube(7.0, 6.0, cfg.controllerPlateHeight)
        val hole = Cylinder(cfg.controllerPlateHeight + 5, Radius.fromDiameter(cfg.screwBoltDiameter + 0.5))
        val horizontal = Hull(
            screwWallPlaces.placeControllerScrews(
                cylinder, ScrewWallPlaces.HeightMode.ControllerHolder, ScrewWallPlaces.ControllerMode.Back
            )

        )

        val vertical = Hull(
            screwWallPlaces.placeControllerScrews(
                cylinder, ScrewWallPlaces.HeightMode.ControllerHolder, ScrewWallPlaces.ControllerMode.Side
            )
        )

        val holes = screwWallPlaces.placeControllerScrews(
            hole, ScrewWallPlaces.HeightMode.ControllerHolder, ScrewWallPlaces.ControllerMode.All
        )

        return Union(horizontal, vertical).moveZ(cfg.controllerPlateHeight / 2).subtractModel(holes)
    }
}
