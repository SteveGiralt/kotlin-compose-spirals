package com.stevegiralt.spirals.fibonacci

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/** Converts degrees to radians (multiplatform compatible) */
private fun toRadians(degrees: Double): Double = degrees * PI / 180.0

/**
 * Core geometry calculations for positioning and scaling the Fibonacci spiral.
 *
 * This implements the critical bounding box algorithm for positioning squares
 * and the two-dimensional scaling with adaptive margins.
 */
object SpiralGeometry {

    /**
     * Calculates positions for each square in the Fibonacci spiral.
     *
     * Uses the bounding box algorithm to correctly place squares:
     * - Square 0: at origin (0, 0)
     * - Square 1: ABOVE square 0
     * - Then cycle: LEFT, DOWN, RIGHT, UP (repeating)
     *
     * Expected positions for n=5 with [1,1,2,3,5]:
     * - Square 0 (size 1): (0, 0)
     * - Square 1 (size 1): (0, 1)
     * - Square 2 (size 2): (-2, 0)
     * - Square 3 (size 3): (-2, -3)
     * - Square 4 (size 5): (1, -3)
     *
     * @param fibNumbers List of Fibonacci numbers representing square sizes
     * @return List of positions for each square
     */
    fun calculatePositions(fibNumbers: List<Int>): List<Position> {
        if (fibNumbers.isEmpty()) return emptyList()

        val positions = mutableListOf<Position>()
        val sizes = fibNumbers.map { it.toFloat() }

        // Square 0: at origin
        positions.add(Position.ZERO)

        if (fibNumbers.size == 1) return positions

        // Square 1: above square 0
        val firstSize = sizes[0]
        positions.add(Position(0f, firstSize))

        if (fibNumbers.size == 2) return positions

        // Initialize bounding box with first two squares
        var bbox = BoundingBox(
            minX = 0f,
            minY = 0f,
            maxX = firstSize,
            maxY = sizes[0] + sizes[1]
        )

        // Direction cycle: LEFT, DOWN, RIGHT, UP (repeating)
        val directions = listOf("LEFT", "DOWN", "RIGHT", "UP")
        var directionIndex = 0

        // Place remaining squares
        for (i in 2 until fibNumbers.size) {
            val size = sizes[i]
            val direction = directions[directionIndex % 4]

            val position = when (direction) {
                "LEFT" -> Position(bbox.minX - size, bbox.minY)
                "DOWN" -> Position(bbox.minX, bbox.minY - size)
                "RIGHT" -> Position(bbox.maxX, bbox.minY)
                "UP" -> Position(bbox.minX, bbox.maxY)
                else -> throw IllegalStateException("Unknown direction: $direction")
            }

            positions.add(position)

            // Update bounding box to include new square
            bbox = bbox.include(position, size)

            directionIndex++
        }

        return positions
    }

    /**
     * Calculates the bounding box encompassing all positioned squares.
     *
     * @param positions List of square positions
     * @param sizes List of square sizes (must match positions length)
     * @return BoundingBox encompassing all squares
     */
    fun calculateBoundingBox(positions: List<Position>, sizes: List<Float>): BoundingBox {
        return BoundingBox.fromSquares(positions, sizes)
    }

    /**
     * Calculates the scale factor needed to fit the spiral on screen.
     *
     * Uses high utilization of available space (~90%).
     *
     * @param bbox Bounding box of the unscaled spiral
     * @param canvasSize Dimensions of the canvas/screen
     * @return Scale factor to apply to positions and sizes
     */
    fun calculateScale(bbox: BoundingBox, canvasSize: CanvasSize): Float {
        // Use 90% of available space
        val marginFactor = 0.90f

        // Two-dimensional scaling - use the more restrictive dimension
        val scaleX = (canvasSize.width * marginFactor) / bbox.width
        val scaleY = (canvasSize.height * marginFactor) / bbox.height

        return min(scaleX, scaleY)
    }

    /**
     * Determines if the spiral should be rotated 90° to better fit the canvas.
     *
     * NOTE: Rotation is currently disabled because rotating individual corner
     * positions breaks the edge-sharing relationship between squares.
     * A proper implementation would need to recalculate positions after rotation.
     *
     * @param bbox Bounding box of the unscaled spiral
     * @param canvasSize Dimensions of the canvas/screen
     * @return true if rotation would improve space utilization
     */
    fun shouldRotate(bbox: BoundingBox, canvasSize: CanvasSize): Boolean {
        // Rotation disabled - breaks square alignment
        return false
    }

