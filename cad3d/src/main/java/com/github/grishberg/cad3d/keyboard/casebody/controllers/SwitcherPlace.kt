package com.github.grishberg.cad3d.keyboard.casebody.controllers

import eu.printingin3d.javascad.models.Abstract3dModel

class SwitcherPlace(
    private val controller: Controller,
    private val controllerPlace: ControllerPlace,
) {

    fun place(model: Abstract3dModel): Abstract3dModel {
        return controllerPlace.place(model).move(-19.0, controller.depth / 2.0, 4.5)
    }
}
