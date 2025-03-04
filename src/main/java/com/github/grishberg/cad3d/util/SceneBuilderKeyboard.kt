package com.github.grishberg.cad3d.util

import com.github.grishberg.cad3d.keyboard.*
import com.github.grishberg.cad3d.keyboard.cfg.KeyboardConfig
import com.github.grishberg.cad3d.keyboard.screws.ScrewBase
import com.github.grishberg.cad3d.keyboard.screws.ScrewKeyMatrixPlace
import com.github.grishberg.cad3d.keyboard.screws.ScrewWallPlaces
import com.github.grishberg.cad3d.keyboard.screws.ScrewsMatrixHolder
import com.github.grishberg.cad3d.keyboard.walls.Walls
import com.github.grishberg.cad3d.keyboard.wristrest.WristRest
import com.github.grishberg.cad3d.util.SceneBuilder.ReadyListener
import eu.printingin3d.javascad.coords.V3d
import eu.printingin3d.javascad.models.Abstract3dModel
import eu.printingin3d.javascad.models.Cube
import eu.printingin3d.javascad.models.Cylinder
import eu.printingin3d.javascad.models.IModel
import eu.printingin3d.javascad.models.surfaces.SmoothSurface
import eu.printingin3d.javascad.models.surfaces.bicubic.BicubicSurfaceSpline
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

