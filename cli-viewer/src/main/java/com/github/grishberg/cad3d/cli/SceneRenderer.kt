package com.github.grishberg.cad3d.cli

import eu.printingin3d.javascad.vrl.CSG
import eu.printingin3d.javascad.utils.Color as JSCadColor
import java.awt.Color
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.geom.AffineTransform
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class SceneRenderer(
    private val width: Int = 1920,
    private val height: Int = 1080,
    private val fov: Double = 45.0,
    private val cameraDistance: Double = 39800.0,
    private val cameraAngleX: Double = 155.0,
    private val cameraAngleY: Double = 35.0,
    private val textureOverlays: Map<String, TextureOverlay> = emptyMap()
) {
    private val lightDir = normalize(doubleArrayOf(0.4, -0.7, 0.5))

    fun renderScene(models: List<Pair<String, CSG>>, outputFile: File) {
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val g = image.createGraphics()
        g.color = Color(30, 30, 40)
        g.fillRect(0, 0, width, height)
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        val rx = Math.toRadians(cameraAngleX)
        val ry = Math.toRadians(cameraAngleY)
        val cosRx = Math.cos(rx); val sinRx = Math.sin(rx)
        val cosRy = Math.cos(ry); val sinRy = Math.sin(ry)

        val projected = mutableListOf<ProjFace>()

        for ((_, csg) in models) {
            for (polygon in csg.polygons) {
                val verts3d = polygon.vertices
                if (verts3d.size < 3) continue

                var nx = polygon.normal.x; var ny = polygon.normal.y; var nz = polygon.normal.z
                var rnx = nx; var rny = ny; var rnz = nz
                var t = rny * cosRx - rnz * sinRx; rnz = rny * sinRx + rnz * cosRx; rny = t
                t = rnx * cosRy + rnz * sinRy; rnz = -rnx * sinRy + rnz * cosRy; rnx = t

                if (rnz > 0) continue

                val shade = Math.max(0.2, -(rnx * lightDir[0] + rny * lightDir[1] + rnz * lightDir[2]))

                val screenVerts = mutableListOf<DoubleArray>()
                var avgZ = 0.0
                var valid = true
                for (v in verts3d) {
                    var x = v.x; var y = v.y; var z = v.z
                    t = y * cosRx - z * sinRx; z = y * sinRx + z * cosRx; y = t
                    t = x * cosRy + z * sinRy; z = -x * sinRy + z * cosRy; x = t
                    z += cameraDistance
                    if (z <= 1.0) { valid = false; break }
                    val scale = fov * width / (2.0 * z)
                    screenVerts.add(doubleArrayOf(width / 2.0 + x * scale, height / 2.0 + y * scale))
                    avgZ += z
                }
                if (!valid) continue
                avgZ /= verts3d.size

                val jc = polygon.color
                projected.add(ProjFace(screenVerts, jc, shade, avgZ))
            }
        }

        println("  Facets to render: ${projected.size}")

        projected.sortedByDescending { it.avgZ }.forEach { face ->
            val c = face.color
            val s = face.shade
            val r = Math.min(255, (c.getRed() * s).toInt())
            val gv = Math.min(255, (c.getGreen() * s).toInt())
            val b = Math.min(255, (c.getBlue() * s).toInt())
            g.color = Color(r, gv, b)

            val xs = IntArray(face.verts.size) { face.verts[it][0].toInt() }
            val ys = IntArray(face.verts.size) { face.verts[it][1].toInt() }
            g.fillPolygon(xs, ys, xs.size)
        }

        renderTextureOverlays(g, rx, ry)

        g.dispose()
        ImageIO.write(image, "png", outputFile)
        println("  [OK] Render: ${outputFile.name} (${width}x${height})")
    }

    private fun normalize(v: DoubleArray): DoubleArray {
        val len = Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2])
        return if (len > 0) doubleArrayOf(v[0] / len, v[1] / len, v[2] / len) else v
    }

    private fun projectToScreen(wx: Double, wy: Double, wz: Double,
                                 cosRx: Double, sinRx: Double,
                                 cosRy: Double, sinRy: Double): DoubleArray? {
        var x = wx; var y = wy; var z = wz
        var t = y * cosRx - z * sinRx; z = y * sinRx + z * cosRx; y = t
        t = x * cosRy + z * sinRy; z = -x * sinRy + z * cosRy; x = t
        z += cameraDistance
        if (z <= 1.0) return null
        val scale = fov * width / (2.0 * z)
        return doubleArrayOf(width / 2.0 + x * scale, height / 2.0 + y * scale)
    }

    private fun renderTextureOverlays(g: Graphics2D, rx: Double, ry: Double) {
        val cosRx = Math.cos(rx); val sinRx = Math.sin(rx)
        val cosRy = Math.cos(ry); val sinRy = Math.sin(ry)

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

            val corners = listOf(
                projectToScreen(minX, minY, overlay.polygons.first().vertices.first().z, cosRx, sinRx, cosRy, sinRy),
                projectToScreen(maxX, minY, overlay.polygons.first().vertices.first().z, cosRx, sinRx, cosRy, sinRy),
                projectToScreen(minX, maxY, overlay.polygons.first().vertices.first().z, cosRx, sinRx, cosRy, sinRy),
                projectToScreen(maxX, maxY, overlay.polygons.first().vertices.first().z, cosRx, sinRx, cosRy, sinRy)
            )
            if (corners.any { it == null }) continue
            val c = corners.map { it!! }

            val scrMinX = c.minOf { it[0] }; val scrMaxX = c.maxOf { it[0] }
            val scrMinY = c.minOf { it[1] }; val scrMaxY = c.maxOf { it[1] }

            val texToScreen = AffineTransform()
            texToScreen.translate(scrMinX, scrMinY)
            texToScreen.scale((scrMaxX - scrMinX) / imgW, (scrMaxY - scrMinY) / imgH)

            val savedClip = g.clip
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

                g.clip = screenPoly
                g.drawImage(image, texToScreen, null)
            }
            g.clip = savedClip
        }
    }

    private data class ProjFace(
        val verts: List<DoubleArray>,
        val color: JSCadColor,
        val shade: Double,
        val avgZ: Double
    )
}

data class TextureOverlay(
    val image: BufferedImage,
    val polygons: List<eu.printingin3d.javascad.vrl.Polygon>
)
