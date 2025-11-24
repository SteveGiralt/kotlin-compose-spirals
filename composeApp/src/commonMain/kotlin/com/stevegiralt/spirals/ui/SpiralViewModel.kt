package com.stevegiralt.spirals.ui

import androidx.compose.runtime.*
import com.stevegiralt.spirals.fibonacci.FibonacciGenerator
import com.stevegiralt.spirals.fibonacci.GoldenRatio
import com.stevegiralt.spirals.fibonacci.RatioInfo

/**
 * UI state for the Fibonacci spiral visualization.
 */
data class SpiralUiState(
    val n: Int = 8,
    val fibonacciSequence: List<Int> = emptyList(),
    val ratios: List<RatioInfo> = emptyList(),
    val convergenceDistance: Double? = null,
    // Animation state
    val animationProgress: Float = 0f,  // 0.0 to 1.0 (start at 0%)
    val isAnimating: Boolean = false,
    val animationSpeed: Float = 1f  // 0.5x to 2.0x speed multiplier
)

/**
 * State holder for the spiral visualization.
 * Manages user input and calculated data for the UI.
 */
class SpiralViewModel {
    private var _uiState by mutableStateOf(SpiralUiState())
    val uiState: SpiralUiState
        get() = _uiState

    init {
        // Initialize with default value
        updateN(8)
    }

    /**
     * Updates the number of Fibonacci numbers to generate and recalculates all derived data.
     */
    fun updateN(newN: Int) {
        // Clamp to valid range
        val clampedN = newN.coerceIn(1, 15)

        // Generate Fibonacci sequence
        val fibSequence = FibonacciGenerator.generateSequence(clampedN)

        // Calculate golden ratio info for each consecutive pair
        val ratios = if (fibSequence.size >= 2) {
            (0 until fibSequence.size - 1).mapNotNull { index ->
                GoldenRatio.getRatioInfo(fibSequence, index)
            }
        } else {
            emptyList()
        }

        // Calculate convergence distance for the last ratio
        val convergenceDistance = ratios.lastOrNull()?.convergence

        _uiState = _uiState.copy(
            n = clampedN,
            fibonacciSequence = fibSequence,
            ratios = ratios,
            convergenceDistance = convergenceDistance,
            animationProgress = 0f  // Reset to 0% when n changes so user can animate
        )
    }

    /**
     * Starts the animation from current progress.
     */
    fun startAnimation() {
        _uiState = _uiState.copy(isAnimating = true)
    }

    /**
     * Pauses the animation at current progress.
     */
    fun pauseAnimation() {
        _uiState = _uiState.copy(isAnimating = false)
    }

    /**
     * Resets animation to the beginning and starts playing.
     */
    fun resetAnimation() {
        _uiState = _uiState.copy(
            animationProgress = 0f,
            isAnimating = true
        )
    }

    /**
     * Sets the animation speed multiplier (0.5x to 2.0x).
     */
    fun setAnimationSpeed(speed: Float) {
        val clampedSpeed = speed.coerceIn(0.5f, 2f)
        _uiState = _uiState.copy(animationSpeed = clampedSpeed)
    }

    /**
     * Updates the animation progress (used by animation coroutine).
     */
    fun setAnimationProgress(progress: Float) {
        val clampedProgress = progress.coerceIn(0f, 1f)
        _uiState = _uiState.copy(animationProgress = clampedProgress)

        // Auto-pause when animation completes
        if (clampedProgress >= 1f) {
            _uiState = _uiState.copy(isAnimating = false)
        }
    }
}

/**
 * Composable function to remember a SpiralViewModel instance.
 */
@Composable
fun rememberSpiralViewModel(): SpiralViewModel {
    return remember { SpiralViewModel() }
}
