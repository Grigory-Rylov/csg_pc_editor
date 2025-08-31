package com.github.grishberg.cad3d.keyboard

import eu.printingin3d.javascad.models.Abstract3dModel

interface ObjectPlace {

    fun place(obj: Abstract3dModel): Abstract3dModel
}
