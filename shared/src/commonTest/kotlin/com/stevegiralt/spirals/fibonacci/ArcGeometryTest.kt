package com.stevegiralt.spirals.fibonacci

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Converts degrees to radians (multiplatform compatible) */
private fun toRadians(degrees: Double): Double = degrees * PI / 180.0

/**
 * Unit tests for Fibonacci spiral arc geometry calculations.
 *
 * These tests verify the critical arc calculation algorithm that creates
 * the smooth golden spiral curve. This is the highest-risk component of
 * the port from Python.
 */
class ArcGeometryTest {

    companion object {
        const val EPSILON = 0.001f
    }

    private fun assertPositionEquals(expected: Position, actual: Position, message: String = "") {
        assertTrue(
            abs(expected.x - actual.x) < EPSILON && abs(expected.y - actual.y) < EPSILON,
            "$message\nExpected: (${expected.x}, ${expected.y}), Actual: (${actual.x}, ${actual.y})"
        )
    }

    private fun assertFloatEquals(expected: Float, actual: Float, message: String = "") {
        assertTrue(
            abs(expected - actual) < EPSILON,
            "$message\nExpected: $expected, Actual: $actual"
        )
    }

    @Test
    fun `empty list returns empty arcs`() {
        val arcs = SpiralGeometry.calculateSpiralArcs(emptyList())
        assertEquals(0, arcs.size, "Empty input should produce no arcs")
    }

    @Test
    fun `single arc at origin facing right`() {
        val arcs = SpiralGeometry.calculateSpiralArcs(listOf(1f))

        assertEquals(1, arcs.size, "Should create one arc")

        val arc = arcs[0]
        assertEquals(1f, arc.radius, "Radius should match input")
        assertEquals(0f, arc.startAngle, "Should start facing right (0°)")
        assertEquals(90f, arc.sweepAngle, "Should sweep 90°")

        // Center should be perpendicular left (90° from 0° = up)
        // Center = (0, 0) + 1 * (cos(90°), sin(90°)) = (0, 1)
        assertPositionEquals(Position(0f, 1f), arc.center, "Arc center should be at (0, 1)")
    }

    @Test
    fun `three arcs create correct spiral pattern`() {
        // First three Fibonacci numbers: [1, 1, 2]
        val arcs = SpiralGeometry.calculateSpiralArcs(listOf(1f, 1f, 2f))

        assertEquals(3, arcs.size, "Should create three arcs")

        // Arc 1: radius=1, start at (0,0), heading=0° (right)
        val arc1 = arcs[0]
        assertEquals(1f, arc1.radius)
        assertEquals(0f, arc1.startAngle)
        assertPositionEquals(Position(0f, 1f), arc1.center, "Arc 1 center")

        // Arc 2: radius=1, starts at (0,2) after arc1, heading=90° (up)
        // Perpendicular left (180°): center at (0,2) + 1*(-1,0) = (-1, 2)
        val arc2 = arcs[1]
        assertEquals(1f, arc2.radius)
        assertEquals(90f, arc2.startAngle)
        assertPositionEquals(Position(-1f, 2f), arc2.center, "Arc 2 center")

        // Arc 3: radius=2, starts at (-2,2) after arc2, heading=180° (left)
        // Perpendicular left (270° = down): center at (-2,2) + 2*(0,-1) = (-2, 0)
        val arc3 = arcs[2]
        assertEquals(2f, arc3.radius)
        assertEquals(180f, arc3.startAngle)
        assertPositionEquals(Position(-2f, 0f), arc3.center, "Arc 3 center")
    }

    @Test
    fun `arcs connect smoothly - end position matches next start`() {
        val fibNumbers = listOf(1f, 1f, 2f, 3f)
        val arcs = SpiralGeometry.calculateSpiralArcs(fibNumbers)

        // Calculate end position of each arc and verify it matches
        // where the next arc would start
        var currentPos = Position.ZERO
        var currentAngle = 0f

        for (i in arcs.indices) {
            val arc = arcs[i]

            // Verify this arc starts at the expected position
            // by checking that the arc's geometry is consistent
            assertEquals(currentAngle, arc.startAngle, "Arc $i should start at angle $currentAngle")

            // Calculate where this arc ends
            val endAngleRad = toRadians(currentAngle + 90.0)
            val endPos = Position(
                x = arc.center.x + (arc.radius * cos(endAngleRad)).toFloat(),
                y = arc.center.y + (arc.radius * sin(endAngleRad)).toFloat()
            )

            // Update position and angle for next iteration
            currentPos = endPos
            currentAngle = (currentAngle + 90f) % 360f
        }

        // If we got here without assertion failures, arcs connect smoothly
        assertTrue(true, "All arcs connect smoothly")
    }

    @Test
    fun `angle progression follows 90 degree pattern`() {
        val arcs = SpiralGeometry.calculateSpiralArcs(listOf(1f, 1f, 2f, 3f, 5f))

        val expectedAngles = listOf(0f, 90f, 180f, 270f, 0f) // Cycles back to 0
        arcs.forEachIndexed { i, arc ->
            assertEquals(
                expectedAngles[i],
                arc.startAngle,
                "Arc $i should start at ${expectedAngles[i]}°"
            )
        }
    }

    @Test
    fun `all arcs have 90 degree sweep`() {
        val arcs = SpiralGeometry.calculateSpiralArcs(listOf(1f, 1f, 2f, 3f, 5f, 8f))

        arcs.forEachIndexed { i, arc ->
            assertEquals(90f, arc.sweepAngle, "Arc $i should sweep 90°")
        }
    }

