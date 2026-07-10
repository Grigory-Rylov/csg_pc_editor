package com.github.grishberg.cad3d.pccase

import eu.printingin3d.javascad.models.Abstract3dModel
import eu.printingin3d.javascad.models.Cube
import eu.printingin3d.javascad.models.Cylinder
import eu.printingin3d.javascad.models.Hull
import eu.printingin3d.javascad.models.Sphere
import eu.printingin3d.javascad.basic.Radius

class Cooler {

    fun build(): Abstract3dModel {
        val sphere = Sphere(Radius.fromRadius(100.0))
        val cube = Cube(100.0, 100.0, 100.0).move(200.0, 0.0, 0.0)
        val sphereUp = Sphere(Radius.fromRadius(70.0)).move(0.0, 300.0, 0.0)
        val sphereDown = Sphere(Radius.fromRadius(70.0)).move(0.0, -300.0, 0.0)
        val hull = Hull(listOf(sphere, cube, sphereUp, sphereDown))
        val cylinder = Cylinder(300.0, Radius.fromRadius(30.0))
        val cylinder2 = Cylinder(300.0, Radius.fromRadius(30.0)).move(0.0, 50.0, 0.0)
        return hull.subtractModel(cylinder).subtractModel(cylinder2)
    }
}
