package com.stevegiralt.spirals.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import com.stevegiralt.spirals.allowSpiralRotation
import com.stevegiralt.spirals.fibonacci.*
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/** Converts degrees to radians (multiplatform compatible) */
private fun toRadians(degrees: Double): Double = degrees * PI / 180.0

/** Converts radians to degrees (multiplatform compatible) */
private fun toDegrees(radians: Double): Double = radians * 180.0 / PI

/**
 * Color scheme using Material Design 3 colors with WCAG AA contrast compliance
 * All colors have minimum 3:1 contrast ratio against white background
 */
object SpiralColors {
    val SQUARE_COLORS = listOf(
        Color(0xFFD32F2F), // Red 700 - contrast 4.6:1
        Color(0xFF1565C0), // Blue 800 - contrast 5.5:1
        Color(0xFF2E7D32), // Green 800 - contrast 4.5:1
        Color(0xFF6A1B9A), // Purple 800 - contrast 7.3:1
        Color(0xFFE65100), // Orange 900 - contrast 3.9:1
        Color(0xFF00695C), // Teal 800 - contrast 5.0:1
        Color(0xFFC2185B), // Pink 700 - contrast 4.9:1
        Color(0xFF4E342E)  // Brown 800 - contrast 7.5:1
    )
    val SPIRAL_COLOR = Color(0xFFBF360C) // Deep Orange 900 - contrast 5.8:1
    val LABEL_COLOR = Color(0xFF000000)  // Black
}

/**
 * Transform coordinates from Turtle convention (origin center, Y+ up)
 * to Canvas convention (origin top-left, Y+ down)
 */
fun toCanvasCoords(position: Position, canvasSize: Size): Offset {
    return Offset(
        x = position.x + canvasSize.width / 2,
        y = canvasSize.height / 2 - position.y  // Flip Y axis
    )
}

/**
 * Main canvas composable that renders the Fibonacci spiral
 * @param n Number of Fibonacci squares to render
 * @param animationProgress Progress of the animation (0.0 to 1.0)
 * @param modifier Modifier for the canvas
 */
