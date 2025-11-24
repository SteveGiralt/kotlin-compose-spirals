package com.stevegiralt.spirals.fibonacci

/**
 * Data classes representing the state of the Fibonacci spiral.
 *
 * These classes encapsulate the geometric and layout information needed
 * to render the spiral visualization.
 */

/**
 * A 2D position in Cartesian coordinates.
 *
 * Uses Turtle graphics convention: origin at center, Y+ is up.
 * (Will be transformed to Canvas coordinates in the UI layer)
 *
 * @property x Horizontal position (negative = left, positive = right)
 * @property y Vertical position (negative = down, positive = up)
 */
data class Position(
    val x: Float,
    val y: Float
) {
    companion object {
        val ZERO = Position(0f, 0f)
    }

    operator fun plus(other: Position) = Position(x + other.x, y + other.y)
    operator fun minus(other: Position) = Position(x - other.x, y - other.y)
    operator fun times(scale: Float) = Position(x * scale, y * scale)
}

/**
 * A bounding box representing the extents of positioned squares.
 *
 * Critical for the position calculation algorithm - maintains the edges
 * of all placed squares to determine where the next square should go.
 *
 * @property minX Leftmost edge
 * @property minY Bottom edge
 * @property maxX Rightmost edge
 * @property maxY Top edge
 */
data class BoundingBox(
    val minX: Float,
    val minY: Float,
    val maxX: Float,
    val maxY: Float
) {
    /** Total width of the bounding box */
    val width: Float get() = maxX - minX

    /** Total height of the bounding box */
    val height: Float get() = maxY - minY

    companion object {
        /**
         * Creates a bounding box from a list of positioned squares.
         *
         * @param positions List of square positions
         * @param sizes List of square sizes (must match positions length)
         * @return BoundingBox encompassing all squares
         */
        fun fromSquares(positions: List<Position>, sizes: List<Float>): BoundingBox {
            require(positions.isNotEmpty()) { "Cannot create bounding box from empty list" }
            require(positions.size == sizes.size) { "Positions and sizes must have same length" }

            val minX = positions.minOf { it.x }
            val minY = positions.minOf { it.y }
            val maxX = positions.indices.maxOf { i -> positions[i].x + sizes[i] }
            val maxY = positions.indices.maxOf { i -> positions[i].y + sizes[i] }

            return BoundingBox(minX, minY, maxX, maxY)
        }
    }

    /**
     * Updates the bounding box to include a new square.
     *
     * @param position Position of the new square
     * @param size Size of the new square
     * @return New BoundingBox that includes the square
     */
    fun include(position: Position, size: Float): BoundingBox {
        return BoundingBox(
            minX = minOf(minX, position.x),
            minY = minOf(minY, position.y),
            maxX = maxOf(maxX, position.x + size),
            maxY = maxOf(maxY, position.y + size)
        )
    }
}

/**
 * Result of scaling and centering the spiral for display.
 *
 * Contains all the transformed geometry needed to render the spiral
 * on a specific canvas size.
 *
 * @property positions Scaled and centered positions for each square
 * @property sizes Scaled sizes for each square
 * @property scale The scale factor applied
 * @property rotated Whether the spiral was rotated 90° to better fit the canvas
 * @property diagnostics Human-readable debug information about the transformation
 */
data class ScalingResult(
    val positions: List<Position>,
    val sizes: List<Float>,
    val scale: Float,
    val rotated: Boolean = false,
    val diagnostics: String
) {
    init {
        require(positions.size == sizes.size) {
            "Positions and sizes must have same length"
        }
    }
}

/**
 * Represents canvas/screen dimensions.
 *
 * @property width Width in pixels
 * @property height Height in pixels
 */
data class CanvasSize(
    val width: Float,
    val height: Float
)

/**
 * Geometry for a single arc in the Fibonacci spiral.
 *
 * Each arc is a 90-degree quarter circle that connects smoothly to the next arc.
 * The center point is calculated to be perpendicular to the current heading direction.
 *
 * @property center Center point of the circular arc
 * @property radius Radius of the arc (equals the Fibonacci number for this segment)
 * @property startAngle Starting angle in degrees (0° = right, 90° = up, 180° = left, 270° = down)
 * @property sweepAngle How many degrees to sweep (always 90° for Fibonacci spiral)
 */
data class ArcGeometry(
    val center: Position,
    val radius: Float,
    val startAngle: Float,
    val sweepAngle: Float = 90f
)
