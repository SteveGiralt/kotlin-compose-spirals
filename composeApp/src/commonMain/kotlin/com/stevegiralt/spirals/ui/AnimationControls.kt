package com.stevegiralt.spirals.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/** Format a Float to 1 decimal place (multiplatform-compatible) */
private fun formatOneDecimal(value: Float): String {
    val rounded = (value * 10).roundToInt()
    return "${rounded / 10}.${rounded % 10}"
}

/**
 * Animation control buttons for the Fibonacci spiral visualization.
 * Provides play/pause, reset, and speed control.
 */
@Composable
fun AnimationControls(
    isAnimating: Boolean,
    animationProgress: Float,
    animationSpeed: Float,
    onPlayPause: () -> Unit,
    onReset: () -> Unit,
    onSpeedChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Top row: Buttons and Progress
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Play/Pause Button (disabled when animation is complete)
                val isComplete = animationProgress >= 1f
                FilledTonalIconButton(
                    onClick = onPlayPause,
                    modifier = Modifier.size(48.dp),
                    enabled = !isComplete
                ) {
                    Icon(
                        imageVector = if (isAnimating) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isAnimating) "Pause" else "Play",
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Reset Button
                FilledTonalIconButton(
                    onClick = onReset,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Replay,
                        contentDescription = "Reset",
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Progress indicator
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Progress: ${(animationProgress * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    LinearProgressIndicator(
                        progress = { animationProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                    )
                }
            }

            // Speed control slider
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Animation Speed: ${formatOneDecimal(animationSpeed)}x",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Slider(
                    value = animationSpeed,
                    onValueChange = onSpeedChange,
                    valueRange = 0.5f..2f,
                    steps = 2,  // 0.5x, 1.0x, 1.5x, 2.0x
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "0.5x",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "2.0x",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
