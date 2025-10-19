package com.github.grishberg.cad3d

import com.github.grishberg.cad3d.keyboard.KeyCaps
import com.github.grishberg.cad3d.keyboard.KeyHolderBottomWalls
import com.github.grishberg.cad3d.keyboard.KeyPlace
import com.github.grishberg.cad3d.keyboard.KeyPlaceHoles
import com.github.grishberg.cad3d.keyboard.KeySwitchHoles
import com.github.grishberg.cad3d.keyboard.ModelHolder
import com.github.grishberg.cad3d.keyboard.ThumbKeyPlace
import com.github.grishberg.cad3d.keyboard.Utils
import com.github.grishberg.cad3d.keyboard.amoeba.Amoeba
import com.github.grishberg.cad3d.keyboard.casebody.DefaultBottomEdgePatcher
import com.github.grishberg.cad3d.keyboard.casebody.Walls
import com.github.grishberg.cad3d.keyboard.casebody.controllers.Controller
import com.github.grishberg.cad3d.keyboard.casebody.controllers.ControllerFactory
import com.github.grishberg.cad3d.keyboard.casebody.controllers.ControllerHolderBuilder
import com.github.grishberg.cad3d.keyboard.casebody.controllers.ControllerHolderDimensions
import com.github.grishberg.cad3d.keyboard.casebody.controllers.ControllerPlace
import com.github.grishberg.cad3d.keyboard.casebody.controllers.SwitcherPlace
import com.github.grishberg.cad3d.keyboard.casebody.controllers.battery.BatteryFactory
import com.github.grishberg.cad3d.keyboard.casebody.controllers.switcher.SwitcherFactory
import com.github.grishberg.cad3d.keyboard.casebody.thumb.SingleColumn3ButtonsThumbWalls
import com.github.grishberg.cad3d.keyboard.casebody.thumb.SingleColumn3ButtonsThumbsBordersBuilder
import com.github.grishberg.cad3d.keyboard.casebody.thumb.ThumbBorders
import com.github.grishberg.cad3d.keyboard.casebody.thumb.ThumbPoints
import com.github.grishberg.cad3d.keyboard.casebody.thumb.ThumbWalls
import com.github.grishberg.cad3d.keyboard.casebody.thumb.TwoRows5ButtonsThumbWalls
import com.github.grishberg.cad3d.keyboard.casebody.thumb.TwoRows5ButtonsMatrixThumbsBordersBuilder
import com.github.grishberg.cad3d.keyboard.casebody.wall.ControllerHolderWall
import com.github.grishberg.cad3d.keyboard.casebody.wall.FrontRightToMatrixWallBuilder
import com.github.grishberg.cad3d.keyboard.casebody.wall.SingleRow3ButtonsFrontRightToMatrixWallBuilder
import com.github.grishberg.cad3d.keyboard.casebody.wall.TwoRowsButtonsFrontRightToMatrixWallBuilder
import com.github.grishberg.cad3d.keyboard.cfg.KeyboardConfig
import com.github.grishberg.cad3d.keyboard.cfg.WallsSettings
import com.github.grishberg.cad3d.keyboard.matrix.KeyMatrix
import com.github.grishberg.cad3d.keyboard.plate.BottomPoints
import com.github.grishberg.cad3d.keyboard.plate.PlateFactory
import com.github.grishberg.cad3d.keyboard.screws.ScrewBase
import com.github.grishberg.cad3d.keyboard.screws.ScrewKeyMatrixPlace
import com.github.grishberg.cad3d.keyboard.screws.ScrewWallPlaces
import com.github.grishberg.cad3d.keyboard.screws.ScrewsMatrixHolder
import com.github.grishberg.cad3d.keyboard.wristrest.WristRest
import com.github.grishberg.cad3d.plugin.ResultListener
import com.github.grishberg.cad3d.plugin.VertexHolder
import com.github.grishberg.cad3d.plugin.cfg.KeyPlaceholderType
import com.github.grishberg.cad3d.plugin.cfg.KeyboardPart
import com.github.grishberg.cad3d.plugin.cfg.ThumbClusterMode
import com.github.grishberg.cad3d.plugin.cfg.TrackballMode
import com.github.grishberg.cad3d.trackball.Trackball
import com.github.grishberg.cad3d.util.fromModel
import com.github.grishberg.javascad.StlExporter
import eu.printingin3d.javascad.coords.V3d
import eu.printingin3d.javascad.models.Abstract3dModel
import eu.printingin3d.javascad.models.Cube
import eu.printingin3d.javascad.models.Cylinder
import eu.printingin3d.javascad.models.IModel
import eu.printingin3d.javascad.tranzitions.Union
import eu.printingin3d.javascad.utils.Color
import eu.printingin3d.javascad.vrl.ColorFacetGenerationContext
import eu.printingin3d.javascad.vrl.FacetGenerationContext
import java.io.File
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class KeyboardBuilder(
    private val coroutineScope: CoroutineScope,
    private val mainThreadDispatcher: CoroutineDispatcher,
) {

    private var resolution = 15 // Количество промежуточных точек между заданными точками
    private val cache = ConcurrentHashMap<KeyboardPart, List<VertexHolder>>()
    private val currentResults = mutableListOf<VertexHolder>()

    fun rebuildModels(cfg: KeyboardConfig, listener: ResultListener) {
        if (resolution == 0) {
            resolution = 20
        }
        create3dModels(cfg, listener)
    }

    private fun create3dModels(cfg: KeyboardConfig, listener: ResultListener) {
        currentResults.clear()

        val visibleModels = cfg.visibleKeyboardParts
        println("--------------- create3dModels: ${this} visibleModels = $visibleModels, modifiedKeyboardParts = ${cfg.modifiedKeyboardParts}")

        // remove modified parts from cache
        cfg.modifiedKeyboardParts.forEach {
            cache.remove(it)
        }

        val keyPlace = KeyPlace(cfg.keyPlaceConfig)
        val thumbKeyPlace = ThumbKeyPlace(cfg)
        val trackball = Trackball(cfg, keyPlace)

        val controllerFactory = ControllerFactory(cfg)
        val controller = controllerFactory.createController()

        val controllerPlace = ControllerPlace(cfg, keyPlace, controller)
        val switcherPlace = SwitcherPlace(controller, controllerPlace)
        val switcherFactory = SwitcherFactory(cfg)
        val controllerHolderDimensions = ControllerHolderDimensions()

        val wallsSettings = WallsSettings(bottomBorderHeight = 1.0)
        val controllerHolderWall = ControllerHolderWall(wallsSettings, keyPlace)
        val screwWallPlaces = ScrewWallPlaces(
            cfg, wallsSettings, keyPlace, thumbKeyPlace, controllerHolderWall, controllerHolderDimensions
        )

        val topEdgeOffsetZ = -2.0

        val thumbPoints = ThumbPoints(cfg, keyPlace, thumbKeyPlace)
        val bottomEdgePatcher = DefaultBottomEdgePatcher(
            wallsSettings.borderThickness, wallsSettings.bottomBorderHeight
        )
        val frontRightToMatrixWallBuilder: FrontRightToMatrixWallBuilder =  when (cfg.thumbClusterSettings.type) {
            ThumbClusterMode.SingleColumn3Buttons -> SingleRow3ButtonsFrontRightToMatrixWallBuilder(cfg, bottomEdgePatcher, topEdgeOffsetZ)
            ThumbClusterMode.SingleColumn4Buttons -> SingleRow3ButtonsFrontRightToMatrixWallBuilder(cfg, bottomEdgePatcher, topEdgeOffsetZ)
            ThumbClusterMode.TwoRows5Buttons -> TwoRowsButtonsFrontRightToMatrixWallBuilder(cfg, bottomEdgePatcher, topEdgeOffsetZ, thumbPoints)
        }

        val thumbBorders = when (cfg.thumbClusterSettings.type) {
            ThumbClusterMode.SingleColumn3Buttons -> SingleColumn3ButtonsThumbsBordersBuilder(thumbKeyPlace)
            ThumbClusterMode.SingleColumn4Buttons -> SingleColumn3ButtonsThumbsBordersBuilder(thumbKeyPlace)
            ThumbClusterMode.TwoRows5Buttons -> TwoRows5ButtonsMatrixThumbsBordersBuilder(thumbKeyPlace)
        }

        val thumbWalls: ThumbWalls = when (cfg.thumbClusterSettings.type) {
            ThumbClusterMode.SingleColumn3Buttons -> SingleColumn3ButtonsThumbWalls(cfg, keyPlace, thumbKeyPlace, frontRightToMatrixWallBuilder)
            ThumbClusterMode.SingleColumn4Buttons -> SingleColumn3ButtonsThumbWalls(cfg, keyPlace, thumbKeyPlace, frontRightToMatrixWallBuilder)
            ThumbClusterMode.TwoRows5Buttons -> TwoRows5ButtonsThumbWalls(cfg, keyPlace, thumbKeyPlace, thumbPoints, frontRightToMatrixWallBuilder)
        }
        val walls = Walls(
            cfg, wallsSettings, keyPlace, thumbKeyPlace, topEdgeOffsetZ = topEdgeOffsetZ,
            thumbBorders = thumbBorders,
            thumbWalls = thumbWalls,
        )

        val resultsChannel = Channel<List<VertexHolder>>()
        coroutineScope.launch {
            createIfNeeded(resultsChannel, KeyboardPart.KeyMatrix, visibleModels) {
                createMatrix(cfg, keyPlace, thumbKeyPlace, thumbBorders, thumbWalls)
            }

            createIfNeeded(resultsChannel, KeyboardPart.Case, visibleModels) {
                createCase(
                    cfg,
                    keyPlace,
                    thumbKeyPlace,
                    screwWallPlaces,
                    walls,
                    controllerPlace,
                    switcherPlace,
                    controllerFactory,
                    switcherFactory,
                    controller,
                    trackball,
                    thumbBorders = thumbBorders,
                    thumbWalls = thumbWalls,
                )
            }
            createIfNeeded(resultsChannel, KeyboardPart.KeyCaps, visibleModels) {
                createKeyCaps(cfg, keyPlace, thumbKeyPlace)
            }
            createIfNeeded(resultsChannel, KeyboardPart.WristRest, visibleModels) {
                createWristRest(cfg)
            }
            createIfNeeded(resultsChannel, KeyboardPart.TrackBallHolder, visibleModels) {
                createTrackballHolder(cfg, trackball)
            }
            createIfNeeded(resultsChannel, KeyboardPart.TrackBall, visibleModels) {
                createTrackBall(cfg, keyPlace)
            }
            createIfNeeded(resultsChannel, KeyboardPart.TrackBallSensor, visibleModels) {
                createTrackballSensor(cfg, keyPlace)
            }
            createIfNeeded(resultsChannel, KeyboardPart.TrackBallSensorCap, visibleModels) {
                createTrackballSensorCap(cfg, keyPlace)
            }
            createIfNeeded(resultsChannel, KeyboardPart.Controller, visibleModels) {
                createController(controllerFactory, controllerPlace)
            }
            createIfNeeded(resultsChannel, KeyboardPart.ControllerHolder, visibleModels) {
                createControllerHolder(
                    cfg = cfg,
                    controllerPlace = controllerPlace,
                    switcherPlace = switcherPlace,
                    controller = controller,
                    controllerHolderDimensions = controllerHolderDimensions,
                    screwWallPlaces = screwWallPlaces,
                )
            }
            createIfNeeded(resultsChannel, KeyboardPart.Plate, visibleModels) {
                createPlate(cfg, keyPlace, thumbKeyPlace, wallsSettings, screwWallPlaces)
            }
            createIfNeeded(resultsChannel, KeyboardPart.Amoeba, visibleModels) {
                createAmoeba(cfg, keyPlace, thumbKeyPlace)
            }

            val expectedCount = visibleModels.size
            launch(mainThreadDispatcher) {
                var receivedCount = 0

                while (receivedCount < expectedCount && isActive) {
                    try {
                        currentResults.addAll(resultsChannel.receive())
                        receivedCount++
                        // Emit partial update (not complete yet)
                        listener.onReady(currentResults.toList(), complete = receivedCount >= expectedCount)
                    } catch (e: ClosedReceiveChannelException) {
                        println("Channel was closed ${this@KeyboardBuilder}" + e.message)
                        break
                    }
                }
                println("--------------- create3dModels: $this onReady : ${currentResults.size}")

                if (receivedCount >= expectedCount) {
                    // Ensure final complete callback in case loop ended exactly
                    listener.onReady(currentResults.toList(), complete = true)
                }
                resultsChannel.close()
            }
        }
    }

    private fun CoroutineScope.createIfNeeded(
        resultsChannel: Channel<List<VertexHolder>>,
        keyboardPart: KeyboardPart,
        visibleModels: Set<KeyboardPart>,
        producer: () -> List<VertexHolder>
    ) {
        if (!visibleModels.contains(keyboardPart)) {
            return
        }
        val cachedPart = cache.get(keyboardPart)

        launch {
            if (cachedPart == null) {
                val vertexHolders = producer.invoke()
                cache[keyboardPart] = vertexHolders
                resultsChannel.send(vertexHolders)
            } else {
                resultsChannel.send(cachedPart)
            }
        }
    }

    private fun createMatrix(
        cfg: KeyboardConfig, keyPlace: KeyPlace, thumbKeyPlace: ThumbKeyPlace,
        thumbBorders: ThumbBorders,
        thumbWalls: ThumbWalls,
    ): List<VertexHolder> {
        val result = mutableListOf<VertexHolder>()

        val startTime = System.currentTimeMillis()

        val keyMatrix = KeyMatrix(cfg, keyPlace, thumbKeyPlace)
        val connections = keyMatrix.createConnectionsModel()
        val amoebaHoles = amoebaHoles(
            cfg, keyPlace, thumbKeyPlace
        ).takeIf { cfg.keyPlaceConfig.keyPlaceholderType == KeyPlaceholderType.AmoebaSu120 }
        val borders = keyMatrix.createBordersModel(amoebaHoles, thumbBorders, thumbWalls = thumbWalls)
        val placeHolders = keyMatrix.createPlaceholders()

        saveModel(cfg, "placeHolder.stl", keyMatrix.createPlaceHolder())

        result.addAll(connections.vertexHolders)
        result.addAll(borders.vertexHolders)
        result.addAll(placeHolders.vertexHolders)

        val matrix = placeHolders.model.addModel(connections.model.addModel(borders.model))

        saveModel(cfg, "matrix_right.stl", matrix)

        val delta = System.currentTimeMillis() - startTime
        println("createMatrix : $delta")
        return result
    }

    private fun createCase(
        cfg: KeyboardConfig,
        keyPlace: KeyPlace,
        thumbKeyPlace: ThumbKeyPlace,
        screwWallPlaces: ScrewWallPlaces,
        walls: Walls,
        controllerPlace: ControllerPlace,
        switcherPlace: SwitcherPlace,
        controllerFactory: ControllerFactory,
        switcherFactory: SwitcherFactory,
        controller: Controller,
        trackball: Trackball,
        thumbBorders: ThumbBorders,
        thumbWalls: ThumbWalls,
    ): List<VertexHolder> {
        val result = mutableListOf<VertexHolder>()

        val startTime = System.currentTimeMillis()

        var tbHolder: Abstract3dModel? = null
        val caseWalls = createCaseModel(
            cfg = cfg,
            keyPlace = keyPlace,
            thumbKeyPlace = thumbKeyPlace,
            screwWallPlaces = screwWallPlaces,
            walls = walls,
            controllerPlace = controllerPlace,
            switcherPlace = switcherPlace,
            controllerFactory = controllerFactory,
            switcherFactory = switcherFactory,
            controller = controller,
            thumbBorders = thumbBorders,
            thumbWalls = thumbWalls,
            trackball = trackball,
        )
        if (cfg.trackball.mode != TrackballMode.None) {
            val trackBallHolder = Trackball(cfg, keyPlace).createTrackballHolder()
            tbHolder = trackBallHolder.model
            result.addAll(trackBallHolder.vertexHolders)

        }

        result.addAll(caseWalls.vertexHolders)
        val resultCase = if (tbHolder != null) caseWalls.model.addModel(tbHolder) else caseWalls.model
        saveModel(cfg, "case.stl", resultCase)
        val delta = System.currentTimeMillis() - startTime
        println("createCase : $delta")
        return result
    }

    private fun createKeyCaps(
        cfg: KeyboardConfig, keyPlace: KeyPlace, thumbKeyPlace: ThumbKeyPlace,
    ): List<VertexHolder> {
        val result = mutableListOf<VertexHolder>()
        val startTime = System.currentTimeMillis()
        val keyCap = KeyCaps(cfg)
        result.addAll(
            createThumbKeyPlaceModel(
                cfg, keyCap.create().model, thumbKeyPlace, Color.BLUE
            ).vertexHolders
        )
        result.addAll(createKeycapsModel(cfg, keyCap.create().model, keyPlace, Color.PINK).vertexHolders)
        val delta = System.currentTimeMillis() - startTime
        println("createKeyCaps : $delta")
        return result
    }

    private fun amoebaHoles(cfg: KeyboardConfig, keyPlace: KeyPlace, thumbKeyPlace: ThumbKeyPlace): Abstract3dModel {
        val models = mutableListOf<Abstract3dModel>()
        val amoeba = Amoeba(cfg)
        val hole = amoeba.createHoles(height = 7.0, diameter = 0.7).addModel(amoeba.createSimple())

        models.add(createThumbKeyPlaceModel(cfg, hole, thumbKeyPlace, Color.BLUE).model)
        models.add(createKeycapsModel(cfg, hole, keyPlace, Color.PINK).model)
        return Union(models)
    }

    private fun createWristRest(cfg: KeyboardConfig): List<VertexHolder> {
        val result = mutableListOf<VertexHolder>()
        val startTime = System.currentTimeMillis()
        val wristRest = createWristRestModel(cfg)
        result.addAll(wristRest.vertexHolders)
        val delta = System.currentTimeMillis() - startTime
        println("createWristRest : $delta")
        return result
    }

    private fun createTrackballHolder(
        cfg: KeyboardConfig, trackball: Trackball,
    ): List<VertexHolder> {
        val result = mutableListOf<VertexHolder>()
        val startTime = System.currentTimeMillis()

        val trackBallModelHolder = trackball.create()
        result.addAll(trackBallModelHolder.vertexHolders)
        saveModel(cfg, "trackball.stl", trackBallModelHolder.model)

        val delta = System.currentTimeMillis() - startTime
        println("createTrackball : $delta")
        return result
    }

    private fun createTrackBall(cfg: KeyboardConfig, keyPlace: KeyPlace): List<VertexHolder> {
        val trackball = Trackball(cfg, keyPlace)
        return listOf(trackball.createTrackBall())
    }

    private fun createTrackballSensorCap(cfg: KeyboardConfig, keyPlace: KeyPlace): List<VertexHolder> {
        val trackball = Trackball(cfg, keyPlace)
        val sensorCap = trackball.createSensorCap()
        saveModel(cfg, "trackballCap.stl", sensorCap.model)
        return sensorCap.vertexHolders
    }

    private fun createTrackballSensor(cfg: KeyboardConfig, keyPlace: KeyPlace): List<VertexHolder> {
        val trackball = Trackball(cfg, keyPlace)
        return trackball.createTrackballSensor().vertexHolders
    }

    private fun createController(
        controllerFactory: ControllerFactory,
        controllerPlace: ControllerPlace,
    ): List<VertexHolder> {
        val result = mutableListOf<VertexHolder>()
        val startTime = System.currentTimeMillis()
        val controller = controllerFactory.createController()
        val controllerModel = controller.create(controllerPlace)
        result.addAll(controllerModel.vertexHolders)
        val delta = System.currentTimeMillis() - startTime
        println("createController : $delta")
        return result
    }

    private fun createControllerHolder(
        cfg: KeyboardConfig, controllerPlace: ControllerPlace,
        controller: Controller,
        switcherPlace: SwitcherPlace,
        controllerHolderDimensions: ControllerHolderDimensions,
        screwWallPlaces: ScrewWallPlaces,
    ): List<VertexHolder> {
        val result = mutableListOf<VertexHolder>()
        val startTime = System.currentTimeMillis()

        val switcherFactory = SwitcherFactory(cfg)
        val batteryFactory = BatteryFactory(cfg)

        val controllerHolder = ControllerHolderBuilder(
            cfg = cfg,
            controller = controller,
            controllerPlace = controllerPlace,
            controllerHolderDimensions = controllerHolderDimensions,
            screwWallPlaces = screwWallPlaces,
            switcherPlace = switcherPlace,
            switcher = switcherFactory.createSwitcher(),
            battery = batteryFactory.create(),
        ).create(showPreview = true)
        result.addAll(controllerHolder.vertexHolders)
        saveModel(cfg, "controller_holder.stl", controllerHolder.model)

        val delta = System.currentTimeMillis() - startTime
        println("createControllerHolder : $delta")
        return result
    }

    private fun createPlate(
        cfg: KeyboardConfig,
        keyPlace: KeyPlace,
        thumbKeyPlace: ThumbKeyPlace,
        wallsSettings: WallsSettings,
        screwWallPlaces: ScrewWallPlaces,
    ): List<VertexHolder> {
        val result = mutableListOf<VertexHolder>()
        val startTime = System.currentTimeMillis()

        val bottomPoints = BottomPoints(cfg, keyPlace, thumbKeyPlace, wallsSettings)
        val plate = PlateFactory(cfg, bottomPoints, screwWallPlaces, ScrewBase(cfg)).create()

        result.addAll(plate.vertexHolders)
        saveModel(cfg, "plate.stl", plate.model)

        val delta = System.currentTimeMillis() - startTime
        println("createPlate : $delta")
        return result
    }

    private fun createAmoeba(
        cfg: KeyboardConfig, keyPlace: KeyPlace, thumbKeyPlace: ThumbKeyPlace
    ): List<VertexHolder> {
        val settings = cfg.visibleKeyboardParts
        val result = mutableListOf<VertexHolder>()
        val startTime = System.currentTimeMillis()

        val amoeba = Amoeba(cfg).create()

        result.addAll(createThumbKeyPlaceModel(cfg, amoeba, thumbKeyPlace, Color.GREEN).vertexHolders)
        result.addAll(createKeycapsModel(cfg, amoeba, keyPlace, Color.green).vertexHolders)

        val delta = System.currentTimeMillis() - startTime
        println("createAmoeba : $delta")
        return result
    }

    private fun saveModel(cfg: KeyboardConfig, name: String, model: Abstract3dModel, needCheck: Boolean = false) {
        val outDir = File("stl")
        if (!outDir.exists()) {
            outDir.mkdirs()
        }

        Thread {
            try {
                val context: FacetGenerationContext = ColorFacetGenerationContext(DEFAULT_COLOR)
                context.setFn(cfg.stlFn)
                println("Start stl exporting")
                StlExporter.saveStl(
                    model.toCSG(context).polygons, File(outDir, name).absolutePath
                )
                println("End stl exporting")
            } catch (e: IOException) {
                println("Error while stl exporting" + e.message)
                throw RuntimeException(e)
            }
        }.start()
    }

    private fun createThumbKeyPlaceModel(
        cfg: KeyboardConfig, model: Abstract3dModel, thumbKeyPlace: ThumbKeyPlace, color: Color
    ): ModelHolder {
        val placedModel = thumbKeyPlace.thumbPlace(model)
        return ModelHolder(placedModel, createVertexHolder(cfg, placedModel, color))
    }

    private fun wristRestMount(): Abstract3dModel {
        // left back
        return Cylinder(42.0, 6.0).move(-56.0, -88.0, -2.0).addModel( // left front
            Cylinder(56.0, 6.0).move(-53.0, -142.0, -4.0)
        ).addModel( // right back
            Cylinder(48.0, 6.0).move(60.0, -85.0, -6.0)
        ).addModel( // right front
            Cylinder(62.0, 6.0).move(53.0, -140.0, -6.0)
        )
    }

    private fun keyPlaceBottomWalls(cfg: KeyboardConfig, keyPlace: KeyPlace): Abstract3dModel {
        return KeyHolderBottomWalls(cfg.keyPlaceConfig, keyPlace).build()
    }

    private fun createKeycapsModel(
        cfg: KeyboardConfig, model: Abstract3dModel, keyPlace: KeyPlace, color: Color
    ): ModelHolder {
        val models = mutableListOf<Abstract3dModel>()
        for (column in 0 until cfg.keyPlaceConfig.columnsCount) {
            for (row in 0 until cfg.keyPlaceConfig.rowsCount) {
                models.add(keyPlace.place(column, row, model))
            }
        }
        val result = Union(color, models)
        return ModelHolder(result, fromModel(result, color, 20))
    }

    private fun createCaseModel(
        cfg: KeyboardConfig,
        keyPlace: KeyPlace,
        thumbKeyPlace: ThumbKeyPlace,
        screwWallPlaces: ScrewWallPlaces,
        walls: Walls,
        controllerPlace: ControllerPlace,
        switcherPlace: SwitcherPlace,
        controllerFactory: ControllerFactory,
        switcherFactory: SwitcherFactory,
        controller: Controller,
        trackball: Trackball,
        thumbBorders: ThumbBorders,
        thumbWalls: ThumbWalls,
    ): ModelHolder {
        val holeVerticalExtra = -3.0

        val borderHeight = 5.0
        val borderVerticalOffset = 5.0

        val wallsSettings = WallsSettings(
            verticalOffset = borderVerticalOffset,
            borderHeight = borderHeight,
            bottomBorderHeight = if (cfg.isSkeletonMode) 4.0 else 2.0
        )

        val holeBorderThikness = 2.0
        val holeBorderHeight = 3.0
        val holeBordersModels = Walls(
            cfg,
            wallsSettings,
            keyPlace,
            thumbKeyPlace,
            topEdgeOffsetZ = holeVerticalExtra / 2,
            thumbBorders = thumbBorders,
            thumbWalls = thumbWalls,
        ).createBorders(
            borderThickness = holeBorderThikness, borderHeight = holeBorderHeight
        )
        val trackballHole = trackball.createTrackballWireHole()
        val holeBorders = Union(holeBordersModels).moveZ((holeBorderHeight - 2.0) / 2.0 + 0.3)
        val usbPortHole =
            controllerPlace.place(controller.placeUsbPort(controllerFactory.createUsbPortHole())).moveY(-2.0)
        val switcherHole = switcherPlace.place(switcherFactory.createSwitcher().createSwitcherHole())
        val usbPortHoleCase =
            controllerPlace.place(controller.placeUsbPort(controllerFactory.createUsbPortCase())).moveY(-1.0)

        val screwBase = ScrewBase(cfg)
        val matrixWallScrewHolder = ScrewsMatrixHolder(cfg, screwBase).create()
        val matrixWallNutHole = ScrewsMatrixHolder(cfg, screwBase).createNutHole()
        val screwKeyMatrixPlace = ScrewKeyMatrixPlace(cfg, keyPlace, thumbKeyPlace)
        val screwMatrixHoldersHoles = screwKeyMatrixPlace.place(matrixWallNutHole)
        val screwMatrixHolders = screwKeyMatrixPlace.place(matrixWallScrewHolder).subtractModel(holeBorders)
            .subtractModel(screwMatrixHoldersHoles)

        //
        val wallScrews = placeWallScrews(screwBase.screwHolder(), screwWallPlaces)

        val bottomEdgeHeight = if (cfg.isSkeletonMode) 4.0 else 2.0

        val wallsModels = walls.createWalls(
            bottomBorderHeight = bottomEdgeHeight
        )

        val wallsModel =
            Union(wallsModels).subtractModel(holeBorders).subtractModel(switcherHole).subtractModel(usbPortHole)
                .addModel(usbPortHoleCase).subtractModel(Cube(300.0, 300.0, 50.0).move(0.0, 0.0, -25.0))

        var wallModelsWithHoles =
            Union(wallsModels).subtractModel(holeBorders).subtractModel(usbPortHole).subtractModel(switcherHole)
        if (cfg.trackball.mode == TrackballMode.Back) {
            wallModelsWithHoles = wallModelsWithHoles.subtractModel(trackballHole)
        }

        return ModelHolder(
            cfg,
            wallsModel.addModel(screwMatrixHolders).addModel(wallScrews).subtractModel(screwMatrixHoldersHoles),
            wallModelsWithHoles, // for color wall debug place wallsModels
            wallScrews.withColor(Color.yellow),
            //usbPortHole.withColor(Color.PURPLE),
            usbPortHoleCase.withColor(Color.GREEN),
            //trackballHole.withColor(Color.CHOCOLATE),
            //switcherHole.withColor(Color.AQUA_MARINE),
            //holeBorders.withColor(Color.PURPLE),
            //createVertexHolder(holeBorders, Color.PINK),
            screwMatrixHolders.subtractModel(Cube(300.0, 300.0, 50.0).move(0.0, 0.0, -25.0)).withColor(Color.CYAN),
        )
    }

    private fun placeWallScrews(
        screwHolder: Abstract3dModel,
        screwWallPlaces: ScrewWallPlaces,
    ): Abstract3dModel {

        val holderHeight = 2.2
        val screwWallHolderBack = Cube(6.0, 4.0, holderHeight).move(0.0, 4.0, holderHeight / 2)
        val screwWallHolderSide = Cube(4.0, 6.0, holderHeight).move(-4.0, 0.0, holderHeight / 2)
        val controllerScrewsHolderBack = screwWallPlaces.placeControllerScrews(
            screwWallHolderBack,
            heightMode = ScrewWallPlaces.HeightMode.Walls,
            mode = ScrewWallPlaces.ControllerMode.Back
        )

        val controllerScrewsHolderSide = screwWallPlaces.placeControllerScrews(
            screwWallHolderSide,
            heightMode = ScrewWallPlaces.HeightMode.Walls,
            mode = ScrewWallPlaces.ControllerMode.Side
        )

        return screwWallPlaces.place(screwHolder, heightMode = ScrewWallPlaces.HeightMode.Walls)
            .addModel(controllerScrewsHolderBack).addModel(controllerScrewsHolderSide)
    }

    private fun createWristRestModel(cfg: KeyboardConfig): ModelHolder {
        val wristRest = WristRest.build().subtractModel(Cube(300.0, 300.0, 50.0).move(0.0, 0.0, -25.0))

        val vertexHolders = mutableListOf<VertexHolder>()
        vertexHolders.add(
            createVertexHolder(
                cfg, wristRest // .subtractModel(keyPlaceHoles(-4))
                , Color.ORANGE
            )
        )
        //vertexHolders.addAll(createControlPoints(pointsController.controlPoints))

        return ModelHolder(wristRest, vertexHolders)
    }

    private fun createControlPoints(cfg: KeyboardConfig, points: Array<Array<V3d>>): List<VertexHolder> {
        val result = mutableListOf<VertexHolder>()
        val colors = arrayOf(Color.RED, Color.BLUE, Color.GREEN, Color.CYAN, Color.MAGENTA, Color.ORANGE)
        for (rowIndex in points.indices) {
            for (colIndex in points[rowIndex].indices) {
                result.add(
                    createVertexHolder(
                        cfg, Utils.sphere(2.0).move(points[rowIndex][colIndex]), colors[colIndex % colors.size]
                    )
                )
            }
        }
        return result
    }

    private fun createVertexHolder(cfg: KeyboardConfig, model: IModel) {
        createVertexHolder(cfg, model, DEFAULT_COLOR)
    }

    private fun createVertexHolder(cfg: KeyboardConfig, model: IModel, color: Color): VertexHolder {
        return fromModel(model, color, cfg.fn)
    }

    companion object {

        private val DEFAULT_COLOR = Color.GRAY
    }
}
