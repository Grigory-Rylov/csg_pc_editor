package com.github.grishberg.cad3d.keyboard.matrix

import com.github.grishberg.cad3d.keyboard.*
import com.github.grishberg.cad3d.keyboard.amoeba.Amoeba
import com.github.grishberg.cad3d.keyboard.casebody.Walls
import com.github.grishberg.cad3d.keyboard.cfg.KeyPlaceholderType
import com.github.grishberg.cad3d.keyboard.cfg.KeyboardConfig
import com.github.grishberg.cad3d.keyboard.cfg.WallsSettings
import com.github.grishberg.cad3d.keyboard.screws.ScrewBase
import com.github.grishberg.cad3d.keyboard.screws.ScrewKeyMatrixPlace
import eu.printingin3d.javascad.models.Abstract3dModel
import eu.printingin3d.javascad.models.IModel
import eu.printingin3d.javascad.tranzitions.Union
import eu.printingin3d.javascad.utils.Color
import eu.printingin3d.javascad.vrl.VertexHolder

class KeyMatrixBuilder(
    private val cfg: KeyboardConfig,
    private val keyPlace: KeyPlace,
    private val thumbKeyPlace: ThumbKeyPlace
) {
    fun createConnectionsModel(): ModelHolder {
        val connections = Connections(cfg, keyPlace).buildConnections()
        val thumbPlaceConnections = ThumbConnections(thumbKeyPlace).buildThumbPlaceConnections()

        return ModelHolder(
            connections.addModel(thumbPlaceConnections),
            createVertexHolder(connections, DEFAULT_COLOR),
            createVertexHolder(thumbPlaceConnections, DEFAULT_COLOR)
        )
    }

    fun createBordersModel(
        amoebaHoles: Abstract3dModel? = null
    ): ModelHolder {
        val screwBase = ScrewBase(cfg)
        val screws = ScrewKeyMatrixPlace(cfg, keyPlace, thumbKeyPlace).place(screwBase.matrixScrewHole())

        val borderHeigth = 2.5
        val wallsSettings = WallsSettings(
            borderHeight = borderHeigth, bottomBorderHeight = 4.0

        )
        var borders = Walls(cfg, wallsSettings, keyPlace, thumbKeyPlace, topEdgeOffsetZ = 0.0).createBorders(
            1.5, borderHeigth
        ).subtractModel(screws).subtractModel(amoebaHoles)

        return ModelHolder(
            borders,
            createVertexHolder(borders, Color.lightGray),
        )
    }

    fun createPlaceholders(): ModelHolder {
        val models = mutableListOf<Abstract3dModel>()

        val amoeba = Amoeba(cfg)
        val amoebaHole = amoeba.createHoles(height = 7.0, diameter = 0.7).addModel(amoeba.createSimple())

        val placeHolder = if (cfg.keyPlaceholderType == KeyPlaceholderType.AmoebaSu120) {
            KeyPlaceholder.placeHolder(cfg).subtractModel(amoebaHole)
        } else {
            KeyPlaceholder.placeHolder(cfg)
        }

        for (column in 0 until cfg.columnsCount) {
            for (row in 0 until cfg.rowsCount) {
                models.add(keyPlace.place(column, row, placeHolder))
            }
        }

        models.add(thumbKeyPlace.thumbPlace(placeHolder))

        val allPlaceholders = Union(models)

        //TODO
        //saveModel("placeHolder.stl", placeHolder)

        return ModelHolder(
            allPlaceholders,
            createVertexHolder(allPlaceholders, Color(30, 127, 40)),
        )
    }

    private fun createThumbKeyPlaceModel(
        model: Abstract3dModel, thumbKeyPlace: ThumbKeyPlace, color: Color
    ): ModelHolder {
        val placedModel = thumbKeyPlace.thumbPlace(model)
        return ModelHolder(placedModel, createVertexHolder(placedModel, color))
    }


    private fun createVertexHolder(model: IModel, color: Color): VertexHolder {
        return VertexHolder.fromModel(model, color, cfg.fn)
    }

    companion object {

        private val DEFAULT_COLOR = Color.GRAY
    }

}