class SceneBuilderKeyboard(
    private val initialConfig: KeyboardConfig,
    private val pointsController: ControlPointsController,
) : SceneBuilder {

    @Volatile
    private var cfg: KeyboardConfig = initialConfig

    private var resolution = 15 // Количество промежуточных точек между заданными точками
    val buffers: MutableList<VertexHolder>
    private var listener: ReadyListener? = null
    private val executor: Executor = Executors.newSingleThreadExecutor()

    init {
        buffers = ArrayList()
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
        executor.execute {
            val keyPlace = KeyPlace(cfg)
            val thumbKeyPlace = ThumbKeyPlace(cfg.thumbClusterSettings)

            buffers.clear()
            val settings = cfg.assemblySettings

            if (settings.settingsShowMatrix) {
                val connections = createConnections(keyPlace, thumbKeyPlace)
                val borders = createBorders(keyPlace, thumbKeyPlace)
                val matrix = connections.addModel(borders).addModel(createPlaceholders(keyPlace, thumbKeyPlace))

                saveModel("matrix.stl", matrix)
            }
            if (settings.settingsShowCaps) {
                createThumbKeyPlace(thumbKeyPlace)
                createKeycaps(keyPlace)
            }

            if (settings.settingsShowPlate) {
                //TODO generate plate
            }

            if (settings.settingsShowCase) {
                val caseWalls = createCase(keyPlace, thumbKeyPlace)
                saveModel("case.stl", caseWalls)
            }

            if (settings.settingsShowWristRest) {
                createWristRest()
            }
            SwingUtilities.invokeLater {
                if (listener != null) {
                    listener!!.onReady(buffers)
                }
            }
        }
    }

    private fun saveModel(name: String, model: Abstract3dModel, fn: Int = 20) {
        val outDir = File("stl")
        if (!outDir.exists()) {
            outDir.mkdirs()
        }

        Thread {
            try {
                val context: FacetGenerationContext = ColorFacetGenerationContext(DEFAULT_COLOR)
                context.setFn(fn)
                StlExporter.saveStringToFile(
                    model.toCSG(context).verticesAndColorsAsFloatArray, File(outDir, name).absolutePath
                )
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }.start()
    }

    private fun createThumbKeyPlace(thumbKeyPlace: ThumbKeyPlace) {
        val keycap = Cube(
            cfg.keyswitchWidth, cfg.keyswitchHeight, cfg.saProfileKeyHeight
        ).move(0.0, 0.0, 10.0)
        createAndAdd(thumbKeyPlace.thumbPlace(keycap), Color.BLUE)
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

    private fun createPlaceholders(keyPlace: KeyPlace, thumbKeyPlace: ThumbKeyPlace): Abstract3dModel {
        val models = mutableListOf<Abstract3dModel>()
        for (column in 0 until cfg.columnsCount) {
            for (row in 0 until cfg.rowsCount) {
                models.add(keyPlace.place(column, row, KeyPlaceholder.placeHolder()))
            }
        }

        models.add(thumbKeyPlace.thumbPlace(KeyPlaceholder.placeHolder()))

        val allPlaceholders = Union(models)
        createAndAdd(allPlaceholders, Color(30, 127, 40))
        return allPlaceholders
    }

    private fun createKeycaps(keyPlace: KeyPlace) {
        for (column in 0 until cfg.columnsCount) {
            for (row in 0 until cfg.rowsCount) {
                val obj = Cube(
                    cfg.keyswitchWidth, cfg.keyswitchHeight, cfg.saProfileKeyHeight
                ).move(0.0, 0.0, 10.0)
                createAndAdd(keyPlace.place(column, row, obj), Color.PINK)
            }
        }
    }

    private fun createConnections(keyPlace: KeyPlace, thumbKeyPlace: ThumbKeyPlace): Abstract3dModel {
        val connections = Connections(cfg, keyPlace).buildConnections()
        createAndAdd(connections, DEFAULT_COLOR)
        val thumbPlaceConnections = ThumbConnections(thumbKeyPlace).buildThumbPlaceConnections()
        createAndAdd(
            thumbPlaceConnections, DEFAULT_COLOR
        )
        return connections.addModel(thumbPlaceConnections)
    }

    private fun createBorders(keyPlace: KeyPlace, thumbKeyPlace: ThumbKeyPlace): Abstract3dModel {
        val borders = Walls(cfg, keyPlace, thumbKeyPlace).createBorders(1.5, 4.0)

        val screwBase = ScrewBase(cfg)

        val screws = ScrewKeyMatrixPlace(cfg, keyPlace, thumbKeyPlace).place(screwBase.matrixScrewHole())
        createAndAdd(borders.subtractModel(screws), Color.lightGray, 30)
        return borders
    }

    private fun createCase(keyPlace: KeyPlace, thumbKeyPlace: ThumbKeyPlace): Abstract3dModel {
        val holeBorders = Walls(cfg, keyPlace, thumbKeyPlace).createBorders(1.7, 6.0)

        val screwBase = ScrewBase(cfg)
        val screwHolder = ScrewsMatrixHolder(cfg, screwBase).create()
        val screwMatrixHolders = ScrewKeyMatrixPlace(cfg, keyPlace, thumbKeyPlace)
            .place(screwHolder)
            .subtractModel(holeBorders)

        createAndAdd(screwMatrixHolders, Color.CYAN, cfg.fn)
        createAndAdd(placeWallScrews(keyPlace, thumbKeyPlace), Color.YELLOW, 20)

        val walls = Walls(cfg, keyPlace, thumbKeyPlace).createWalls(1.5, 7.5).subtractModel(holeBorders)
            .subtractModel(Cube(300.0, 300.0, 50.0).move(0.0, 0.0, -25.0))
        createAndAdd(walls, Color.gray, 30)
        return walls
    }

    private fun placeWallScrews(keyPlace: KeyPlace, thumbKeyPlace: ThumbKeyPlace): Abstract3dModel {
        val screwWallPlaces = ScrewWallPlaces(cfg, keyPlace, thumbKeyPlace)
        val screwBase = ScrewBase(cfg)
        val screwHolder = screwBase.screwHolder()

        return screwWallPlaces.place(screwHolder)
    }

    private fun createWristRest() {
        showControlPoints(pointsController.controlPoints)

        // Задаем параметры поверхности
        val thickness = 4.0 // Толщина поверхности/*
        val topSurface = SmoothSurface(
            BicubicSurfaceSpline.bSplineSurface(pointsController.controlPoints, resolution), thickness
        );

        val wristRest = WristRest.build().subtractModel(Cube(300.0, 300.0, 50.0).move(0.0, 0.0, -25.0))
        createAndAdd(
            wristRest // .subtractModel(keyPlaceHoles(-4))
            , Color.ORANGE, 30
        )
        Thread {
            try {
                val context: FacetGenerationContext = ColorFacetGenerationContext(DEFAULT_COLOR)
                context.setFn(20)
                StlExporter.saveStringToFile(
                    wristRest.toCSG(context).verticesAndColorsAsFloatArray, "out.stl"
                )
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }.start()
    }

    private fun showControlPoints(points: Array<Array<V3d>>) {
        val colors = arrayOf(Color.RED, Color.BLUE, Color.GREEN, Color.CYAN, Color.MAGENTA, Color.ORANGE)
        for (rowIndex in points.indices) {
            for (colIndex in points[rowIndex].indices) {
                createAndAdd(
                    Utils.sphere(2.0).move(points[rowIndex][colIndex]), colors[colIndex % colors.size]
                )
            }
        }
    }

    private fun createAndAdd(model: IModel) {
        createAndAdd(model, DEFAULT_COLOR)
    }

    private fun createAndAdd(model: IModel, color: Color, fn: Int = 6): VertexHolder {
        val context: FacetGenerationContext = ColorFacetGenerationContext(color)
        context.setFn(fn)
        val csg = model.toCSG(context)
        val vertex = csg.verticesAndColorsAsFloatArray
        buffers.add(vertex)
        return vertex
    }

    companion object {

        private val DEFAULT_COLOR = Color.GRAY
    }
}
