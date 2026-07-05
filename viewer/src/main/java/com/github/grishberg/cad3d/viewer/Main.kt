package com.github.grishberg.cad3d.viewer

import com.github.grishberg.cad3d.pccase.AluminumProfile
import com.github.grishberg.cad3d.pccase.Cooler
import com.github.grishberg.cad3d.pccase.Gpu
import com.github.grishberg.cad3d.pccase.Motherboard
import com.github.grishberg.cad3d.pccase.PcFrame
import com.github.grishberg.cad3d.pccase.Psu
import eu.printingin3d.javascad.coords.Angles3d
import com.jogamp.opengl.GL2
import com.jogamp.opengl.GLAutoDrawable
import com.jogamp.opengl.GLCapabilities
import com.jogamp.opengl.GLEventListener
import com.jogamp.opengl.GLProfile
import com.jogamp.opengl.awt.GLCanvas
import com.jogamp.opengl.fixedfunc.GLLightingFunc
import com.jogamp.opengl.glu.GLU
import com.jogamp.opengl.util.Animator
import eu.printingin3d.javascad.utils.Color as JcColor
import eu.printingin3d.javascad.vrl.ColorFacetGenerationContext
import eu.printingin3d.javascad.vrl.FacetGenerationContext
import eu.printingin3d.javascad.vrl.CSG
import java.awt.BorderLayout
import java.awt.FlowLayout
import java.awt.event.InputEvent
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseWheelEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.JCheckBox
import javax.swing.JFrame
import javax.swing.JPanel

data class ModelData(
    val vertex: FloatArray,
    val normals: FloatArray,
    val verticesCount: Int
)

class Main(title: String?) : JFrame(title), GLEventListener {

    private val animator = Animator()
    private val glu = GLU()
    private var glCanvas: GLCanvas? = null
    private var prevMouseX = 0
    private var prevMouseY = 0

    private var rotateX = 0f
    private var rotateY = -90f
    private var rotateZ = 0f
    private var translateX = 0f
    private var translateY = 0f
    private var translateZ = -300f

    private val allModels = buildPcCaseModels().mapValues { (_, csg) -> csgToModelData(csg) }.toMutableMap()
    private val visibleModels = allModels.toMutableMap()

