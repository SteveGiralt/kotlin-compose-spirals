package com.stevegiralt.spirals.fibonacci

import kotlin.math.abs

/**
 * Golden Ratio calculations and convergence analysis.
 *
 * The Golden Ratio (φ, phi) ≈ 1.618033988749895 is the limit of the ratio
 * of consecutive Fibonacci numbers as they approach infinity.
 */
object GoldenRatio {

    /** The mathematical Golden Ratio constant */
    const val PHI = 1.618033988749895

    /**
     * Calculates the ratio of consecutive Fibonacci numbers.
     *
     * @param fibNumbers List of Fibonacci numbers
     * @return List of ratios where ratio[i] = fib[i+1] / fib[i]
     */
    fun calculateRatios(fibNumbers: List<Int>): List<Double> {
        if (fibNumbers.size < 2) return emptyList()

        return fibNumbers.zipWithNext { a, b ->
            b.toDouble() / a.toDouble()
        }
    }

    /**
     * Calculates how close each ratio is to the Golden Ratio.
     *
     * @param ratios List of ratios from consecutive Fibonacci numbers
     * @return List of convergence values (smaller = closer to phi)
     */
    fun calculateConvergence(ratios: List<Double>): List<Double> {
        return ratios.map { ratio ->
            abs(ratio - PHI)
        }
    }

    /**
     * Gets the ratio at a specific index with metadata.
     *
     * @param fibNumbers List of Fibonacci numbers
     * @param index Index of the ratio to calculate
     * @return RatioInfo containing the ratio and convergence data
     */
    fun getRatioInfo(fibNumbers: List<Int>, index: Int): RatioInfo? {
        if (index < 0 || index >= fibNumbers.size - 1) return null

        val numerator = fibNumbers[index + 1]
        val denominator = fibNumbers[index]
        val ratio = numerator.toDouble() / denominator.toDouble()
        val convergence = abs(ratio - PHI)

        return RatioInfo(
            numerator = numerator,
            denominator = denominator,
            ratio = ratio,
            convergence = convergence
        )
    }

    /**
     * Finds the index of the ratio closest to the Golden Ratio.
     *
     * @param fibNumbers List of Fibonacci numbers
     * @return Index of the ratio closest to phi (or -1 if list is too short)
     */
    fun findClosestRatio(fibNumbers: List<Int>): Int {
        val ratios = calculateRatios(fibNumbers)
        if (ratios.isEmpty()) return -1

        val convergences = calculateConvergence(ratios)
        return convergences.indices.minByOrNull { convergences[it] } ?: -1
    }
}

/**
 * Information about a specific Fibonacci ratio.
 *
 * @property numerator The larger Fibonacci number (F[n+1])
 * @property denominator The smaller Fibonacci number (F[n])
 * @property ratio The calculated ratio (numerator / denominator)
 * @property convergence How far from the Golden Ratio (|ratio - phi|)
 */
data class RatioInfo(
    val numerator: Int,
    val denominator: Int,
    val ratio: Double,
    val convergence: Double
)
