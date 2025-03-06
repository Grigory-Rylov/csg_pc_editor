package com.github.grishberg.cad3d.keyboard.walls

import com.github.grishberg.cad3d.keyboard.KeyPlaceholder
import eu.printingin3d.javascad.models.Abstract3dModel

interface WallsBuilder {

    fun backWall(
        onlyBorder: Boolean = false,
        onlyBottomEdge: Boolean = false,
        keyPlace:(Abstract3dModel) -> Abstract3dModel): Abstract3dModel
    fun backMidWall(
        onlyBottomEdge: Boolean = false,
        leftPlace: (Abstract3dModel) -> Abstract3dModel,
        rightPlace: (Abstract3dModel) -> Abstract3dModel,
    ): Abstract3dModel

    fun leftWall(
        onlyBottomEdge: Boolean = false,
        topOffset: Double = 0.0,
        bottomOffset: Double = 0.0,
        keyPlace:(Abstract3dModel) -> Abstract3dModel): Abstract3dModel
    fun leftMidWall(
        onlyBottomEdge: Boolean = false,
        leftPlace: (Abstract3dModel) -> Abstract3dModel,
        rightPlace: (Abstract3dModel) -> Abstract3dModel,
    ): Abstract3dModel
    fun frontWall(
        leftOffset: Double = 0.0,
        rightOffset: Double = 0.0,
        onlyBorder: Boolean = false,
        onlyBottomEdge: Boolean = false,
        keyPlace:(Abstract3dModel) -> Abstract3dModel,
        ): Abstract3dModel
    fun frontMidWall(
        leftOffset: Double = 0.0,
        rightOffset: Double = 0.0,
        onlyBottomEdge: Boolean = false,
        leftPlace: (Abstract3dModel) -> Abstract3dModel,
        rightPlace: (Abstract3dModel) -> Abstract3dModel,
    ): Abstract3dModel
    fun rightWall(onlyBottomEdge: Boolean = false,
        topOffset: Double = 0.0,
        bottomOffset: Double = 0.0,
        keyPlace:(Abstract3dModel) -> Abstract3dModel): Abstract3dModel
    fun rightMidWall(
        onlyBottomEdge: Boolean = false,
        backPlace: (Abstract3dModel) -> Abstract3dModel,
        frontPlace: (Abstract3dModel) -> Abstract3dModel,
    ): Abstract3dModel

    fun midEdge(
        onlyBottomEdge: Boolean = false,
        midPlace: (Abstract3dModel) -> Abstract3dModel,
        leftPlace: (Abstract3dModel) -> Abstract3dModel,
        rightPlace: (Abstract3dModel) -> Abstract3dModel,
    ): Abstract3dModel
}
