package com.github.grishberg.cad3d.util

import com.github.grishberg.cad3d.keyboard.Connections
import com.github.grishberg.cad3d.keyboard.ControlPointsController
import com.github.grishberg.cad3d.keyboard.KeyHolderBottomWalls
import com.github.grishberg.cad3d.keyboard.KeyPlace
import com.github.grishberg.cad3d.keyboard.KeyPlaceHoles
import com.github.grishberg.cad3d.keyboard.KeyPlaceholder
import com.github.grishberg.cad3d.keyboard.KeySwitchHoles
import com.github.grishberg.cad3d.keyboard.ModelHolder
import com.github.grishberg.cad3d.keyboard.ThumbConnections
import com.github.grishberg.cad3d.keyboard.ThumbKeyPlace
import com.github.grishberg.cad3d.keyboard.Utils
import com.github.grishberg.cad3d.keyboard.casebody.Walls
import com.github.grishberg.cad3d.keyboard.casebody.controllers.SuperMiniNRF52840
import com.github.grishberg.cad3d.keyboard.casebody.wall.ControllerHolderWall
import com.github.grishberg.cad3d.keyboard.cfg.KeyboardConfig
import com.github.grishberg.cad3d.keyboard.cfg.TrackballMode
import com.github.grishberg.cad3d.keyboard.cfg.WallsSettings
import com.github.grishberg.cad3d.keyboard.screws.ScrewBase
import com.github.grishberg.cad3d.keyboard.screws.ScrewKeyMatrixPlace
import com.github.grishberg.cad3d.keyboard.screws.ScrewWallPlaces
import com.github.grishberg.cad3d.keyboard.screws.ScrewsMatrixHolder
import com.github.grishberg.cad3d.keyboard.wristrest.WristRest
import com.github.grishberg.cad3d.trackball.Trackball
import com.github.grishberg.cad3d.util.SceneBuilder.ReadyListener
import eu.printingin3d.javascad.coords.V3d
import eu.printingin3d.javascad.models.Abstract3dModel
import eu.printingin3d.javascad.models.Cube
import eu.printingin3d.javascad.models.Cylinder
import eu.printingin3d.javascad.models.IModel
import eu.printingin3d.javascad.tranzitions.Union
import eu.printingin3d.javascad.utils.Color
import eu.printingin3d.javascad.utils.StlExporter
import eu.printingin3d.javascad.vrl.ColorFacetGenerationContext
import eu.printingin3d.javascad.vrl.FacetGenerationContext
import eu.printingin3d.javascad.vrl.VertexHolder
import java.io.File
import java.io.IOException
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import javax.swing.SwingUtilities
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