    init {
        title ?: "PC Case Viewer (OpenGL)"

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

        val boxes = createCheckboxes()
        contentPane.add(boxes, BorderLayout.NORTH)
        contentPane.add(glCanvas, BorderLayout.CENTER)

        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent) {
                if (animator.isAnimating) animator.stop()
                dispose()
            }
        })
        setSize(1200, 800)
        isVisible = true
    }

    private fun createCheckboxes(): JPanel {
        val panel = JPanel(FlowLayout(FlowLayout.LEFT))
        val boxes = mutableMapOf<String, JCheckBox>()
        val labels = listOf(
            "frame_vertical" to "Frame (vert.)",
            "frame_horizontal" to "Frame (horiz.)",
            "motherboard" to "Motherboard",
            "gpu" to "GPU",
            "psu" to "PSU",
            "cooler" to "Cooler"
        )
        for ((key, label) in labels) {
            val cb = JCheckBox(label, true)
            cb.addActionListener {
                visibleModels.clear()
                visibleModels.putAll(allModels.filterKeys { k -> boxes[k]?.isSelected ?: true })
                glCanvas?.display()
            }
            boxes[key] = cb
            panel.add(cb)
        }
        return panel
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

        gl.glTranslatef(translateX, translateY, translateZ)
        gl.glPushMatrix()
        gl.glRotatef(rotateX, 1f, 0f, 0f)
        gl.glRotatef(rotateY, 0f, 1f, 0f)
        gl.glRotatef(rotateZ, 0f, 0f, 1f)

        for ((_, md) in visibleModels) {
            gl.glBegin(GL2.GL_TRIANGLES)
            var vi = 0
            var ni = 0
            for (i in 0 until md.verticesCount) {
                val x = md.vertex[vi++]
                val y = md.vertex[vi++]
                val z = md.vertex[vi++]
                gl.glColor4f(md.vertex[vi++], md.vertex[vi++], md.vertex[vi++], md.vertex[vi++])
                gl.glNormal3f(md.normals[ni++], md.normals[ni++], md.normals[ni++])
                gl.glVertex3f(x, y, z)
            }
            gl.glEnd()
        }

        gl.glPopMatrix()
        gl.glFlush()
    }

    override fun dispose(drawable: GLAutoDrawable) {}

    private inner class GlCanvasMouseListener : MouseAdapter() {
        override fun mousePressed(e: MouseEvent) {
            prevMouseX = e.x; prevMouseY = e.y
        }
        override fun mouseDragged(e: MouseEvent) {
            val dx = e.x - prevMouseX; val dy = e.y - prevMouseY
            if (e.modifiersEx and InputEvent.CTRL_DOWN_MASK != 0) {
                translateX += dx * 0.5f; translateY -= dy * 0.5f
            } else {
                rotateX += dy * 0.5f; rotateY += dx * 0.5f
            }
            prevMouseX = e.x; prevMouseY = e.y
            glCanvas?.display()
        }
        override fun mouseWheelMoved(e: MouseWheelEvent) {
            translateZ -= e.wheelRotation * 10f
            translateZ = translateZ.coerceIn(-1500f, -50f)
            glCanvas?.display()
        }
    }

    private inner class GlCanvasKeyListener : KeyListener {
        override fun keyTyped(e: KeyEvent) {}
        override fun keyPressed(e: KeyEvent) {
            when (e.keyCode) {
                KeyEvent.VK_R -> {
                    rotateX = 0f; rotateY = -90f; rotateZ = 0f
                    translateX = 0f; translateY = 0f; translateZ = -300f
                }
                KeyEvent.VK_LEFT -> translateX -= 5f
                KeyEvent.VK_RIGHT -> translateX += 5f
                KeyEvent.VK_UP -> translateY += 5f
                KeyEvent.VK_DOWN -> translateY -= 5f
            }
            glCanvas?.display()
        }
        override fun keyReleased(e: KeyEvent) {}
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            com.github.grishberg.cad3d.viewer.Main("PC Case Viewer (OpenGL)")
        }
    }

        private fun buildPcCaseModels(): Map<String, CSG> {
            val defaultContext: FacetGenerationContext = ColorFacetGenerationContext(JcColor.GRAY).apply { setFn(8) }
            val frameVertContext: FacetGenerationContext = ColorFacetGenerationContext(JcColor(100, 140, 200)).apply { setFn(8) }
            val mbContext: FacetGenerationContext = ColorFacetGenerationContext(JcColor.GREEN).apply { setFn(8) }
            val gpuContext: FacetGenerationContext = ColorFacetGenerationContext(JcColor(200, 30, 30)).apply { setFn(8) }
            val psuContext: FacetGenerationContext = ColorFacetGenerationContext(JcColor(60, 60, 60)).apply { setFn(8) }
            val coolerContext: FacetGenerationContext = ColorFacetGenerationContext(JcColor(180, 180, 180)).apply { setFn(8) }

            AluminumProfile.reset()

            val frameCfg = PcFrame(width = 530.0, height = 350.0, depth = 330.0)
            val frameVertical = frameCfg.buildVertical()
            val frameHorizontal = frameCfg.buildHorizontal()

            val p = AluminumProfile.PROFILE_SIZE
            val bottomY = p / 2 + p / 2

            val mb = Motherboard().build().move(-40.0, bottomY + 1.6 / 2, 0.0)
            val gpu = Gpu().build().rotate(Angles3d.yOnly(-90.0)).move(-50.0, 15.0 + 112.0 / 2, 0.0)
            val psu = Psu().build().move(175.0, bottomY + 86.0 / 2, 0.0)
            val cooler = Cooler().build().rotate(Angles3d.zOnly(90.0)).move(50.0, bottomY + 1.6 + 80.0, -20.0)

            return mapOf(
                "frame_vertical" to frameVertical.toCSG(frameVertContext),
                "frame_horizontal" to frameHorizontal.toCSG(defaultContext),
                "motherboard" to mb.toCSG(mbContext),
                "gpu" to gpu.toCSG(gpuContext),
                "psu" to psu.toCSG(psuContext),
                "cooler" to cooler.toCSG(coolerContext)
            )
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
            vertex = vertList.toFloatArray(),
            normals = normList.toFloatArray(),
            verticesCount = triCount
        )
    }
}
