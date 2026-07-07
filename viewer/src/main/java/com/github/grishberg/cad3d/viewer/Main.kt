package com.github.grishberg.cad3d.viewer

import com.github.grishberg.cad3d.config.SceneConfigParser
import com.github.grishberg.cad3d.config.SyntaxColors
import com.github.grishberg.cad3d.pccase.PcCaseModelFactory
import com.github.grishberg.cad3d.pccase.SceneConfig
import com.github.grishberg.cad3d.pccase.TransformOp
import com.jogamp.opengl.GL2
import com.jogamp.opengl.GLAutoDrawable
import com.jogamp.opengl.GLCapabilities
import com.jogamp.opengl.GLEventListener
import com.jogamp.opengl.GLProfile
import com.jogamp.opengl.awt.GLCanvas
import com.jogamp.opengl.fixedfunc.GLLightingFunc
import com.jogamp.opengl.glu.GLU
import com.jogamp.opengl.util.texture.Texture
import com.jogamp.opengl.util.texture.TextureIO
import com.jogamp.opengl.util.Animator
import eu.printingin3d.javascad.vrl.CSG
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea
import org.fife.ui.rtextarea.RTextScrollPane
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Font
import java.awt.event.InputEvent
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseWheelEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.io.File
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JSplitPane

data class ModelData(
    val vertex: FloatArray, val normals: FloatArray, val verticesCount: Int
)

class Main(title: String?) : JFrame(title), GLEventListener {

    private val animator = Animator()
    private val glu = GLU()
    private var glCanvas: GLCanvas? = null
    private var prevMouseX = 0
    private var prevMouseY = 0

    private var camPitch = -90f
    private var camYaw = 0f
    private var camDist = -600f
    private var panX = 0f
    private var panY = 0f

    companion object {
        private const val PAN_SENSITIVITY = 0.5f
        private const val VIEW_CONF_FILE = "scene-view.conf"
    }

    private var mbTexture: Texture? = null
    private var mbTextureBottom: Texture? = null

    private val allModels = mutableMapOf<String, ModelData>()
    private val visibleModels = mutableMapOf<String, ModelData>()

    private var currentScript: String = loadScriptFromFile()
    private var currentConfig: SceneConfig = SceneConfig.DEFAULT
    private val parser = SceneConfigParser()

    private var mbOffsetX = PcCaseModelFactory.MB_OFFSET_X.toFloat()
    private var mbOffsetY = PcCaseModelFactory.MB_OFFSET_Y.toFloat()
    private var mbOffsetZ = PcCaseModelFactory.MB_OFFSET_Z.toFloat()

    private val checkboxBoxes = mutableMapOf<String, JCheckBox>()
    private var checkboxPanel: JPanel = JPanel()