    /**
     * Scales and centers the spiral for display on a specific canvas size.
     *
     * This is the complete transformation pipeline:
     * 1. Calculate unscaled bounding box
     * 2. Determine if rotation improves space utilization
     * 3. Rotate positions if beneficial
     * 4. Calculate scale factor
     * 5. Scale all positions and sizes
     * 6. Recalculate scaled bounding box
     * 7. Calculate centering offset
     * 8. Apply offset to center the spiral
     *
     * @param positions Unscaled positions of squares
     * @param sizes Unscaled sizes of squares
     * @param n Number of Fibonacci squares (for diagnostics)
     * @param canvasSize Dimensions of the canvas/screen
     * @return ScalingResult with transformed positions, sizes, and diagnostics
     */
    fun scaleAndCenter(
        positions: List<Position>,
        sizes: List<Float>,
        n: Int,
        canvasSize: CanvasSize
    ): ScalingResult {
        require(positions.isNotEmpty()) { "Cannot scale empty spiral" }
        require(positions.size == sizes.size) { "Positions and sizes must match" }

        // Step 1: Calculate unscaled bounding box
        val unscaledBbox = calculateBoundingBox(positions, sizes)

        // Step 2: Determine if rotation improves space utilization
        val rotate = shouldRotate(unscaledBbox, canvasSize)

        // Step 3: Rotate positions if beneficial (90° counter-clockwise: x' = -y, y' = x)
        val transformedPositions = if (rotate) {
            positions.map { pos -> Position(-pos.y, pos.x) }
        } else {
            positions
        }

        // Recalculate bbox after rotation
        val transformedBbox = if (rotate) {
            calculateBoundingBox(transformedPositions, sizes)
        } else {
            unscaledBbox
        }

        // Step 4: Calculate scale factor
        val scale = calculateScale(transformedBbox, canvasSize)

        // Step 5: Scale everything
        val scaledPositions = transformedPositions.map { it * scale }
        val scaledSizes = sizes.map { it * scale }

        // Step 6: Recalculate scaled bounding box
        val scaledBbox = calculateBoundingBox(scaledPositions, scaledSizes)

        // Step 7: Calculate centering offset
        val offsetX = -scaledBbox.width / 2 - scaledBbox.minX
        val offsetY = -scaledBbox.height / 2 - scaledBbox.minY

        // Step 8: Apply offset to center
        val centeredPositions = scaledPositions.map { pos ->
            Position(pos.x + offsetX, pos.y + offsetY)
        }

        // Generate diagnostics
        val diagnostics = buildString {
            appendLine("Spiral Scaling Diagnostics:")
            appendLine("  n = $n")
            appendLine("  Canvas: ${canvasSize.width} x ${canvasSize.height}")
            appendLine("  Unscaled bbox: ${unscaledBbox.width} x ${unscaledBbox.height}")
            appendLine("  Rotated: $rotate")
            appendLine("  Scale factor: $scale")
            appendLine("  Scaled bbox: ${scaledBbox.width} x ${scaledBbox.height}")
        }

        return ScalingResult(
            positions = centeredPositions,
            sizes = scaledSizes,
            scale = scale,
            rotated = rotate,
            diagnostics = diagnostics.trim()
        )
    }

    /**
     * Calculates the arc geometries for drawing the Fibonacci spiral.
     *
     * This is the critical algorithm that creates the smooth golden spiral curve.
     * Each arc is a 90-degree quarter circle. The Python turtle graphics naturally
     * handled arc connections, but we must calculate each arc's geometry manually.
     *
     * Algorithm:
     * - Start at (0, 0) facing right (angle = 0°)
     * - For each Fibonacci number (radius):
     *   1. Calculate arc center: perpendicular left of current heading
     *   2. Create arc with current heading as start angle
     *   3. Calculate end position (becomes next arc's start)
     *   4. Rotate heading 90° counter-clockwise
     *
     * The arcs naturally connect because:
     * - Each arc's end position is exactly where the next arc starts
     * - Each arc rotates the heading by 90°
     * - The geometry is inherent in the quarter-circle construction
     *
     * @param fibNumbers List of Fibonacci numbers (as Float for precision)
     * @param startPosition Starting position for the spiral (default: origin)
     * @param startAngle Initial heading in degrees (default: 0° = facing right)
     * @return List of ArcGeometry objects defining each quarter-circle arc
     */
    fun calculateSpiralArcs(
        fibNumbers: List<Float>,
        startPosition: Position = Position.ZERO,
        startAngle: Float = 0f
    ): List<ArcGeometry> {
        if (fibNumbers.isEmpty()) return emptyList()

        val arcs = mutableListOf<ArcGeometry>()
        var currentAngle = startAngle
        var currentPos = startPosition

        for (radius in fibNumbers) {
            // Convert angles to radians for trigonometry
            val currentAngleRad = toRadians(currentAngle.toDouble())

            // Perpendicular angle: 90° counter-clockwise from current heading
            val perpAngleRad = toRadians(currentAngle + 90.0)

            // Calculate arc center: perpendicular left of current position
            val centerX = currentPos.x + (radius * cos(perpAngleRad)).toFloat()
            val centerY = currentPos.y + (radius * sin(perpAngleRad)).toFloat()

            // Create arc geometry
            arcs.add(
                ArcGeometry(
                    center = Position(centerX, centerY),
                    radius = radius,
                    startAngle = currentAngle,
                    sweepAngle = 90f
                )
            )

            // Calculate end position for this arc (becomes next arc's start)
            val endAngleRad = toRadians(currentAngle + 90.0)
            currentPos = Position(
                x = centerX + (radius * cos(endAngleRad)).toFloat(),
                y = centerY + (radius * sin(endAngleRad)).toFloat()
            )

            // Rotate heading 90° counter-clockwise
            currentAngle = (currentAngle + 90f) % 360f
        }

        return arcs
    }
}