@Composable
fun SpiralCanvas(
    n: Int,
    animationProgress: Float = 1f,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    // Calculate all geometry data
    val spiralData = remember(n) {
        if (n <= 0) return@remember null

        val fibNumbers = FibonacciGenerator.generateSequence(n)
        val positions = SpiralGeometry.calculatePositions(fibNumbers)
        val sizes = fibNumbers.map { it.toFloat() }

        // Calculate spiral arcs
        val arcs = SpiralGeometry.calculateSpiralArcs(sizes)

        SpiralRenderData(
            fibNumbers = fibNumbers,
            positions = positions,
            sizes = sizes,
            arcs = arcs
        )
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        if (spiralData == null) return@Canvas

        // Scale and center the spiral to fit the canvas
        val canvasSize = CanvasSize(size.width, size.height)
        val scalingResult = SpiralGeometry.scaleAndCenter(
            positions = spiralData.positions,
            sizes = spiralData.sizes,
            n = n,
            canvasSize = canvasSize,
            allowRotation = allowSpiralRotation
        )

        // Transform to canvas coordinates
        val canvasPositions = scalingResult.positions.map { position ->
            toCanvasCoords(position, size)
        }

        // Calculate the centering offset that was applied to squares
        // Original square 0 was at (0, 0), after scaling and centering it's at scalingResult.positions[0]
        // So the total offset (including scaling) is scalingResult.positions[0]
        // But we need just the centering part. After scaling, (0,0) is still (0,0).
        // The centering offset is what was added to get to scalingResult.positions[0]
        val centeringOffset = scalingResult.positions[0]

        // Apply the SAME transformation to arc centers: scale, then add same centering offset
        val scaledAndCenteredArcs = spiralData.arcs.map { arc ->
            val scaledCenter = arc.center * scalingResult.scale
            ArcGeometry(
                center = scaledCenter + centeringOffset,
                radius = arc.radius * scalingResult.scale,
                startAngle = arc.startAngle,
                sweepAngle = arc.sweepAngle
            )
        }

        // Animation in two phases:
        // Phase 1 (0.0 - 0.5): Squares appear one by one
        // Phase 2 (0.5 - 1.0): All squares visible, spiral draws smoothly

        val squarePhaseProgress = (animationProgress * 2f).coerceAtMost(1f)
        val spiralPhaseProgress = ((animationProgress - 0.5f) * 2f).coerceIn(0f, 1f)

        val visibleSquareCount = (n * squarePhaseProgress).toInt().coerceIn(0, n)

        // Apply canvas rotation if needed (rotate 90° CCW around canvas center)
        withTransform({
            if (scalingResult.rotated) {
                rotate(degrees = -90f, pivot = Offset(size.width / 2, size.height / 2))
            }
        }) {
        // 1. Draw squares with color cycling (show all during spiral phase)
        canvasPositions.take(visibleSquareCount).forEachIndexed { index, pos ->
            val scaledSize = scalingResult.sizes[index]
            val color = SpiralColors.SQUARE_COLORS[index % SpiralColors.SQUARE_COLORS.size]
            // pos is bottom-left in Canvas coords, but drawRect needs top-left
            // Since Y+ is down in Canvas, top-left is at y - size
            drawRect(
                color = color,
                topLeft = Offset(pos.x, pos.y - scaledSize),
                size = Size(scaledSize, scaledSize),
                style = Stroke(width = 2f)
            )
        }

        // 2. Draw Fibonacci number labels (only for visible squares with sufficient size)
        // Minimum size threshold: don't draw labels on squares smaller than 40px
        val minLabelSize = 40f
        canvasPositions.take(visibleSquareCount).forEachIndexed { index, pos ->
            val scaledSize = scalingResult.sizes[index]

            // Skip labels for squares that are too small to read
            if (scaledSize < minLabelSize) return@forEachIndexed

            val fibNumber = spiralData.fibNumbers[index]
            val text = fibNumber.toString()

            // Measure text to center it
            val fontSize = (scaledSize * 0.3f).coerceIn(12f, 48f)
            val textLayoutResult = textMeasurer.measure(
                text = text,
                style = TextStyle(
                    color = SpiralColors.LABEL_COLOR,
                    fontSize = fontSize.sp
                )
            )

            // Center the text in the square (accounting for bottom-left to top-left conversion)
            val squareTopLeft = Offset(pos.x, pos.y - scaledSize)
            val squareCenter = Offset(
                x = squareTopLeft.x + scaledSize / 2,
                y = squareTopLeft.y + scaledSize / 2
            )
            val textOffset = Offset(
                x = squareTopLeft.x + (scaledSize - textLayoutResult.size.width) / 2,
                y = squareTopLeft.y + (scaledSize - textLayoutResult.size.height) / 2
            )

            // Counter-rotate text when canvas is rotated so labels appear upright
            if (scalingResult.rotated) {
                withTransform({
                    rotate(degrees = 90f, pivot = squareCenter)
                }) {
                    drawText(
                        textLayoutResult = textLayoutResult,
                        topLeft = textOffset
                    )
                }
            } else {
                drawText(
                    textLayoutResult = textLayoutResult,
                    topLeft = textOffset
                )
            }
        }

        // 3. Draw spiral arc - draws smoothly during phase 2
        val spiralPath = Path().apply {
            if (scalingResult.sizes.isNotEmpty() && spiralPhaseProgress > 0f) {
                // Start at centered origin
                var currentX = centeringOffset.x
                var currentY = centeringOffset.y
                // Initial heading is 0 (facing right)
                var currentHeading = 0f

                val startCanvas = toCanvasCoords(Position(currentX, currentY), size)
                moveTo(startCanvas.x, startCanvas.y)

                // Calculate how many complete arcs and partial arc amount
                val totalArcProgress = n * spiralPhaseProgress
                val completeArcs = totalArcProgress.toInt()
                val partialArcAmount = totalArcProgress - completeArcs

                // Draw complete arcs
                scalingResult.sizes.take(completeArcs).forEach { radius ->
                    // Calculate center: perpendicular LEFT of current heading
                    // In turtle coords, perpendicular left is heading + 90
                    val perpAngleRad = toRadians(currentHeading + 90.0)
                    val centerX = currentX + (radius * cos(perpAngleRad)).toFloat()
                    val centerY = currentY + (radius * sin(perpAngleRad)).toFloat()

                    // Calculate the angle from center to current position
                    val angleFromCenter = toDegrees(
                        atan2((currentY - centerY).toDouble(), (currentX - centerX).toDouble())
                    ).toFloat()

                    // Convert center to canvas coords
                    val canvasCenter = toCanvasCoords(Position(centerX, centerY), size)

                    // Convert angles to canvas convention (Y-axis flipped)
                    val canvasStartAngle = -angleFromCenter
                    val canvasSweepAngle = -90f  // Sweep 90 degrees counter-clockwise in turtle = clockwise in canvas

                    // Draw the arc
                    arcTo(
                        rect = Rect(
                            left = canvasCenter.x - radius,
                            top = canvasCenter.y - radius,
                            right = canvasCenter.x + radius,
                            bottom = canvasCenter.y + radius
                        ),
                        startAngleDegrees = canvasStartAngle,
                        sweepAngleDegrees = canvasSweepAngle,
                        forceMoveTo = false
                    )

                    // Update position: end of arc is 90 degrees further around the circle
                    val endAngleRad = toRadians(angleFromCenter + 90.0)
                    currentX = centerX + (radius * cos(endAngleRad)).toFloat()
                    currentY = centerY + (radius * sin(endAngleRad)).toFloat()

                    // Update heading: rotated 90 degrees counter-clockwise
                    currentHeading = (currentHeading + 90f) % 360f
                }

                // Draw partial arc if needed
                if (partialArcAmount > 0f && completeArcs < scalingResult.sizes.size) {
                    val radius = scalingResult.sizes[completeArcs]

                    // Calculate center: perpendicular LEFT of current heading
                    val perpAngleRad = toRadians(currentHeading + 90.0)
                    val centerX = currentX + (radius * cos(perpAngleRad)).toFloat()
                    val centerY = currentY + (radius * sin(perpAngleRad)).toFloat()

                    // Calculate the angle from center to current position
                    val angleFromCenter = toDegrees(
                        atan2((currentY - centerY).toDouble(), (currentX - centerX).toDouble())
                    ).toFloat()

                    // Convert center to canvas coords
                    val canvasCenter = toCanvasCoords(Position(centerX, centerY), size)

                    // Convert angles to canvas convention (Y-axis flipped)
                    val canvasStartAngle = -angleFromCenter
                    val canvasSweepAngle = -90f * partialArcAmount  // Partial sweep

                    // Draw the partial arc
                    arcTo(
                        rect = Rect(
                            left = canvasCenter.x - radius,
                            top = canvasCenter.y - radius,
                            right = canvasCenter.x + radius,
                            bottom = canvasCenter.y + radius
                        ),
                        startAngleDegrees = canvasStartAngle,
                        sweepAngleDegrees = canvasSweepAngle,
                        forceMoveTo = false
                    )
                }
            }
        }

        drawPath(
            path = spiralPath,
            color = SpiralColors.SPIRAL_COLOR,
            style = Stroke(width = 3f)
        )
        } // end withTransform
    }
}

/**
 * Data class to hold all rendering information
 */
private data class SpiralRenderData(
    val fibNumbers: List<Int>,
    val positions: List<Position>,
    val sizes: List<Float>,
    val arcs: List<ArcGeometry>
)