    init {
        // Log all uncaught EDT exceptions to diagnose text-disappearance bug
        Thread.setDefaultUncaughtExceptionHandler { thread, ex ->
            System.err.println("[EDT] Uncaught exception on " + thread.name + ":")
            ex.printStackTrace()
        }
        loadViewConf()
        title ?: "PC Case Viewer (OpenGL)"

        val parsedConfig = parser.parse(currentScript).getOrNull()
        if (parsedConfig != null) {
            currentConfig = parsedConfig
        }
        buildWithConfig(currentConfig)

        val glProfile = GLProfile.get(GLProfile.GL2)
        val glCapabilities = GLCapabilities(glProfile)
        glCapabilities.depthBits = 24

        glCanvas = GLCanvas(glCapabilities)
        glCanvas!!.addGLEventListener(this)
        val ml = GlCanvasMouseListener()
        glCanvas!!.addMouseListener(ml)
        glCanvas!!.addMouseMotionListener(ml)
        glCanvas!!.addMouseWheelListener(ml)
        glCanvas!!.addKeyListener(GlCanvasKeyListener())
        glCanvas!!.isFocusable = true

        defaultCloseOperation = EXIT_ON_CLOSE
        animator.add(glCanvas)
        animator.start()

        checkboxPanel = createCheckboxes()
        val northPanel = JPanel(BorderLayout())
        northPanel.add(checkboxPanel, BorderLayout.WEST)

        val glPanel = JPanel(BorderLayout())
        glPanel.add(northPanel, BorderLayout.NORTH)
        glPanel.add(glCanvas, BorderLayout.CENTER)
        glPanel.minimumSize = Dimension(400, 400)

        // --- Code editor ---
        DslTokenMaker.register()
        val syntaxColors = SyntaxColors.load()
        val tokenMaker = DslTokenMaker(syntaxColors)
        val editorTextArea = SafeRSyntaxTextArea(25, 45)
        editorTextArea.syntaxEditingStyle = DslTokenMaker.SYNTAX_STYLE
        tokenMaker.applyColors(editorTextArea)
        editorTextArea.font = Font("Monospaced", Font.PLAIN, 18)
        editorTextArea.text = currentScript
        editorTextArea.caretPosition = 0
        editorTextArea.tabSize = 2

        val editorScroll = RTextScrollPane(editorTextArea)
        editorScroll.lineNumbersEnabled = true
        val gutter = editorScroll.gutter
        if (gutter != null) {
            gutter.background = Color(syntaxColors.gutterBackground)
            gutter.borderColor = Color(syntaxColors.gutterBackground)
            gutter.lineNumberColor = Color(syntaxColors.gutterForeground)
        }

        val statusLabel = JLabel(" ")

        val applyBtn = JButton("Apply (Ctrl+Enter)")
        applyBtn.addActionListener { applyScript(editorTextArea.text, statusLabel) }
        editorTextArea.addKeyListener(object : KeyListener {
            override fun keyTyped(e: KeyEvent) {}
            override fun keyPressed(e: KeyEvent) {
                if (e.keyCode == KeyEvent.VK_ENTER && (e.modifiersEx and InputEvent.CTRL_DOWN_MASK) != 0) {
                    applyScript(editorTextArea.text, statusLabel)
                }
            }
            override fun keyReleased(e: KeyEvent) {}
        })

        val editorPanel = JPanel(BorderLayout())
        val bottomBar = JPanel(BorderLayout())
        bottomBar.add(applyBtn, BorderLayout.WEST)
        bottomBar.add(statusLabel, BorderLayout.CENTER)
        editorPanel.add(editorScroll, BorderLayout.CENTER)
        editorPanel.add(bottomBar, BorderLayout.SOUTH)
        editorPanel.preferredSize = Dimension(400, 800)
        editorPanel.minimumSize = Dimension(250, 400)

        val splitPane = JSplitPane(JSplitPane.HORIZONTAL_SPLIT, glPanel, editorPanel)
        splitPane.resizeWeight = 0.7
        splitPane.dividerLocation = 700

        contentPane.add(splitPane, BorderLayout.CENTER)

        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent) {
                saveViewConf()
                if (animator.isAnimating) animator.stop()
                dispose()
            }
        })
        setSize(1200, 800)
        isVisible = true
    }

    private fun loadScriptFromFile(): String {
        val file = File("pc-config.txt")
        return if (file.exists()) {
            println("  [OK] Loaded pc-config.txt")
            file.readText()
        } else {
            SceneConfigParser().getDefaultScript()
        }
    }

    private fun applyScript(script: String, statusLabel: JLabel) {
        val config = parser.parse(script).getOrNull()
        if (config == null) {
            statusLabel.text = "Parse error!"
            return
        }
        currentScript = script
        currentConfig = config
        buildWithConfig(config)
        rebuildCheckboxes()
        saveScriptToFile(script)
        statusLabel.text = "Applied ✓"
    }

    private fun saveScriptToFile(script: String) {
        try {
            File("pc-config.txt").writeText(script)
        } catch (e: Exception) {
            println("  [WARN] Failed to save pc-config.txt: ${e.message}")
        }
    }

    private fun saveViewConf() {
        try {
            File(VIEW_CONF_FILE).writeText(
                "camPitch=$camPitch\ncamYaw=$camYaw\ncamDist=$camDist\npanX=$panX\npanY=$panY\n"
            )
        } catch (e: Exception) {
            println("  [WARN] Failed to save $VIEW_CONF_FILE: ${e.message}")
        }
    }

    private fun loadViewConf() {
        val file = File(VIEW_CONF_FILE)
        if (!file.exists()) return
        try {
            val lines = file.readLines()
            for (line in lines) {
                val parts = line.split('=', limit = 2)
                if (parts.size != 2) continue
                when (parts[0]) {
                    "camPitch" -> camPitch = parts[1].toFloatOrNull() ?: camPitch
                    "camYaw" -> camYaw = parts[1].toFloatOrNull() ?: camYaw
                    "camDist" -> camDist = parts[1].toFloatOrNull() ?: camDist
                    "panX" -> panX = parts[1].toFloatOrNull() ?: panX
                    "panY" -> panY = parts[1].toFloatOrNull() ?: panY
                }
            }
            println("  [OK] Loaded $VIEW_CONF_FILE")
        } catch (e: Exception) {
            println("  [WARN] Failed to load $VIEW_CONF_FILE: ${e.message}")
        }
    }

    private fun buildWithConfig(config: SceneConfig) {
        val models = PcCaseModelFactory.buildAll(config)
        allModels.clear()
        allModels.putAll(models.mapValues { (_, csg) -> csgToModelData(csg) })
        visibleModels.clear()
        visibleModels.putAll(allModels)

        val mbComp = config.components.find { it.type == "motherboard" }
        if (mbComp != null) {
            for (op in mbComp.transforms) {
                if (op is TransformOp.Move) {
                    mbOffsetX = op.x.toFloat(); mbOffsetY = op.y.toFloat(); mbOffsetZ = op.z.toFloat()
                }
            }
        }
        glCanvas?.display()
    }

    private fun createCheckboxes(): JPanel {
        val panel = JPanel(FlowLayout(FlowLayout.LEFT))
        for ((key, _) in allModels) {
            val label = when {
                key.startsWith("frame_vertical") -> "Frame (vert.)"
                key.startsWith("frame_horizontal") -> "Frame (horiz.)"
                key == "motherboard" -> "Motherboard"
                key == "gpu" -> "GPU"
                key.startsWith("psu") -> "PSU"
                key == "cooler" -> "Cooler"
                key == "radiator" -> "Radiator"
                else -> key
            }
            if (checkboxBoxes.any { it.value.text == label }) continue
            val cb = JCheckBox(label, true)
            cb.addActionListener {
                visibleModels.clear()
                visibleModels.putAll(allModels.filterKeys { k ->
                    checkboxBoxes.any { (ck, ccb) ->
                        ccb.isSelected && (k == ck || k.startsWith("${ck}_"))
                    }
                })
                glCanvas?.display()
            }
            checkboxBoxes[key] = cb
            panel.add(cb)
        }
        return panel
    }

    private fun rebuildCheckboxes() {
        checkboxBoxes.clear()
        checkboxPanel.removeAll()
        for ((key, _) in allModels) {
            val label = when {
                key.startsWith("frame_vertical") -> "Frame (vert.)"
                key.startsWith("frame_horizontal") -> "Frame (horiz.)"
                key == "motherboard" -> "Motherboard"
                key == "gpu" -> "GPU"
                key.startsWith("psu") -> "PSU"
                key == "cooler" -> "Cooler"
                key == "radiator" -> "Radiator"
                else -> key
            }
            if (checkboxBoxes.any { it.value.text == label }) continue
            val cb = JCheckBox(label, true)
            cb.addActionListener {
                visibleModels.clear()
                visibleModels.putAll(allModels.filterKeys { k ->
                    checkboxBoxes.any { (ck, ccb) ->
                        ccb.isSelected && (k == ck || k.startsWith("${ck}_"))
                    }
                })
                glCanvas?.display()
            }
            checkboxBoxes[key] = cb
            checkboxPanel.add(cb)
        }
        checkboxPanel.revalidate()
        checkboxPanel.repaint()
    }

    override fun init(drawable: GLAutoDrawable) {
        val gl = drawable.gl.gL2
        gl.glShadeModel(GL2.GL_SMOOTH)
        gl.glClearColor(0.1f, 0.1f, 0.15f, 0f)
        gl.glClearDepth(1.0)
        gl.glEnable(GL2.GL_DEPTH_TEST)
        gl.glDepthFunc(GL2.GL_LEQUAL)
        gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL2.GL_NICEST)

        gl.glEnable(GLLightingFunc.GL_LIGHTING)
        gl.glEnable(GLLightingFunc.GL_LIGHT0)
        gl.glEnable(GL2.GL_COLOR_MATERIAL)
        gl.glColorMaterial(GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE)

        val lightAmbient = floatArrayOf(0.3f, 0.3f, 0.3f, 1.0f)
        val lightDiffuse = floatArrayOf(1.0f, 1.0f, 1.0f, 1.0f)
        val lightPosition = floatArrayOf(200f, 400f, 300f, 1.0f)
        gl.glLightfv(GLLightingFunc.GL_LIGHT0, GLLightingFunc.GL_AMBIENT, lightAmbient, 0)
        gl.glLightfv(GLLightingFunc.GL_LIGHT0, GLLightingFunc.GL_DIFFUSE, lightDiffuse, 0)
        gl.glLightfv(GLLightingFunc.GL_LIGHT0, GLLightingFunc.GL_POSITION, lightPosition, 0)

        try {
            val texFile = listOf(File("motherboard.png"), File("../motherboard.png"))
                .firstOrNull { it.exists() }
            if (texFile != null) {
                mbTexture = TextureIO.newTexture(texFile, false)
                mbTexture?.setTexParameteri(gl, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR)
                mbTexture?.setTexParameteri(gl, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR)
                mbTexture?.setTexParameteri(gl, GL2.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP_TO_EDGE)
                mbTexture?.setTexParameteri(gl, GL2.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP_TO_EDGE)
                println("  [OK] Loaded motherboard texture: ${texFile.name} (${mbTexture?.imageWidth}x${mbTexture?.imageHeight})")
            } else {
                println("  [WARN] motherboard.png not found, tried: ${File("motherboard.png").absolutePath}, ${File("../motherboard.png").absolutePath}")
            }
        } catch (e: Exception) {
            println("  [WARN] Failed to load motherboard texture: ${e.message}")
            e.printStackTrace()
        }

        try {
            val texFile = listOf(File("motherboard_down.png"), File("../motherboard_down.png"))
                .firstOrNull { it.exists() }
            if (texFile != null) {
                mbTextureBottom = TextureIO.newTexture(texFile, false)
                mbTextureBottom?.setTexParameteri(gl, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR)
                mbTextureBottom?.setTexParameteri(gl, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR)
                mbTextureBottom?.setTexParameteri(gl, GL2.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP_TO_EDGE)
                mbTextureBottom?.setTexParameteri(gl, GL2.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP_TO_EDGE)
                println("  [OK] Loaded motherboard bottom texture: ${texFile.name} (${mbTextureBottom?.imageWidth}x${mbTextureBottom?.imageHeight})")
            } else {
                println("  [WARN] motherboard_down.png not found")
            }
        } catch (e: Exception) {
            println("  [WARN] Failed to load motherboard_down texture: ${e.message}")
        }
    }

    override fun reshape(drawable: GLAutoDrawable, x: Int, y: Int, width: Int, height: Int) {
        var h = height
        val gl = drawable.gl.gL2
        if (h <= 0) h = 1
        gl.glViewport(0, 0, width, h)
        gl.glMatrixMode(GL2.GL_PROJECTION)
        gl.glLoadIdentity()
        val aspect = width.toFloat() / h
        glu.gluPerspective(45f, aspect, 0.1f, 2000f)
        gl.glMatrixMode(GL2.GL_MODELVIEW)
        gl.glLoadIdentity()
    }

    override fun display(drawable: GLAutoDrawable) {
        val gl = drawable.gl.gL2
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT or GL2.GL_DEPTH_BUFFER_BIT)
        gl.glLoadIdentity()

        gl.glTranslatef(panX, panY, camDist)
        gl.glRotatef(camPitch, 1f, 0f, 0f)
        gl.glRotatef(camYaw, 0f, 0f, 1f)

        motherboardModel()?.let { md ->
            gl.glBegin(GL2.GL_TRIANGLES)
            var vi = 0; var ni = 0
            for (i in 0 until md.verticesCount) {
                val x = md.vertex[vi++]; val y = md.vertex[vi++]; val z = md.vertex[vi++]
                gl.glColor4f(md.vertex[vi++], md.vertex[vi++], md.vertex[vi++], md.vertex[vi++])
                gl.glNormal3f(md.normals[ni++], md.normals[ni++], md.normals[ni++])
                gl.glVertex3f(x, y, z)
            }
            gl.glEnd()
        }

        gl.glEnable(GL2.GL_CULL_FACE)
        gl.glCullFace(GL2.GL_BACK)
        gl.glDepthMask(false)
        gl.glDepthFunc(GL2.GL_ALWAYS)
        drawMotherboardTexture(gl)
        drawMotherboardBottomTexture(gl)
        gl.glDepthFunc(GL2.GL_LEQUAL)
        gl.glDepthMask(true)
        gl.glDisable(GL2.GL_CULL_FACE)

        for ((name, md) in visibleModels) {
            if (name == "motherboard" || name.startsWith("motherboard_")) continue
            gl.glBegin(GL2.GL_TRIANGLES)
            var vi = 0; var ni = 0
            for (i in 0 until md.verticesCount) {
                val x = md.vertex[vi++]; val y = md.vertex[vi++]; val z = md.vertex[vi++]
                gl.glColor4f(md.vertex[vi++], md.vertex[vi++], md.vertex[vi++], md.vertex[vi++])
                gl.glNormal3f(md.normals[ni++], md.normals[ni++], md.normals[ni++])
                gl.glVertex3f(x, y, z)
            }
            gl.glEnd()
        }

        drawAxesOverlay(gl)
        gl.glFlush()
    }

    private fun drawAxesOverlay(gl: GL2) {
        val prevVp = IntArray(4)
        gl.glGetIntegerv(GL2.GL_VIEWPORT, prevVp, 0)

        val overlaySize = 260
        val orthoRange = 4.0f
        val axisLen = 2.5f
        val arrowSize = 0.25f
        val labelSize = 0.2f
        val labelOffset = 0.5f

        gl.glViewport(0, 0, overlaySize, overlaySize)

        gl.glMatrixMode(GL2.GL_PROJECTION)
        gl.glPushMatrix()
        gl.glLoadIdentity()
        gl.glOrtho(-orthoRange.toDouble(), orthoRange.toDouble(), -orthoRange.toDouble(), orthoRange.toDouble(), -10.0, 10.0)

        gl.glMatrixMode(GL2.GL_MODELVIEW)
        gl.glPushMatrix()
        gl.glLoadIdentity()

        gl.glRotatef(camPitch, 1f, 0f, 0f)
        gl.glRotatef(camYaw, 0f, 0f, 1f)

        gl.glDisable(GL2.GL_LIGHTING)
        gl.glDisable(GL2.GL_DEPTH_TEST)

        gl.glLineWidth(3.0f)

        drawAxisLine(gl, 0f, 0f, 0f, axisLen, 0f, 0f, 1f, 0f, 0f)
        drawAxisLine(gl, 0f, 0f, 0f, 0f, 0f, axisLen, 0f, 1f, 0f)
        drawAxisLine(gl, 0f, 0f, 0f, 0f, axisLen, 0f, 0f, 0f, 1f)

        drawArrowHead(gl, axisLen, 0f, 0f, 1f, 0f, 0f, arrowSize, 1f, 0f, 0f)
        drawArrowHead(gl, 0f, 0f, axisLen, 0f, 0f, 1f, arrowSize, 0f, 1f, 0f)
        drawArrowHead(gl, 0f, axisLen, 0f, 0f, 1f, 0f, arrowSize, 0f, 0f, 1f)

        gl.glPopMatrix()
        gl.glLoadIdentity()

        val lx = rotatePoint(axisLen + labelOffset, 0f, 0f, camPitch, camYaw)
        val ly = rotatePoint(0f, axisLen + labelOffset, 0f, camPitch, camYaw)
        val lz = rotatePoint(0f, 0f, axisLen + labelOffset, camPitch, camYaw)

        drawLetterX(gl, lx[0], lx[1], lx[2], labelSize, 1f, 0f, 0f)
        drawLetterY(gl, ly[0], ly[1], ly[2], labelSize, 0f, 0f, 1f)
        drawLetterZ(gl, lz[0], lz[1], lz[2], labelSize, 0f, 1f, 0f)

        gl.glLineWidth(1.0f)
        gl.glEnable(GL2.GL_DEPTH_TEST)
        gl.glEnable(GL2.GL_LIGHTING)

        gl.glMatrixMode(GL2.GL_PROJECTION)
        gl.glPopMatrix()
        gl.glMatrixMode(GL2.GL_MODELVIEW)
        gl.glViewport(prevVp[0], prevVp[1], prevVp[2], prevVp[3])
    }

    private fun drawAxisLine(gl: GL2, x1: Float, y1: Float, z1: Float, x2: Float, y2: Float, z2: Float,
                             r: Float, g: Float, b: Float) {
        gl.glColor3f(r, g, b)
        gl.glBegin(GL2.GL_LINES)
        gl.glVertex3f(x1, y1, z1)
        gl.glVertex3f(x2, y2, z2)
        gl.glEnd()
    }

    private fun drawArrowHead(gl: GL2, tipX: Float, tipY: Float, tipZ: Float,
                              dirX: Float, dirY: Float, dirZ: Float, size: Float,
                              r: Float, g: Float, b: Float) {
        gl.glColor3f(r, g, b)
        val bx = tipX - dirX * size * 3f
        val by = tipY - dirY * size * 3f
        val bz = tipZ - dirZ * size * 3f
        val s = size

        val perpX1: Float; val perpY1: Float; val perpZ1: Float
        val perpX2: Float; val perpY2: Float; val perpZ2: Float

        if (Math.abs(dirZ) < 0.9f) {
            perpX1 = -dirY; perpY1 = dirX; perpZ1 = 0f
        } else {
            perpX1 = 0f; perpY1 = -dirZ; perpZ1 = dirY
        }
        val len1 = kotlin.math.sqrt(perpX1 * perpX1 + perpY1 * perpY1 + perpZ1 * perpZ1)
        val nx = perpX1 / len1; val ny = perpY1 / len1; val nz = perpZ1 / len1

        if (Math.abs(dirX) < 0.9f) {
            perpX2 = 0f; perpY2 = -dirZ; perpZ2 = dirY
        } else {
            perpX2 = -dirY; perpY2 = dirX; perpZ2 = 0f
        }
        val len2 = kotlin.math.sqrt(perpX2 * perpX2 + perpY2 * perpY2 + perpZ2 * perpZ2)
        val mx = perpX2 / len2; val my = perpY2 / len2; val mz = perpZ2 / len2

        gl.glBegin(GL2.GL_TRIANGLES)
        gl.glVertex3f(tipX, tipY, tipZ)
        gl.glVertex3f(bx + nx * s, by + ny * s, bz + nz * s)
        gl.glVertex3f(bx - nx * s, by - ny * s, bz - nz * s)
        gl.glEnd()
        gl.glBegin(GL2.GL_TRIANGLES)
        gl.glVertex3f(tipX, tipY, tipZ)
        gl.glVertex3f(bx + mx * s, by + my * s, bz + mz * s)
        gl.glVertex3f(bx - mx * s, by - my * s, bz - mz * s)
        gl.glEnd()
    }

    private fun drawLetterX(gl: GL2, x: Float, y: Float, z: Float, s: Float,
                            r: Float, g: Float, b: Float) {
        gl.glColor3f(r, g, b)
        gl.glBegin(GL2.GL_LINES)
        gl.glVertex3f(x - s, y - s, z); gl.glVertex3f(x + s, y + s, z)
        gl.glVertex3f(x - s, y + s, z); gl.glVertex3f(x + s, y - s, z)
        gl.glEnd()
    }

    private fun drawLetterY(gl: GL2, x: Float, y: Float, z: Float, s: Float,
                            r: Float, g: Float, b: Float) {
        gl.glColor3f(r, g, b)
        gl.glBegin(GL2.GL_LINES)
        gl.glVertex3f(x - s, y + s, z); gl.glVertex3f(x, y, z)
        gl.glVertex3f(x + s, y + s, z); gl.glVertex3f(x, y, z)
        gl.glVertex3f(x, y, z); gl.glVertex3f(x, y - s, z)
        gl.glEnd()
    }

    private fun drawLetterZ(gl: GL2, x: Float, y: Float, z: Float, s: Float,
                            r: Float, g: Float, b: Float) {
        gl.glColor3f(r, g, b)
        gl.glBegin(GL2.GL_LINES)
        gl.glVertex3f(x - s, y + s, z); gl.glVertex3f(x + s, y + s, z)
        gl.glVertex3f(x + s, y + s, z); gl.glVertex3f(x - s, y - s, z)
        gl.glVertex3f(x - s, y - s, z); gl.glVertex3f(x + s, y - s, z)
        gl.glEnd()
    }

    private fun rotatePoint(x: Float, y: Float, z: Float,
                            pitch: Float, yaw: Float): FloatArray {
        var x1 = x.toDouble(); var y1 = y.toDouble(); var z1 = z.toDouble()
        val yawRad = Math.toRadians(yaw.toDouble())
        val cy = kotlin.math.cos(yawRad); val sy = kotlin.math.sin(yawRad)
        var t = x1 * cy - y1 * sy; y1 = x1 * sy + y1 * cy; x1 = t
        val pitchRad = Math.toRadians(pitch.toDouble())
        val cp = kotlin.math.cos(pitchRad); val sp = kotlin.math.sin(pitchRad)
        t = y1 * cp - z1 * sp; z1 = y1 * sp + z1 * cp; y1 = t
        return floatArrayOf(x1.toFloat(), y1.toFloat(), z1.toFloat())
    }

    override fun dispose(drawable: GLAutoDrawable) {}

    private inner class GlCanvasMouseListener : MouseAdapter() {

        override fun mousePressed(e: MouseEvent) {
            prevMouseX = e.x; prevMouseY = e.y
        }

        override fun mouseDragged(e: MouseEvent) {
            val dx = e.x - prevMouseX;
            val dy = e.y - prevMouseY
            if (e.modifiersEx and InputEvent.CTRL_DOWN_MASK != 0) {
                panX += dx * PAN_SENSITIVITY
                panY -= dy * PAN_SENSITIVITY
            } else {
                camYaw += dx * 0.5f
                camPitch += dy * 0.5f
            }
            prevMouseX = e.x; prevMouseY = e.y
            glCanvas?.display()
        }

        override fun mouseWheelMoved(e: MouseWheelEvent) {
            camDist -= e.wheelRotation * 10f
            camDist = camDist.coerceIn(-1500f, -50f)
            glCanvas?.display()
        }
    }

    private inner class GlCanvasKeyListener : KeyListener {

        override fun keyTyped(e: KeyEvent) {}
        override fun keyPressed(e: KeyEvent) {
            when (e.keyCode) {
                KeyEvent.VK_R -> {
                    camPitch = -90f; camYaw = 0f; camDist = -300f; panX = 0f; panY = 0f
                }
            }
            glCanvas?.display()
        }

        override fun keyReleased(e: KeyEvent) {}
    }

    private fun motherboardModel(): ModelData? =
        visibleModels.entries.firstOrNull { it.key == "motherboard" || it.key.startsWith("motherboard_") }?.value

    private fun hasMotherboard(): Boolean =
        visibleModels.keys.any { it == "motherboard" || it.startsWith("motherboard_") }

    private fun drawMotherboardTexture(gl: GL2) {
        if (!hasMotherboard() || mbTexture == null) return

        val mbZ = mbOffsetZ
        val mx = mbOffsetX
        val my = mbOffsetY
        val hw = 305f / 2
        val hd = 205.8f / 2

        gl.glActiveTexture(GL2.GL_TEXTURE0)
        gl.glEnable(GL2.GL_TEXTURE_2D)
        mbTexture!!.bind(gl)
        gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_REPLACE)

        gl.glDisable(GL2.GL_LIGHTING)
        gl.glDisable(GL2.GL_COLOR_MATERIAL)

        gl.glBegin(GL2.GL_QUADS)
        gl.glTexCoord2f(0f, 0f); gl.glVertex3f(mx - hw, my - hd, mbZ)
        gl.glTexCoord2f(1f, 0f); gl.glVertex3f(mx + hw, my - hd, mbZ)
        gl.glTexCoord2f(1f, 1f); gl.glVertex3f(mx + hw, my + hd, mbZ)
        gl.glTexCoord2f(0f, 1f); gl.glVertex3f(mx - hw, my + hd, mbZ)
        gl.glEnd()

        gl.glDisable(GL2.GL_TEXTURE_2D)
        gl.glEnable(GL2.GL_COLOR_MATERIAL)
        gl.glEnable(GL2.GL_LIGHTING)
    }

    private fun drawMotherboardBottomTexture(gl: GL2) {
        if (!hasMotherboard() || mbTextureBottom == null) return

        val mbZ = mbOffsetZ - 1.6f
        val mx = mbOffsetX
        val my = mbOffsetY
        val hw = 305f / 2
        val hd = 205.8f / 2

        gl.glActiveTexture(GL2.GL_TEXTURE0)
        gl.glEnable(GL2.GL_TEXTURE_2D)
        mbTextureBottom!!.bind(gl)
        gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_REPLACE)

        gl.glDisable(GL2.GL_LIGHTING)
        gl.glDisable(GL2.GL_COLOR_MATERIAL)

        gl.glBegin(GL2.GL_QUADS)
        gl.glTexCoord2f(0f, 0f); gl.glVertex3f(mx - hw, my + hd, mbZ)
        gl.glTexCoord2f(1f, 0f); gl.glVertex3f(mx + hw, my + hd, mbZ)
        gl.glTexCoord2f(1f, 1f); gl.glVertex3f(mx + hw, my - hd, mbZ)
        gl.glTexCoord2f(0f, 1f); gl.glVertex3f(mx - hw, my - hd, mbZ)
        gl.glEnd()

        gl.glDisable(GL2.GL_TEXTURE_2D)
        gl.glEnable(GL2.GL_COLOR_MATERIAL)
        gl.glEnable(GL2.GL_LIGHTING)
    }

    private fun csgToModelData(csg: CSG): ModelData {
        val vertList = mutableListOf<Float>()
        val normList = mutableListOf<Float>()
        var triCount = 0

        for (polygon in csg.polygons) {
            val verts = polygon.vertices
            if (verts.size < 3) continue
            val nx = polygon.normal.x.toFloat()
            val ny = polygon.normal.y.toFloat()
            val nz = polygon.normal.z.toFloat()
            val cr = polygon.color.getRed() / 255f
            val cg = polygon.color.getGreen() / 255f
            val cb = polygon.color.getBlue() / 255f
            val ca = polygon.color.getAlpha() / 255f

            val v0 = verts[0]
            for (i in 1 until verts.size - 1) {
                val v1 = verts[i]
                val v2 = verts[i + 1]
                for (v in listOf(v0, v1, v2)) {
                    vertList.add(v.x.toFloat())
                    vertList.add(v.y.toFloat())
                    vertList.add(v.z.toFloat())
                    vertList.add(cr)
                    vertList.add(cg)
                    vertList.add(cb)
                    vertList.add(ca)
                    normList.add(nx)
                    normList.add(ny)
                    normList.add(nz)
                }
                triCount += 3
            }
        }

        return ModelData(
            vertex = vertList.toFloatArray(), normals = normList.toFloatArray(), verticesCount = triCount
        )
    }
}