class SceneBuilderKeyboard(
    private val initialConfig: KeyboardConfig,
    private val pointsController: ControlPointsController,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default),
) : SceneBuilder {

    @Volatile private var cfg: KeyboardConfig = initialConfig

    private var resolution = 15 // Количество промежуточных точек между заданными точками
    private var listener: ReadyListener? = null
    private val executor: Executor = Executors.newSingleThreadExecutor()

    init {
        pointsController.addListener { row: Int, col: Int -> rebuildCaseAndInvalidate() }
    }

    override fun setConfig(cfg: KeyboardConfig) {
        this.cfg = cfg
    }

    override fun setListener(listener: ReadyListener) {
        this.listener = listener
    }

    override fun requestBuffers() {
        if (resolution == 0) {
            resolution = 20
        }
        create3dModels()
    }

    private fun rebuildCaseAndInvalidate() {
        create3dModels()
    }

    private fun create3dModels() {
        val keyPlace = KeyPlace(cfg)
        val thumbKeyPlace = ThumbKeyPlace(cfg)

        coroutineScope.launch {
            val deferredResults = listOf(
                async { createMatrix(cfg, keyPlace, thumbKeyPlace) },
                async { createCase(cfg, keyPlace, thumbKeyPlace) },
                async { createKeyCaps(cfg, keyPlace, thumbKeyPlace) },
                async { createWristRest(cfg, keyPlace, thumbKeyPlace) },
                async { createTrackball(cfg, keyPlace, thumbKeyPlace) },
                async { createController(cfg, keyPlace, thumbKeyPlace) },
            )

            // Ожидаем завершения всех задач
            val allResults = deferredResults.awaitAll()

            // Объединяем результаты
            val buffers = mutableListOf<VertexHolder>()
            allResults.forEach { holders ->
                buffers.addAll(holders)
            }

            SwingUtilities.invokeLater {
                if (listener != null) {
                    listener!!.onReady(buffers)
                }
            }
        }
    }

    private fun createMatrix(
        cfg: KeyboardConfig, keyPlace: KeyPlace, thumbKeyPlace: ThumbKeyPlace
    ): List<VertexHolder> {
        val settings = cfg.assemblySettings
        val result = mutableListOf<VertexHolder>()

        val startTime = System.currentTimeMillis()
        if (settings.settingsShowMatrix) {
            val connections = createConnectionsModel(keyPlace, thumbKeyPlace)
            val borders = createBordersModel(keyPlace, thumbKeyPlace)
            val placeHolders = createPlaceholders(keyPlace, thumbKeyPlace)

            result.addAll(connections.vertexHolders)
            result.addAll(borders.vertexHolders)
            result.addAll(placeHolders.vertexHolders)

            val matrix = connections.model.addModel(borders.model).addModel(placeHolders.model)
            saveModel("matrix.stl", matrix)
        }
        val delta = System.currentTimeMillis() - startTime
        println("createMatrix : $delta")
        return result
    }

    private fun createCase(cfg: KeyboardConfig, keyPlace: KeyPlace, thumbKeyPlace: ThumbKeyPlace): List<VertexHolder> {
        val settings = cfg.assemblySettings
        val result = mutableListOf<VertexHolder>()

        val startTime = System.currentTimeMillis()

        var tbHolder: Abstract3dModel? = null
        if (settings.settingsShowCase) {
            val caseWalls = createCaseModel(cfg, keyPlace, thumbKeyPlace)
            if (cfg.trackball.mode != TrackballMode.None) {
                val trackBallHolder = Trackball(cfg, keyPlace).createTrackballHolder()
                tbHolder = trackBallHolder.model
                result.addAll(trackBallHolder.vertexHolders)

            }
            result.addAll(caseWalls.vertexHolders)
            val resultCase = if (tbHolder != null) caseWalls.model.addModel(tbHolder) else caseWalls.model
            saveModel("case.stl", resultCase)
        }
        val delta = System.currentTimeMillis() - startTime
        println("createCase : $delta")
        return result
    }

    private fun createKeyCaps(
        cfg: KeyboardConfig, keyPlace: KeyPlace, thumbKeyPlace: ThumbKeyPlace
    ): List<VertexHolder> {
        val settings = cfg.assemblySettings
        val result = mutableListOf<VertexHolder>()
        val startTime = System.currentTimeMillis()
        if (settings.settingsShowCaps) {
            result.addAll(createThumbKeyPlaceModel(thumbKeyPlace).vertexHolders)
            result.addAll(createKeycapsModel(keyPlace).vertexHolders)
        }
        val delta = System.currentTimeMillis() - startTime
        println("createKeyCaps : $delta")
        return result
    }

    private fun createWristRest(
        cfg: KeyboardConfig, keyPlace: KeyPlace, thumbKeyPlace: ThumbKeyPlace
    ): List<VertexHolder> {
        val settings = cfg.assemblySettings
        val result = mutableListOf<VertexHolder>()
        val startTime = System.currentTimeMillis()
        if (settings.settingsShowWristRest) {
            val wristRest = createWristRestModel()
            result.addAll(wristRest.vertexHolders)
        }
        val delta = System.currentTimeMillis() - startTime
        println("createWristRest : $delta")
        return result
    }

    private fun createTrackball(
        cfg: KeyboardConfig, keyPlace: KeyPlace, thumbKeyPlace: ThumbKeyPlace
    ): List<VertexHolder> {
        val settings = cfg.assemblySettings
        val result = mutableListOf<VertexHolder>()
        val startTime = System.currentTimeMillis()
        if (settings.settingsTrackball) {
            val trackBall = Trackball(cfg, keyPlace).create()
            result.addAll(trackBall.vertexHolders)
            saveModel("trackball.stl", trackBall.model)
        }
        val delta = System.currentTimeMillis() - startTime
        println("createTrackball : $delta")
        return result
    }

    private fun createController(
        cfg: KeyboardConfig, keyPlace: KeyPlace, thumbKeyPlace: ThumbKeyPlace
    ): List<VertexHolder> {
        val settings = cfg.assemblySettings
        val result = mutableListOf<VertexHolder>()
        val startTime = System.currentTimeMillis()
        if (settings.settingsController) {
            val trackBall = SuperMiniNRF52840(cfg, keyPlace).create()
            result.addAll(trackBall.vertexHolders)

        }
        val delta = System.currentTimeMillis() - startTime
        println("createTrackball : $delta")
        return result
    }

    private fun saveModel(name: String, model: Abstract3dModel, needCheck: Boolean = false) {
        val outDir = File("stl")
        if (!outDir.exists()) {
            outDir.mkdirs()
        }

        Thread {
            try {
                val context: FacetGenerationContext = ColorFacetGenerationContext(DEFAULT_COLOR)
                context.setFn(cfg.stlFn)
                StlExporter.saveStl(
                    model.toCSG(context).verticesAndColorsAsFloatArray, File(outDir, name).absolutePath, needCheck
                )
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }.start()
    }

    private fun createThumbKeyPlaceModel(thumbKeyPlace: ThumbKeyPlace): ModelHolder {
        val keycap = Cube(
            cfg.keyswitchWidth, cfg.keyswitchHeight, cfg.saProfileKeyHeight
        ).move(0.0, 0.0, 10.0)
        return ModelHolder(keycap, createVertexHolder(thumbKeyPlace.thumbPlace(keycap), Color.BLUE))
    }

    private fun keyHoles(keyPlace: KeyPlace): Abstract3dModel {
        return KeySwitchHoles(cfg, keyPlace).build()
    }

    private fun keyPlaceHoles(keyPlace: KeyPlace, offset: Double): Abstract3dModel {
        return KeyPlaceHoles(cfg, keyPlace).build(offset)
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

    private fun keyPlaceBottomWalls(keyPlace: KeyPlace): Abstract3dModel {
        return KeyHolderBottomWalls(cfg, keyPlace).build()
    }

    private fun createPlaceholders(keyPlace: KeyPlace, thumbKeyPlace: ThumbKeyPlace): ModelHolder {
        val models = mutableListOf<Abstract3dModel>()
        for (column in 0 until cfg.columnsCount) {
            for (row in 0 until cfg.rowsCount) {
                models.add(keyPlace.place(column, row, KeyPlaceholder.placeHolder()))
            }
        }

        models.add(thumbKeyPlace.thumbPlace(KeyPlaceholder.placeHolder()))

        val allPlaceholders = Union(models)

        return ModelHolder(allPlaceholders, createVertexHolder(allPlaceholders, Color(30, 127, 40)))
    }

    private fun createKeycapsModel(keyPlace: KeyPlace): ModelHolder {
        val models = mutableListOf<Abstract3dModel>()
        val vertexHolders = mutableListOf<VertexHolder>()

        for (column in 0 until cfg.columnsCount) {
            for (row in 0 until cfg.rowsCount) {
                val obj = Cube(
                    cfg.keyswitchWidth, cfg.keyswitchHeight, cfg.saProfileKeyHeight
                ).move(0.0, 0.0, 10.0)
                models.add(obj)
                vertexHolders.add(createVertexHolder(keyPlace.place(column, row, obj), Color.PINK))
            }
        }
        return ModelHolder(models.first(), vertexHolders)
    }

    private fun createConnectionsModel(keyPlace: KeyPlace, thumbKeyPlace: ThumbKeyPlace): ModelHolder {
        val connections = Connections(cfg, keyPlace).buildConnections()
        val thumbPlaceConnections = ThumbConnections(thumbKeyPlace).buildThumbPlaceConnections()

        return ModelHolder(
            connections.addModel(thumbPlaceConnections),
            createVertexHolder(connections, DEFAULT_COLOR),
            createVertexHolder(thumbPlaceConnections, DEFAULT_COLOR)
        )
    }

    private fun createBordersModel(keyPlace: KeyPlace, thumbKeyPlace: ThumbKeyPlace): ModelHolder {
        val screwBase = ScrewBase(cfg)
        val screws = ScrewKeyMatrixPlace(cfg, keyPlace, thumbKeyPlace).place(screwBase.matrixScrewHole())

        val wallsSettings = WallsSettings(
            borderHeight = 4.0, bottomBorderHeight = 4.0

        )
        val borders = Walls(cfg, wallsSettings, keyPlace, thumbKeyPlace, topEdgeOffsetZ = 0.0).createBorders(1.5, 4.0)
            .subtractModel(screws)


        return ModelHolder(borders, createVertexHolder(borders, Color.lightGray))
    }

    private fun createCaseModel(cfg: KeyboardConfig, keyPlace: KeyPlace, thumbKeyPlace: ThumbKeyPlace): ModelHolder {
        val holeVerticalExtra = 2.0

        val wallsSettings = WallsSettings(
            borderHeight = 4.0, bottomBorderHeight = if (cfg.isSkeletonMode) 4.0 else 2.0
        )

        val holeBorders = Walls(
            this.cfg, wallsSettings, keyPlace, thumbKeyPlace, topEdgeOffsetZ = holeVerticalExtra / 2
        ).createBorders(
            1.7, 4.0 + holeVerticalExtra
        )

        val screwBase = ScrewBase(this.cfg)
        val matrixWallScrewHolder = ScrewsMatrixHolder(this.cfg, screwBase).create()
        val matrixWallNutHole = ScrewsMatrixHolder(this.cfg, screwBase).createNutHole()
        val screwKeyMatrixPlace = ScrewKeyMatrixPlace(this.cfg, keyPlace, thumbKeyPlace)
        val screwMatrixHoldersHoles = screwKeyMatrixPlace.place(matrixWallNutHole)
        val screwMatrixHolders = screwKeyMatrixPlace.place(matrixWallScrewHolder).subtractModel(holeBorders)
            .subtractModel(screwMatrixHoldersHoles)

        //
        val wallScrews = placeWallScrews(keyPlace, thumbKeyPlace, screwBase.screwHolder())

        val topBorderHeight = 6.0
        val topEdgeOffsetZ = -topBorderHeight / 2
        val bottomEdgeHeight = if (cfg.isSkeletonMode) 4.0 else 2.0

        val walls =
            Walls(this.cfg, wallsSettings, keyPlace, thumbKeyPlace, topEdgeOffsetZ = topEdgeOffsetZ).createWalls(
                bottomBorderHeight = bottomEdgeHeight
            ).subtractModel(holeBorders).subtractModel(Cube(300.0, 300.0, 50.0).move(0.0, 0.0, -25.0))

        return ModelHolder(
            walls.addModel(screwMatrixHolders).addModel(wallScrews).subtractModel(screwMatrixHoldersHoles),
            createVertexHolder(walls, Color.gray),
            createVertexHolder(wallScrews, Color.yellow),
            createVertexHolder(
                screwMatrixHolders.subtractModel(Cube(300.0, 300.0, 50.0).move(0.0, 0.0, -25.0)), Color.CYAN
            )

        )
    }

    private fun placeWallScrews(
        keyPlace: KeyPlace,
        thumbKeyPlace: ThumbKeyPlace,
        screwHolder: Abstract3dModel,
    ): Abstract3dModel {

        val wallsSettings = WallsSettings(bottomBorderHeight = 4.0)
        val controllerHolderWall = ControllerHolderWall(wallsSettings, keyPlace)
        val screwWallPlaces = ScrewWallPlaces(cfg, wallsSettings, keyPlace, thumbKeyPlace, controllerHolderWall)

        val holderHeight = 2.2
        val screwWallHolder = Cube(6.0, 4.0, holderHeight).move(0.0, 4.0, holderHeight / 2)
        val controllerScrewsHolder =
            screwWallPlaces.placeControllerScrews(screwWallHolder, mode = ScrewWallPlaces.Mode.Walls)
        return screwWallPlaces.place(screwHolder, mode = ScrewWallPlaces.Mode.Walls).addModel(controllerScrewsHolder)
    }

    private fun createWristRestModel(): ModelHolder {
        val wristRest = WristRest.build().subtractModel(Cube(300.0, 300.0, 50.0).move(0.0, 0.0, -25.0))

        val vertexHolders = mutableListOf<VertexHolder>()
        vertexHolders.add(
            createVertexHolder(
                wristRest // .subtractModel(keyPlaceHoles(-4))
                , Color.ORANGE
            )
        )
        vertexHolders.addAll(createControlPoints(pointsController.controlPoints))

        return ModelHolder(wristRest, vertexHolders)
    }

    private fun createControlPoints(points: Array<Array<V3d>>): List<VertexHolder> {
        val result = mutableListOf<VertexHolder>()
        val colors = arrayOf(Color.RED, Color.BLUE, Color.GREEN, Color.CYAN, Color.MAGENTA, Color.ORANGE)
        for (rowIndex in points.indices) {
            for (colIndex in points[rowIndex].indices) {
                result.add(
                    createVertexHolder(
                        Utils.sphere(2.0).move(points[rowIndex][colIndex]), colors[colIndex % colors.size]
                    )
                )
            }
        }
        return result
    }

    private fun createVertexHolder(model: IModel) {
        createVertexHolder(model, DEFAULT_COLOR)
    }

    private fun createVertexHolder(model: IModel, color: Color): VertexHolder {
        return VertexHolder.createVertexHolder(model, color, cfg.fn)
    }

    companion object {

        private val DEFAULT_COLOR = Color.GRAY
    }
}
