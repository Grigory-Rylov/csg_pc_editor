package com.github.grishberg.cad3d.config

import com.github.grishberg.cad3d.pccase.TransformOp
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SceneConfigParserTest {

    private val parser = SceneConfigParser()

    @Test
    fun `move with named z param in block keeps transform`() {
        val script = """
            frame(w=530 d=330 h=350 l=140)
            move(z=363.5) {
                radiator()
            }
        """.trimIndent()

        val result = parser.parse(script).getOrThrow()
        val rad = result.components.find { it.type == "radiator" }

        assertTrue(rad != null, "radiator should be found")
        val moveOp = rad!!.transforms.find { it is TransformOp.Move } as? TransformOp.Move
        assertTrue(moveOp != null, "should have a Move transform")
        assertEquals(0.0, moveOp!!.x, 0.001, "x should be 0")
        assertEquals(0.0, moveOp.y, 0.001, "y should be 0")
        assertEquals(363.5, moveOp.z, 0.001, "z should be 363.5")
    }

    @Test
    fun `move with all named params in block`() {
        val script = """
            frame(w=530 d=330 h=350)
            move(x=10 y=20 z=30) {
                motherboard()
            }
        """.trimIndent()

        val result = parser.parse(script).getOrThrow()
        val mb = result.components.find { it.type == "motherboard" }

        assertTrue(mb != null)
        val moveOp = mb!!.transforms.find { it is TransformOp.Move } as? TransformOp.Move
        assertTrue(moveOp != null)
        assertEquals(10.0, moveOp!!.x, 0.001)
        assertEquals(20.0, moveOp.y, 0.001)
        assertEquals(30.0, moveOp.z, 0.001)
    }

    @Test
    fun `chained move and rotate with single child in block`() {
        val script = """
            frame(w=530 d=330 h=350)
            move(-240 95 0) rotate(90 0 0) {
                psu()
            }
        """.trimIndent()

        val result = parser.parse(script).getOrThrow()
        val psu = result.components.find { it.type == "psu" }

        assertTrue(psu != null)
        assertEquals(2, psu!!.transforms.size)
        assertTrue(psu.transforms[0] is TransformOp.Move)
        assertEquals(-240.0, (psu.transforms[0] as TransformOp.Move).x, 0.001)
        assertTrue(psu.transforms[1] is TransformOp.Rotate)
    }

    @Test
    fun `block with two children both inherit outer transform`() {
        val script = """
            frame(w=530 d=330 h=350)
            move(z=363.5) {
                radiator()
                move(x=-200) radiator()
            }
        """.trimIndent()

        val result = parser.parse(script).getOrThrow()
        val rads = result.components.filter { it.type == "radiator" }

        assertEquals(2, rads.size, "should have 2 radiators")

        val r1 = rads[0]
        assertEquals(1, r1.transforms.size)
        val m1 = r1.transforms[0] as TransformOp.Move
        assertEquals(0.0, m1.x, 0.001)
        assertEquals(0.0, m1.y, 0.001)
        assertEquals(363.5, m1.z, 0.001)

        val r2 = rads[1]
        assertEquals(2, r2.transforms.size)
        val m2a = r2.transforms[0] as TransformOp.Move
        assertEquals(0.0, m2a.x, 0.001)
        assertEquals(0.0, m2a.y, 0.001)
        assertEquals(363.5, m2a.z, 0.001, "second radiator should inherit z=363.5 from outer")
        val m2b = r2.transforms[1] as TransformOp.Move
        assertEquals(-200.0, m2b.x, 0.001)
        assertEquals(0.0, m2b.y, 0.001)
        assertEquals(0.0, m2b.z, 0.001)
    }

    @Test
    fun `parse default script`() {
        val script = parser.getDefaultScript()
        val result = parser.parse(script).getOrThrow()
        assertEquals(530.0, result.frameWidth, 0.001)
        assertEquals(330.0, result.frameDepth, 0.001)
        assertEquals(350.0, result.frameHeight, 0.001)
        assertEquals(6, result.components.size)
    }

    @Test
    fun `chained transforms on single line`() {
        val script = """
            frame(w=530 d=330 h=350)
            move(-240 95 0) rotate(90 0 0) psu()
        """.trimIndent()

        val result = parser.parse(script).getOrThrow()
        val psu = result.components.find { it.type == "psu" }
        assertTrue(psu != null)
        assertEquals(2, psu!!.transforms.size)
    }

    @Test
    fun `component with count and spacing`() {
        val script = """
            frame(w=530 d=330 h=350)
            move(0 0 100) gpu(n=5 s=55)
        """.trimIndent()

        val result = parser.parse(script).getOrThrow()
        val gpu = result.components.find { it.type == "gpu" }
        assertTrue(gpu != null)
        assertEquals(5, gpu!!.count)
        assertEquals(55.0, gpu.spacing, 0.001)
    }

    @Test
    fun `parse error for missing frame`() {
        val script = "move(0 0 0) motherboard()"
        val result = parser.parse(script)
        assertTrue(result.isFailure)
    }

    @Test
    fun `parse error for invalid syntax`() {
        val script = """
            frame(w=530 d=330 h=350)
            unknown(1 2 3)
        """.trimIndent()
        val result = parser.parse(script)
        assertTrue(result.isFailure)
    }

    @Test
    fun `frame with bottomEdge block`() {
        val script = """
            frame(w=530 d=330 h=350) {
                bottomEdge(x=0)
                bottomEdge(x=-100)
                bottomEdge(x=100)
            }
        """.trimIndent()
        val result = parser.parse(script).getOrThrow()
        assertEquals(530.0, result.frameWidth, 0.001)
        assertEquals(330.0, result.frameDepth, 0.001)
        assertEquals(350.0, result.frameHeight, 0.001)
        assertEquals(listOf(0.0, -100.0, 100.0), result.frameBottomBeams)
    }

    @Test
    fun `frame with mixed b param and bottomEdge block`() {
        val script = """
            frame(w=530 d=330 h=350 b=200) {
                bottomEdge(x=0)
                bottomEdge(x=-100)
            }
        """.trimIndent()
        val result = parser.parse(script).getOrThrow()
        assertEquals(listOf(200.0, 0.0, -100.0), result.frameBottomBeams)
    }

    @Test
    fun `frame with empty block`() {
        val script = """
            frame(w=530 d=330 h=350) {
            }
        """.trimIndent()
        val result = parser.parse(script).getOrThrow()
        assertEquals(emptyList<Double>(), result.frameBottomBeams)
    }

    @Test
    fun `comments and blank lines are ignored`() {
        val script = """
            # this is a comment
            frame(w=530 d=330 h=350)

            # another comment
            move(90 0 20.8) motherboard()
        """.trimIndent()

        val result = parser.parse(script).getOrThrow()
        assertEquals(530.0, result.frameWidth, 0.001)
        assertEquals(1, result.components.size)
    }
}
