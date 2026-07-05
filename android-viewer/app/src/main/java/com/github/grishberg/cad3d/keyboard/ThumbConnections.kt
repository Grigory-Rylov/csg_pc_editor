package com.github.grishberg.cad3d.keyboard

import com.github.grishberg.cad3d.keyboard.KeyPlaceholder.placeHolderLeft
import com.github.grishberg.cad3d.keyboard.KeyPlaceholder.placeHolderRight
import eu.printingin3d.javascad.models.Abstract3dModel

class ThumbConnections(private val thumbKeyPlace: ThumbKeyPlace) {

    private val models = ArrayList<Abstract3dModel>()
    fun buildThumbPlaceConnections(): Abstract3dModel {
        models.clear()
        addHull(
            thumbKeyPlace.placeR(placeHolderLeft()), thumbKeyPlace.placeM(placeHolderRight())
        )
        addHull(
            thumbKeyPlace.placeM(placeHolderLeft()), thumbKeyPlace.placeL(placeHolderRight())
        )
        return Utils.union(models)
    }

    private fun addHull(vararg children: Abstract3dModel) {
        models.add(Utils.hull(*children))
    }
}
