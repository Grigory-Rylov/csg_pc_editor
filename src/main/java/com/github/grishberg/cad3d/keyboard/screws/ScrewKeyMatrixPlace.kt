package com.github.grishberg.cad3d.keyboard.screws

import com.github.grishberg.cad3d.keyboard.KeyPlace
import com.github.grishberg.cad3d.keyboard.KeyPlaceholder
import com.github.grishberg.cad3d.keyboard.ThumbKeyPlace
import com.github.grishberg.cad3d.keyboard.cfg.KeyboardConfig
import eu.printingin3d.javascad.models.Abstract3dModel
import eu.printingin3d.javascad.tranzitions.Union

class ScrewKeyMatrixPlace(
    private val cfg: KeyboardConfig,
    private val keyPlace: KeyPlace,
    private val thumbKeyPlace: ThumbKeyPlace,
    private val verticalOffset: Double = 4.0,
    private val leftOffset: Double = -8.0,
    private val rightOffset: Double = 8.0,
) {

    fun place(o: Abstract3dModel): Abstract3dModel {
        val models = mutableListOf<Abstract3dModel>()
        models.add(keyPlace.place(0, 0, KeyPlaceholder.placeHolderLeft(o)))
        models.add(keyPlace.place(0, cfg.lastRow, KeyPlaceholder.placeHolderLeft(o)))
        models.add(keyPlace.place(cfg.lastCol, 0, KeyPlaceholder.placeHolderRight(o)))
        models.add(keyPlace.place(cfg.lastCol, cfg.lastRow, KeyPlaceholder.placeHolderLeft(o)))
        models.add(thumbKeyPlace.placeL(KeyPlaceholder.placeHolderLeft(o)))

        return Union(models)
    }
}