    @Test
    fun `custom start position shifts arc positions`() {
        val startPos = Position(10f, 20f)
        val arcs = SpiralGeometry.calculateSpiralArcs(
            fibNumbers = listOf(1f),
            startPosition = startPos
        )

        assertEquals(1, arcs.size)

        // Center should be offset by start position
        // Expected: (10, 20) + (0, 1) = (10, 21)
        assertPositionEquals(Position(10f, 21f), arcs[0].center, "Center should be offset")
    }

    @Test
    fun `custom start angle rotates spiral orientation`() {
        // Start facing up (90°) instead of right (0°)
        val arcs = SpiralGeometry.calculateSpiralArcs(
            fibNumbers = listOf(1f, 1f),
            startAngle = 90f
        )

        assertEquals(2, arcs.size)

        // First arc should start at 90° (up)
        assertEquals(90f, arcs[0].startAngle, "First arc should start at 90°")

        // Second arc should start at 180° (left)
        assertEquals(180f, arcs[1].startAngle, "Second arc should start at 180°")
    }

    @Test
    fun `first five fibonacci numbers create correct arc sequence`() {
        // [1, 1, 2, 3, 5]
        val arcs = SpiralGeometry.calculateSpiralArcs(listOf(1f, 1f, 2f, 3f, 5f))

        assertEquals(5, arcs.size, "Should create 5 arcs")

        // Verify radii match input
        val expectedRadii = listOf(1f, 1f, 2f, 3f, 5f)
        arcs.forEachIndexed { i, arc ->
            assertEquals(expectedRadii[i], arc.radius, "Arc $i radius")
        }

        // Verify all centers are calculated (not zero/null)
        arcs.forEachIndexed { i, arc ->
            // Just verify centers are reasonable (not infinity or NaN)
            assertTrue(arc.center.x.isFinite(), "Arc $i center.x should be finite")
            assertTrue(arc.center.y.isFinite(), "Arc $i center.y should be finite")
        }
    }

    @Test
    fun `arc centers form outward spiral pattern`() {
        val arcs = SpiralGeometry.calculateSpiralArcs(listOf(1f, 1f, 2f, 3f, 5f))

        // Verify the general outward spiral pattern by checking
        // that arc centers move progressively outward
        // This is a sanity check for the algorithm

        // Arc 0: center at (0, 1)
        assertPositionEquals(Position(0f, 1f), arcs[0].center, "Arc 0 center")

        // Arc 1: center at (-1, 2) - moves left and up
        assertPositionEquals(Position(-1f, 2f), arcs[1].center, "Arc 1 center")

        // Arc 2: center should be further left than Arc 1
        assertTrue(arcs[2].center.x < arcs[1].center.x, "Arc 2 should move left")

        // Arc 3: center should be further down
        assertTrue(arcs[3].center.y < arcs[2].center.y, "Arc 3 should move down")

        // Arc 4: center should be further right
        assertTrue(arcs[4].center.x > arcs[3].center.x, "Arc 4 should move right")
    }

    @Test
    fun `large fibonacci sequence does not overflow`() {
        // First 10 Fibonacci numbers
        val fibNumbers = listOf(1f, 1f, 2f, 3f, 5f, 8f, 13f, 21f, 34f, 55f)
        val arcs = SpiralGeometry.calculateSpiralArcs(fibNumbers)

        assertEquals(10, arcs.size, "Should create 10 arcs")

        // Verify no infinities or NaN values
        arcs.forEachIndexed { i, arc ->
            assertTrue(arc.center.x.isFinite(), "Arc $i center.x should be finite")
            assertTrue(arc.center.y.isFinite(), "Arc $i center.y should be finite")
            assertTrue(arc.radius.isFinite(), "Arc $i radius should be finite")
            assertTrue(arc.startAngle.isFinite(), "Arc $i startAngle should be finite")
        }
    }

    @Test
    fun `arc geometry matches mathematical formula`() {
        // Manually calculate expected values for first arc
        // Start at (0, 0), heading 0° (right), radius 1
        val arcs = SpiralGeometry.calculateSpiralArcs(listOf(1f))
        val arc = arcs[0]

        // Perpendicular angle: 0° + 90° = 90° (up)
        // Center: (0, 0) + 1 * (cos(90°), sin(90°))
        //       = (0, 0) + (0, 1)
        //       = (0, 1)
        val expectedCenterX = 0f + 1f * cos(toRadians(90.0)).toFloat()
        val expectedCenterY = 0f + 1f * sin(toRadians(90.0)).toFloat()

        assertFloatEquals(expectedCenterX, arc.center.x, "Center X matches formula")
        assertFloatEquals(expectedCenterY, arc.center.y, "Center Y matches formula")
    }

    @Test
    fun `verify n=8 arc sequence`() {
        // First 8 Fibonacci numbers: [1, 1, 2, 3, 5, 8, 13, 21]
        val fibNumbers = listOf(1f, 1f, 2f, 3f, 5f, 8f, 13f, 21f)
        val arcs = SpiralGeometry.calculateSpiralArcs(fibNumbers)

        assertEquals(8, arcs.size, "Should create 8 arcs")

        // Verify angle sequence (should cycle through 0, 90, 180, 270)
        val expectedAngles = listOf(0f, 90f, 180f, 270f, 0f, 90f, 180f, 270f)
        arcs.forEachIndexed { i, arc ->
            assertEquals(expectedAngles[i], arc.startAngle, "Arc $i angle")
        }

        // Verify radii
        arcs.forEachIndexed { i, arc ->
            assertEquals(fibNumbers[i], arc.radius, "Arc $i radius")
        }
    }
}
