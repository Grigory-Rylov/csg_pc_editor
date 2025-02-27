package com.github.grishberg.cad3d.keyboard.walls

import eu.printingin3d.javascad.models.Abstract3dModel

class OuterWallsBuilder: WallsBuilder {

    override fun backWall(keyPlace: (Abstract3dModel) -> Abstract3dModel): Abstract3dModel {
        TODO("Not yet implemented")
    }

    override fun backMidWall(
        leftPlace: (Abstract3dModel) -> Abstract3dModel, rightPlace: (Abstract3dModel) -> Abstract3dModel
    ): Abstract3dModel {
        TODO("Not yet implemented")
    }

    override fun leftWall(keyPlace: (Abstract3dModel) -> Abstract3dModel): Abstract3dModel {
        TODO("Not yet implemented")
    }

    override fun leftMidWall(
        leftPlace: (Abstract3dModel) -> Abstract3dModel, rightPlace: (Abstract3dModel) -> Abstract3dModel
    ): Abstract3dModel {
        TODO("Not yet implemented")
    }

    override fun frontWall(keyPlace: (Abstract3dModel) -> Abstract3dModel): Abstract3dModel {
        TODO("Not yet implemented")
    }

    override fun frontMidWall(
        leftPlace: (Abstract3dModel) -> Abstract3dModel, rightPlace: (Abstract3dModel) -> Abstract3dModel
    ): Abstract3dModel {
        TODO("Not yet implemented")
    }

    override fun rightWall(keyPlace: (Abstract3dModel) -> Abstract3dModel): Abstract3dModel {
        TODO("Not yet implemented")
    }

    override fun rightMidWall(
        backPlace: (Abstract3dModel) -> Abstract3dModel, frontPlace: (Abstract3dModel) -> Abstract3dModel
    ): Abstract3dModel {
        TODO("Not yet implemented")
    }

    override fun midEdge(
        midPlace: (Abstract3dModel) -> Abstract3dModel,
        leftPlace: (Abstract3dModel) -> Abstract3dModel,
        rightPlace: (Abstract3dModel) -> Abstract3dModel
    ): Abstract3dModel {
        TODO("Not yet implemented")
    }
}
