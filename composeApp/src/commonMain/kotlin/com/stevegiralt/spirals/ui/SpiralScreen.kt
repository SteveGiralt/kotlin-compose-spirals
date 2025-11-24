package com.stevegiralt.spirals.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

/** Format a Float to 1 decimal place (multiplatform-compatible) */
private fun formatOneDecimal(value: Float): String {
    val rounded = (value * 10).roundToInt()
    return "${rounded / 10}.${rounded % 10}"
}

/**
 * Main screen for the Fibonacci Spiral Visualizer.
 * Combines input controls, canvas visualization, and information panel in a bottom sheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpiralScreen(
    modifier: Modifier = Modifier,
    viewModel: SpiralViewModel = rememberSpiralViewModel()
) {
    val uiState by remember { derivedStateOf { viewModel.uiState } }
    var showInfoSheet by remember { mutableStateOf(false) }
    var showConfigSheet by remember { mutableStateOf(false) }
    val infoSheetState = rememberModalBottomSheetState()
    val configSheetState = rememberModalBottomSheetState()

    // Animation logic
    LaunchedEffect(uiState.isAnimating, uiState.n, uiState.animationSpeed) {
        if (uiState.isAnimating) {
            // Calculate animation duration based on n and speed
            // Base: 500ms per square, adjusted by speed multiplier
            val baseDurationPerSquare = 500L
            val frameDelay = 16L  // ~60 fps

            while (isActive && uiState.isAnimating && uiState.animationProgress < 1f) {
                delay(frameDelay)

                // Calculate increment based on speed
                // progress per frame = (1 / total_duration) * frame_delay
                val totalDuration = (uiState.n * baseDurationPerSquare) / uiState.animationSpeed
                val increment = (frameDelay.toFloat() / totalDuration)

                val newProgress = (uiState.animationProgress + increment).coerceAtMost(1f)
                viewModel.setAnimationProgress(newProgress)
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Animation controls bar at top (compact version)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .safeDrawingPadding(),
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Play/Pause Button (disabled when animation is complete)
                    val isComplete = uiState.animationProgress >= 1f
                    FilledTonalIconButton(
                        onClick = {
                            if (uiState.isAnimating) {
                                viewModel.pauseAnimation()
                            } else {
                                viewModel.startAnimation()
                            }
                        },
                        modifier = Modifier.size(48.dp),
                        enabled = !isComplete
                    ) {
                        Icon(
                            imageVector = if (uiState.isAnimating) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (uiState.isAnimating) "Pause" else "Play",
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Reset Button
                    FilledTonalIconButton(
                        onClick = { viewModel.resetAnimation() },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Replay,
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
                        Text(
                            text = "Progress: ${(uiState.animationProgress * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        LinearProgressIndicator(
                            progress = { uiState.animationProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                        )
                    }
                }
            }

            // Canvas area with spiral visualization - gets remaining space
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                SpiralCanvas(
                    n = uiState.n,
                    animationProgress = uiState.animationProgress
                )
            }
        }

        // Floating action buttons (column on bottom-right)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Settings FAB
            FloatingActionButton(
                onClick = { showConfigSheet = true },
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Show configuration",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            // Info FAB
            FloatingActionButton(
                onClick = { showInfoSheet = true },
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Show Fibonacci info",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        // Bottom sheet with configuration panel
        if (showConfigSheet) {
            ModalBottomSheet(
                onDismissRequest = { showConfigSheet = false },
                sheetState = configSheetState
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .padding(bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Text(
                        text = "Configuration",
                        style = MaterialTheme.typography.headlineSmall
                    )

                    // Number of squares slider
                    InputControls(
                        value = uiState.n,
                        onValueChange = { viewModel.updateN(it) }
                    )

                    HorizontalDivider()

                    // Animation speed slider
                    Column {
                        Text(
                            text = "Animation Speed: ${formatOneDecimal(uiState.animationSpeed)}x",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Slider(
                            value = uiState.animationSpeed,
                            onValueChange = { viewModel.setAnimationSpeed(it) },
                            valueRange = 0.5f..2f,
                            steps = 2,
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

        // Bottom sheet with info panel
        if (showInfoSheet) {
            ModalBottomSheet(
                onDismissRequest = { showInfoSheet = false },
                sheetState = infoSheetState
            ) {
                InfoPanel(
                    uiState = uiState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 500.dp)
                )
            }
        }
    }
}

