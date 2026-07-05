package com.github.grishberg.cad3d.cli

import eu.printingin3d.javascad.vrl.CSG
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.event.InputEvent
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseWheelEvent
import java.awt.geom.AffineTransform
import javax.swing.JCheckBox
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.SwingUtilities
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

class PcCaseViewer(
    private val windowWidth: Int = 1200,
    private val windowHeight: Int = 800,
    private val textureOverlays: Map<String, TextureOverlay> = emptyMap()
) {
    private var allModels: Map<String, CSG> = emptyMap()
    private var visibleModels: Map<String, CSG> = emptyMap()
    private var camAngleX = 155.0
    private var camAngleY = 35.0
    private var camDistance = 700.0
    private val fov = 45.0
    private val lightDir = normalize(doubleArrayOf(0.4, -0.7, 0.5))

    private var prevMouseX = 0
    private var prevMouseY = 0

    private val checkboxes = mutableMapOf<String, JCheckBox>()

    private data class ProjFace(
        val verts: List<DoubleArray>,
        val color: eu.printingin3d.javascad.utils.Color,
        val shade: Double,
        val avgZ: Double
    )

    private var cachedFaces: List<ProjFace> = emptyList()

    fun show(models: Map<String, CSG>) {
        this.allModels = models
        this.visibleModels = models
        rebuildFaces()

        SwingUtilities.invokeLater {
            val frame = JFrame("PC Case Viewer")

            var viewPanel: JPanel? = null

            val controlPanel = JPanel(FlowLayout(FlowLayout.LEFT))
            val componentLabels = mapOf(
                "frame_vertical" to "Frame (vert.)",
                "frame_horizontal" to "Frame (horiz.)",
                "motherboard" to "Motherboard",
                "gpu" to "GPU",
                "psu" to "PSU"
            )
            for ((key, label) in componentLabels) {
                if (key !in models) continue
                val cb = JCheckBox(label, true)
                cb.addActionListener {
                    updateVisibility()
                    viewPanel?.repaint()
                }
                controlPanel.add(cb)
                checkboxes[key] = cb
            }

            val panel = object : JPanel() {
                override fun paintComponent(g: Graphics) {
                    super.paintComponent(g)
                    val g2 = g as Graphics2D
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
                    g2.color = Color(30, 30, 40)
                    g2.fillRect(0, 0, width, height)
                    drawFaces(g2)
                }
            }
            viewPanel = panel
            panel.preferredSize = Dimension(windowWidth, windowHeight)
            panel.isFocusable = true

            val ml = object : MouseAdapter() {
                override fun mousePressed(e: MouseEvent) {
                    prevMouseX = e.x; prevMouseY = e.y
                }
                override fun mouseDragged(e: MouseEvent) {
                    val dx = e.x - prevMouseX; val dy = e.y - prevMouseY
                    if (e.modifiersEx and InputEvent.CTRL_DOWN_MASK != 0) {
                        camDistance -= dy * 2.0; camDistance = camDistance.coerceIn(200.0, 3000.0)
                    } else {
                        camAngleX += dy * 0.5; camAngleY += dx * 0.5
                    }
                    prevMouseX = e.x; prevMouseY = e.y
                    rebuildFaces(); panel.repaint()
                }
                override fun mouseWheelMoved(e: MouseWheelEvent) {
                    camDistance += e.wheelRotation * 20.0
                    camDistance = camDistance.coerceIn(200.0, 3000.0)
                    rebuildFaces(); panel.repaint()
                }
            }
            panel.addMouseListener(ml)
            panel.addMouseMotionListener(ml)
            panel.addMouseWheelListener(ml)
            panel.addKeyListener(object : KeyListener {
                override fun keyTyped(e: KeyEvent) {}
                override fun keyPressed(e: KeyEvent) {
                    if (e.keyCode == KeyEvent.VK_R) {
                        camAngleX = 155.0; camAngleY = 35.0; camDistance = 700.0
                        rebuildFaces(); panel.repaint()
                    }
                }
                override fun keyReleased(e: KeyEvent) {}
            })

            frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
            frame.contentPane.add(controlPanel, BorderLayout.NORTH)
            frame.contentPane.add(panel, BorderLayout.CENTER)
            frame.pack()
            frame.setLocationRelativeTo(null)
            frame.isVisible = true
        }
    }

    private fun updateVisibility() {
        visibleModels = allModels.filterKeys { checkboxes[it]?.isSelected == true }
        rebuildFaces()
    }

    private fun rebuildFaces() {
        val projected = mutableListOf<ProjFace>()
        val rx = Math.toRadians(camAngleX)
        val ry = Math.toRadians(camAngleY)
        val cosRx = cos(rx); val sinRx = sin(rx)
        val cosRy = cos(ry); val sinRy = sin(ry)
        val camDist = camDistance

        for ((_, csg) in visibleModels) {
            for (polygon in csg.polygons) {
                val verts3d = polygon.vertices
                if (verts3d.size < 3) continue

                var nx = polygon.normal.x; var ny = polygon.normal.y; var nz = polygon.normal.z
                var rnx = nx; var rny = ny; var rnz = nz
                var t = rny * cosRx - rnz * sinRx; rnz = rny * sinRx + rnz * cosRx; rny = t
                t = rnx * cosRy + rnz * sinRy; rnz = -rnx * sinRy + rnz * cosRy; rnx = t

                if (rnz > 0) continue

                val shade = max(0.2, -(rnx * lightDir[0] + rny * lightDir[1] + rnz * lightDir[2]))

                val screenVerts = mutableListOf<DoubleArray>()
                var avgZ = 0.0
                var valid = true
                for (v in verts3d) {
                    var x = v.x; var y = v.y; var z = v.z
                    t = y * cosRx - z * sinRx; z = y * sinRx + z * cosRx; y = t
                    t = x * cosRy + z * sinRy; z = -x * sinRy + z * cosRy; x = t
                    z += camDist
                    if (z <= 1.0) { valid = false; break }
                    val scale = fov * windowWidth / (2.0 * z)
                    screenVerts.add(doubleArrayOf(
                        windowWidth / 2.0 + x * scale,
                        windowHeight / 2.0 + y * scale
                    ))
                    avgZ += z
                }
                if (!valid) continue
                avgZ /= verts3d.size

                val jc = polygon.color
                projected.add(ProjFace(screenVerts, jc, shade, avgZ))
            }
        }

        cachedFaces = projected.sortedByDescending { it.avgZ }
    }

    private fun drawFaces(g2: Graphics2D) {
        for (face in cachedFaces) {
            val c = face.color; val s = face.shade
            val r = min(255, (c.getRed() * s).toInt())
            val gv = min(255, (c.getGreen() * s).toInt())
            val b = min(255, (c.getBlue() * s).toInt())
            g2.color = Color(r, gv, b)
            val xs = IntArray(face.verts.size) { face.verts[it][0].toInt() }
            val ys = IntArray(face.verts.size) { face.verts[it][1].toInt() }
            g2.fillPolygon(xs, ys, xs.size)
        }

        renderTextureOverlays(g2)
    }

    private fun projectToScreen(wx: Double, wy: Double, wz: Double,
                                 cosRx: Double, sinRx: Double,
                                 cosRy: Double, sinRy: Double): DoubleArray? {
        var x = wx; var y = wy; var z = wz
        var t = y * cosRx - z * sinRx; z = y * sinRx + z * cosRx; y = t
        t = x * cosRy + z * sinRy; z = -x * sinRy + z * cosRy; x = t
        z += camDistance
        if (z <= 1.0) return null
        val scale = fov * windowWidth / (2.0 * z)
        return doubleArrayOf(windowWidth / 2.0 + x * scale, windowHeight / 2.0 + y * scale)
    }

    private fun renderTextureOverlays(g2: Graphics2D) {
        val rx = Math.toRadians(camAngleX)
        val ry = Math.toRadians(camAngleY)
        val cosRx = cos(rx); val sinRx = sin(rx)
        val cosRy = cos(ry); val sinRy = sin(ry)

        for ((_, overlay) in textureOverlays) {
            val image = overlay.image
            val imgW = image.width.toDouble()
            val imgH = image.height.toDouble()

            var minX = Double.MAX_VALUE; var maxX = -Double.MAX_VALUE
            var minY = Double.MAX_VALUE; var maxY = -Double.MAX_VALUE
            for (polygon in overlay.polygons) {
                for (v in polygon.vertices) {
                    if (v.x < minX) minX = v.x
                    if (v.x > maxX) maxX = v.x
                    if (v.y < minY) minY = v.y
                    if (v.y > maxY) maxY = v.y
                }
            }
            val worldW = maxX - minX
            val worldH = maxY - minY
            if (worldW <= 0 || worldH <= 0) continue

            val mbZ = overlay.polygons.first().vertices.first().z
            val corners = listOf(
                projectToScreen(minX, minY, mbZ, cosRx, sinRx, cosRy, sinRy),
                projectToScreen(maxX, minY, mbZ, cosRx, sinRx, cosRy, sinRy),
                projectToScreen(minX, maxY, mbZ, cosRx, sinRx, cosRy, sinRy),
                projectToScreen(maxX, maxY, mbZ, cosRx, sinRx, cosRy, sinRy)
            )
            if (corners.any { it == null }) continue
            val cn = corners.map { it!! }

            val scrMinX = cn.minOf { it[0] }; val scrMaxX = cn.maxOf { it[0] }
            val scrMinY = cn.minOf { it[1] }; val scrMaxY = cn.maxOf { it[1] }

            val texToScreen = AffineTransform()
            texToScreen.translate(scrMinX, scrMinY)
            texToScreen.scale((scrMaxX - scrMinX) / imgW, (scrMaxY - scrMinY) / imgH)

            val savedClip = g2.clip
            for (polygon in overlay.polygons) {
                val verts3d = polygon.vertices
                if (verts3d.size < 3) continue

                val screenPoly = java.awt.Polygon()
                var valid = true
                for (v in verts3d) {
                    val sp = projectToScreen(v.x, v.y, v.z, cosRx, sinRx, cosRy, sinRy)
                    if (sp == null) { valid = false; break }
                    screenPoly.addPoint(sp[0].toInt(), sp[1].toInt())
                }
                if (!valid || screenPoly.npoints < 3) continue

                g2.clip = screenPoly
                g2.drawImage(image, texToScreen, null)
            }
            g2.clip = savedClip
        }
    }

    private fun normalize(v: DoubleArray): DoubleArray {
        val len = sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2])
        return if (len > 0) doubleArrayOf(v[0] / len, v[1] / len, v[2] / len) else v
    }
}
