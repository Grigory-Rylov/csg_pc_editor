package com.github.grishberg.cad3d.keyboard

import eu.printingin3d.javascad.basic.Radius
import eu.printingin3d.javascad.coords.V3d
import eu.printingin3d.javascad.models.Abstract3dModel
import eu.printingin3d.javascad.models.Cube
import eu.printingin3d.javascad.models.Cylinder
import eu.printingin3d.javascad.models.Hull
import eu.printingin3d.javascad.models.Sphere
import eu.printingin3d.javascad.tranzitions.Union

object Utils {

    @JvmStatic
    fun union(models: List<Abstract3dModel>): Abstract3dModel {
        return Union(models)
    }

    @JvmStatic
    fun union(vararg models: Abstract3dModel): Abstract3dModel {
        return Union(*models)
    }

    @JvmStatic
    fun hull(vararg models: Abstract3dModel): Abstract3dModel {
        return Hull(*models)
    }

    @JvmStatic
    fun hull(models: List<Abstract3dModel>): Abstract3dModel {
        return Hull(models)
    }

    @JvmStatic
    fun cube(x: Number, y: Number, z: Number): Cube {
        return Cube(x.toDouble(), y.toDouble(), z.toDouble())
    }

    @JvmStatic
    fun cube(size: Double): Cube {
        return Cube(size)
    }

    @JvmStatic
    fun cylinder(radius: Number, height: Number): Cylinder {
        return Cylinder(height.toDouble(), Radius.fromRadius(radius.toDouble()))
    }

    @JvmStatic
    fun sphere(radius: Number): Sphere {
        return Sphere(Radius.fromRadius(radius.toDouble()))
    }

    @JvmStatic
    fun v3d(x: Double, y: Double, z: Double): V3d {
        return V3d(x, y, z)
    }

    @JvmStatic
    fun v3d(x: Number, y: Number, z: Number): V3d {
        return V3d(x.toDouble(), y.toDouble(), z.toDouble())
    }
}
