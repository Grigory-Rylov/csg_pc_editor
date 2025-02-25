package com.github.grishberg.cad3d.keyboard.walls

import eu.printingin3d.javascad.models.Abstract3dModel

interface WallsBuilder {

    fun backWall(keyPlace:(Abstract3dModel) -> Abstract3dModel): Abstract3dModel
    fun backMidWall(
        leftPlace: (Abstract3dModel) -> Abstract3dModel,
        rightPlace: (Abstract3dModel) -> Abstract3dModel,
    ): Abstract3dModel

    fun leftWall(keyPlace:(Abstract3dModel) -> Abstract3dModel): Abstract3dModel
    fun leftMidWall(
        leftPlace: (Abstract3dModel) -> Abstract3dModel,
        rightPlace: (Abstract3dModel) -> Abstract3dModel,
    ): Abstract3dModel
    fun frontWall(keyPlace:(Abstract3dModel) -> Abstract3dModel): Abstract3dModel
    fun frontMidWall(
        leftPlace: (Abstract3dModel) -> Abstract3dModel,
        rightPlace: (Abstract3dModel) -> Abstract3dModel,
    ): Abstract3dModel
    fun rightWall(keyPlace:(Abstract3dModel) -> Abstract3dModel): Abstract3dModel
    fun rightMidWall(
        backPlace: (Abstract3dModel) -> Abstract3dModel,
        frontPlace: (Abstract3dModel) -> Abstract3dModel,
    ): Abstract3dModel
}
