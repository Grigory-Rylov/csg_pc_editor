package com.github.grishberg.cad3d.keyboard.walls

import eu.printingin3d.javascad.models.Abstract3dModel

interface CornerWallBuilder {

    fun backLeft(onlyBottomEdge: Boolean = false, keyPlace: (Abstract3dModel) -> Abstract3dModel): Abstract3dModel
    fun backRight(onlyBottomEdge: Boolean = false, keyPlace: (Abstract3dModel) -> Abstract3dModel): Abstract3dModel
    fun frontLeft(onlyBottomEdge: Boolean = false, keyPlace: (Abstract3dModel) -> Abstract3dModel): Abstract3dModel
    fun frontRight(onlyBottomEdge: Boolean = false, keyPlace: (Abstract3dModel) -> Abstract3dModel): Abstract3dModel

    fun frontRightToMatrix(
        onlyBottomEdge: Boolean = false,
        keyPlace: (Abstract3dModel) -> Abstract3dModel,
        matrixOuterPlace: (Abstract3dModel) -> Abstract3dModel,
        matrixInnerPlace: (Abstract3dModel) -> Abstract3dModel,
    ): Abstract3dModel {
        return frontRight(onlyBottomEdge, keyPlace)
    }
}
