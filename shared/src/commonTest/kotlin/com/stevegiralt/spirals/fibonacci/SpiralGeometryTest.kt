package com.stevegiralt.spirals.fibonacci

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SpiralGeometryTest {

    // Tolerance for floating-point comparisons
    private val EPSILON = 0.001f

    private fun assertPositionEquals(expected: Position, actual: Position, message: String = "") {
        assertTrue(
            kotlin.math.abs(expected.x - actual.x) < EPSILON &&
            kotlin.math.abs(expected.y - actual.y) < EPSILON,
            "$message: Expected $expected but got $actual"
        )
    }

    @Test
    fun testCalculatePositions_n1() {
        val fibNumbers = listOf(1)
        val positions = SpiralGeometry.calculatePositions(fibNumbers)

        assertEquals(1, positions.size, "Should have 1 position")
        assertPositionEquals(Position(0f, 0f), positions[0], "Square 0 should be at origin")
    }

    @Test
    fun testCalculatePositions_n2() {
        val fibNumbers = listOf(1, 1)
        val positions = SpiralGeometry.calculatePositions(fibNumbers)

        assertEquals(2, positions.size, "Should have 2 positions")
        assertPositionEquals(Position(0f, 0f), positions[0], "Square 0 should be at origin")
        assertPositionEquals(Position(0f, 1f), positions[1], "Square 1 should be above square 0")
    }

    @Test
    fun testCalculatePositions_n3() {
        val fibNumbers = listOf(1, 1, 2)
        val positions = SpiralGeometry.calculatePositions(fibNumbers)

        assertEquals(3, positions.size, "Should have 3 positions")
        assertPositionEquals(Position(0f, 0f), positions[0], "Square 0 at origin")
        assertPositionEquals(Position(0f, 1f), positions[1], "Square 1 above")
        assertPositionEquals(Position(-2f, 0f), positions[2], "Square 2 to the left")
    }

    @Test
    fun testCalculatePositions_n5_critical() {
        // This is the CRITICAL test case from the Python project
        // Expected positions for n=5 with fib=[1,1,2,3,5]:
        // Square 0 (size 1): (0, 0)      - origin
        // Square 1 (size 1): (0, 1)      - above square 0
        // Square 2 (size 2): (-2, 0)     - left of bbox
        // Square 3 (size 3): (-2, -3)    - below bbox
        // Square 4 (size 5): (1, -3)     - right of bbox

        val fibNumbers = listOf(1, 1, 2, 3, 5)
        val positions = SpiralGeometry.calculatePositions(fibNumbers)

        assertEquals(5, positions.size, "Should have 5 positions")

        assertPositionEquals(Position(0f, 0f), positions[0], "Square 0 at origin")
        assertPositionEquals(Position(0f, 1f), positions[1], "Square 1 above square 0")
        assertPositionEquals(Position(-2f, 0f), positions[2], "Square 2 to the left")
        assertPositionEquals(Position(-2f, -3f), positions[3], "Square 3 below")
        assertPositionEquals(Position(1f, -3f), positions[4], "Square 4 to the right")
    }

    @Test
    fun testCalculatePositions_n8() {
        val fibNumbers = listOf(1, 1, 2, 3, 5, 8, 13, 21)
        val positions = SpiralGeometry.calculatePositions(fibNumbers)

        assertEquals(8, positions.size, "Should have 8 positions")

        // Verify first few positions
        assertPositionEquals(Position(0f, 0f), positions[0], "Square 0 at origin")
        assertPositionEquals(Position(0f, 1f), positions[1], "Square 1 above")
        assertPositionEquals(Position(-2f, 0f), positions[2], "Square 2 left")
        assertPositionEquals(Position(-2f, -3f), positions[3], "Square 3 down")
        assertPositionEquals(Position(1f, -3f), positions[4], "Square 4 right")
        assertPositionEquals(Position(-2f, 2f), positions[5], "Square 5 up")

        // Verify the cycle continues
        // After square 5 (UP), next should be LEFT again
        assertPositionEquals(Position(-15f, -3f), positions[6], "Square 6 left (cycle repeats)")
    }

    @Test
    fun testCalculatePositions_emptyList() {
        val positions = SpiralGeometry.calculatePositions(emptyList())
        assertEquals(0, positions.size, "Empty list should return empty positions")
    }

    @Test
    fun testCalculateBoundingBox_n5() {
        val fibNumbers = listOf(1, 1, 2, 3, 5)
        val positions = SpiralGeometry.calculatePositions(fibNumbers)
        val sizes = fibNumbers.map { it.toFloat() }

        val bbox = SpiralGeometry.calculateBoundingBox(positions, sizes)

        // For n=5:
        // minX = -2 (square 2)
        // maxX = 1 + 5 = 6 (square 4 extends to the right)
        // minY = -3 (square 3)
        // maxY = 1 + 1 = 2 (square 1 extends upward)

        assertEquals(-2f, bbox.minX, "minX should be -2")
        assertEquals(-3f, bbox.minY, "minY should be -3")
        assertEquals(6f, bbox.maxX, "maxX should be 6")
        assertEquals(2f, bbox.maxY, "maxY should be 2")

        assertEquals(8f, bbox.width, "Width should be 8")
        assertEquals(5f, bbox.height, "Height should be 5")
    }

    @Test
    fun testBoundingBox_fromSquares() {
        val positions = listOf(
            Position(0f, 0f),
            Position(0f, 1f),
            Position(-2f, 0f)
        )
        val sizes = listOf(1f, 1f, 2f)

        val bbox = BoundingBox.fromSquares(positions, sizes)

        assertEquals(-2f, bbox.minX)
        assertEquals(0f, bbox.minY)
        assertEquals(1f, bbox.maxX) // max(0+1, 0+1, -2+2) = 1
        assertEquals(2f, bbox.maxY) // max(0+1, 1+1, 0+2) = 2
    }

    @Test
    fun testBoundingBox_include() {
        val bbox = BoundingBox(0f, 0f, 1f, 1f)
        val newBbox = bbox.include(Position(-2f, -1f), 3f)

        assertEquals(-2f, newBbox.minX, "minX should extend left")
        assertEquals(-1f, newBbox.minY, "minY should extend down")
        assertEquals(1f, newBbox.maxX, "maxX should remain")
        assertEquals(2f, newBbox.maxY, "maxY should extend up")
    }

    @Test
    fun testCalculateScale_basicCase() {
        val bbox = BoundingBox(-2f, -3f, 6f, 2f) // width=8, height=5
        val canvasSize = CanvasSize(800f, 600f)

        val scale = SpiralGeometry.calculateScale(bbox, canvasSize)

        // Margin factor is now 0.90 (90% of screen)
        // scaleX = 800 * 0.90 / 8 = 90
        // scaleY = 600 * 0.90 / 5 = 108
        // scale = min(90, 108) = 90

        assertTrue(
            kotlin.math.abs(scale - 90f) < 0.1f,
            "Scale should be approximately 90, got $scale"
        )
    }

    @Test
    fun testCalculateScale_heightConstrained() {
        val bbox = BoundingBox(-10f, -10f, 20f, 15f) // width=30, height=25
        val canvasSize = CanvasSize(1000f, 800f)

        val scale = SpiralGeometry.calculateScale(bbox, canvasSize)

        // Margin factor is 0.90
        // scaleX = 1000 * 0.90 / 30 = 30
        // scaleY = 800 * 0.90 / 25 = 28.8
        // scale = min(30, 28.8) = 28.8

        assertTrue(
            kotlin.math.abs(scale - 28.8f) < 0.1f,
            "Scale should be approximately 28.8, got $scale"
        )
    }

    @Test
    fun testCalculateScale_usesMoreRestrictiveDimension() {
        // Verify that the more restrictive dimension is used
        val bbox = BoundingBox(0f, 0f, 100f, 50f) // width=100, height=50 (2:1 aspect)
        val canvasSize = CanvasSize(1000f, 1000f) // square canvas

        val scale = SpiralGeometry.calculateScale(bbox, canvasSize)

        // With 90% margin:
        // scaleX = 1000 * 0.90 / 100 = 9
        // scaleY = 1000 * 0.90 / 50 = 18
        // scale = min(9, 18) = 9 (width constrained)

        assertTrue(
            kotlin.math.abs(scale - 9f) < 0.1f,
            "Scale should be 9 (width constrained), got $scale"
        )
    }

    @Test
    fun testScaleAndCenter_n5() {
        val fibNumbers = listOf(1, 1, 2, 3, 5)
        val positions = SpiralGeometry.calculatePositions(fibNumbers)
        val sizes = fibNumbers.map { it.toFloat() }
        val canvasSize = CanvasSize(800f, 600f)

        val result = SpiralGeometry.scaleAndCenter(positions, sizes, 5, canvasSize)

        assertEquals(5, result.positions.size, "Should have 5 positions")
        assertEquals(5, result.sizes.size, "Should have 5 sizes")
        assertTrue(result.scale > 0f, "Scale should be positive")
        assertTrue(result.diagnostics.isNotEmpty(), "Should have diagnostics")

        // Verify that the spiral is approximately centered
        // The centered positions should have a bounding box centered around (0, 0)
        val centeredBbox = SpiralGeometry.calculateBoundingBox(result.positions, result.sizes)
        val centerX = (centeredBbox.minX + centeredBbox.maxX) / 2
        val centerY = (centeredBbox.minY + centeredBbox.maxY) / 2

        assertTrue(
            kotlin.math.abs(centerX) < 1f,
            "Spiral should be centered horizontally (centerX=$centerX)"
        )
        assertTrue(
            kotlin.math.abs(centerY) < 1f,
            "Spiral should be centered vertically (centerY=$centerY)"
        )
    }

    @Test
    fun testScaleAndCenter_diagnostics() {
        val fibNumbers = listOf(1, 1, 2, 3, 5)
        val positions = SpiralGeometry.calculatePositions(fibNumbers)
        val sizes = fibNumbers.map { it.toFloat() }
        val canvasSize = CanvasSize(800f, 600f)

        val result = SpiralGeometry.scaleAndCenter(positions, sizes, 5, canvasSize)

        // Verify diagnostics contains key information
        assertTrue(result.diagnostics.contains("n = 5"), "Should contain n value")
        assertTrue(result.diagnostics.contains("Canvas:"), "Should contain canvas size")
        assertTrue(result.diagnostics.contains("Scale factor:"), "Should contain scale factor")
        assertTrue(result.diagnostics.contains("Rotated:"), "Should contain rotation info")
    }

    @Test
    fun testShouldRotate_tallCanvasWithWideSpiral() {
        // Portrait canvas with landscape spiral should rotate (when rotation allowed)
        val wideBbox = BoundingBox(0f, 0f, 100f, 50f) // 2:1 aspect (wide)
        val tallCanvas = CanvasSize(400f, 800f) // 1:2 aspect (tall)

        val shouldRotate = SpiralGeometry.shouldRotate(wideBbox, tallCanvas, allowRotation = true)
        assertTrue(shouldRotate, "Wide spiral should rotate to fit tall canvas")
    }

    @Test
    fun testShouldRotate_matchingAspects() {
        // If aspects roughly match, shouldn't rotate
        val bbox = BoundingBox(0f, 0f, 100f, 150f) // ~2:3 aspect
        val canvas = CanvasSize(400f, 600f) // 2:3 aspect

        val shouldRotate = SpiralGeometry.shouldRotate(bbox, canvas, allowRotation = true)
        // Should not rotate since aspects are similar
        assertTrue(!shouldRotate, "Matching aspects should not rotate")
    }

    @Test
    fun testShouldRotate_disabledByDefault() {
        // When allowRotation is false (default), should never rotate
        val wideBbox = BoundingBox(0f, 0f, 100f, 50f) // 2:1 aspect (wide)
        val tallCanvas = CanvasSize(400f, 800f) // 1:2 aspect (tall)

        val shouldRotate = SpiralGeometry.shouldRotate(wideBbox, tallCanvas) // default allowRotation = false
        assertTrue(!shouldRotate, "Should not rotate when allowRotation is false")
    }
}
