package com.stevegiralt.spirals.fibonacci

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GoldenRatioTest {

    private val EPSILON = 0.0001

    @Test
    fun testPhi_constant() {
        // Verify the Golden Ratio constant is correct
        assertEquals(1.618033988749895, GoldenRatio.PHI, EPSILON)
    }

    @Test
    fun testCalculateRatios_emptyList() {
        val ratios = GoldenRatio.calculateRatios(emptyList())
        assertEquals(0, ratios.size, "Empty list should return empty ratios")
    }

    @Test
    fun testCalculateRatios_singleElement() {
        val ratios = GoldenRatio.calculateRatios(listOf(1))
        assertEquals(0, ratios.size, "Single element should return empty ratios")
    }

    @Test
    fun testCalculateRatios_twoElements() {
        val ratios = GoldenRatio.calculateRatios(listOf(1, 1))
        assertEquals(1, ratios.size, "Two elements should return one ratio")
        assertEquals(1.0, ratios[0], EPSILON, "1/1 should equal 1.0")
    }

    @Test
    fun testCalculateRatios_basicSequence() {
        val fibNumbers = listOf(1, 1, 2, 3, 5)
        val ratios = GoldenRatio.calculateRatios(fibNumbers)

        assertEquals(4, ratios.size, "Should have 4 ratios for 5 numbers")

        // Verify each ratio
        assertEquals(1.0, ratios[0], EPSILON, "1/1 = 1.0")
        assertEquals(2.0, ratios[1], EPSILON, "2/1 = 2.0")
        assertEquals(1.5, ratios[2], EPSILON, "3/2 = 1.5")
        assertEquals(1.666666, ratios[3], EPSILON, "5/3 ≈ 1.667")
    }

    @Test
    fun testCalculateRatios_convergence() {
        // As we go further in the sequence, ratios should converge to phi
        val fibNumbers = listOf(1, 1, 2, 3, 5, 8, 13, 21, 34, 55, 89, 144)
        val ratios = GoldenRatio.calculateRatios(fibNumbers)

        // First ratio should be far from phi
        assertTrue(
            kotlin.math.abs(ratios[0] - GoldenRatio.PHI) > 0.5,
            "First ratio should be far from phi"
        )

        // Last ratio should be very close to phi
        assertTrue(
            kotlin.math.abs(ratios.last() - GoldenRatio.PHI) < 0.01,
            "Last ratio should be close to phi (got ${ratios.last()})"
        )

        // Ratios should generally get closer to phi (allowing for some oscillation)
        val firstHalfAvg = ratios.take(ratios.size / 2)
            .map { kotlin.math.abs(it - GoldenRatio.PHI) }
            .average()
        val secondHalfAvg = ratios.drop(ratios.size / 2)
            .map { kotlin.math.abs(it - GoldenRatio.PHI) }
            .average()

        assertTrue(
            secondHalfAvg < firstHalfAvg,
            "Later ratios should be closer to phi on average"
        )
    }

    @Test
    fun testCalculateConvergence() {
        val ratios = listOf(1.0, 2.0, 1.5, 1.666666, 1.6)
        val convergences = GoldenRatio.calculateConvergence(ratios)

        assertEquals(5, convergences.size, "Should have same number of convergences")

        // All convergences should be positive (absolute values)
        convergences.forEach { conv ->
            assertTrue(conv >= 0.0, "Convergence should be non-negative")
        }

        // Convergence should decrease as ratio gets closer to phi
        // 1.6 is closer to phi (1.618) than 1.0
        assertTrue(
            convergences[4] < convergences[0],
            "Convergence for 1.6 should be less than for 1.0"
        )
    }

    @Test
    fun testGetRatioInfo_validIndex() {
        val fibNumbers = listOf(1, 1, 2, 3, 5)
        val info = GoldenRatio.getRatioInfo(fibNumbers, 2)

        assertNotNull(info, "Should return RatioInfo for valid index")
        assertEquals(3, info.numerator, "Numerator should be fib[3]")
        assertEquals(2, info.denominator, "Denominator should be fib[2]")
        assertEquals(1.5, info.ratio, EPSILON, "Ratio should be 3/2 = 1.5")
        assertTrue(info.convergence >= 0.0, "Convergence should be non-negative")
    }

    @Test
    fun testGetRatioInfo_firstRatio() {
        val fibNumbers = listOf(1, 1, 2, 3, 5)
        val info = GoldenRatio.getRatioInfo(fibNumbers, 0)

        assertNotNull(info)
        assertEquals(1, info.numerator)
        assertEquals(1, info.denominator)
        assertEquals(1.0, info.ratio, EPSILON)
    }

    @Test
    fun testGetRatioInfo_lastRatio() {
        val fibNumbers = listOf(1, 1, 2, 3, 5)
        val info = GoldenRatio.getRatioInfo(fibNumbers, 3)

        assertNotNull(info)
        assertEquals(5, info.numerator)
        assertEquals(3, info.denominator)
        assertEquals(1.666666, info.ratio, EPSILON)
    }

    @Test
    fun testGetRatioInfo_invalidIndex() {
        val fibNumbers = listOf(1, 1, 2, 3, 5)

        assertNull(
            GoldenRatio.getRatioInfo(fibNumbers, -1),
            "Negative index should return null"
        )
        assertNull(
            GoldenRatio.getRatioInfo(fibNumbers, 4),
            "Index >= size-1 should return null"
        )
        assertNull(
            GoldenRatio.getRatioInfo(fibNumbers, 100),
            "Out of bounds index should return null"
        )
    }

    @Test
    fun testFindClosestRatio_emptyList() {
        val index = GoldenRatio.findClosestRatio(emptyList())
        assertEquals(-1, index, "Empty list should return -1")
    }

    @Test
    fun testFindClosestRatio_singleElement() {
        val index = GoldenRatio.findClosestRatio(listOf(1))
        assertEquals(-1, index, "Single element should return -1")
    }

    @Test
    fun testFindClosestRatio_smallSequence() {
        val fibNumbers = listOf(1, 1, 2, 3, 5)
        val index = GoldenRatio.findClosestRatio(fibNumbers)

        // Last ratio (5/3 ≈ 1.667) should be closest to phi
        assertEquals(3, index, "Last ratio should be closest for small sequence")
    }

    @Test
    fun testFindClosestRatio_largeSequence() {
        val fibNumbers = listOf(1, 1, 2, 3, 5, 8, 13, 21, 34, 55, 89, 144)
        val index = GoldenRatio.findClosestRatio(fibNumbers)

        // For larger sequences, the last ratio should be closest
        assertEquals(10, index, "Last ratio should be closest for large sequence")

        val info = GoldenRatio.getRatioInfo(fibNumbers, index)
        assertNotNull(info)
        assertTrue(
            info.convergence < 0.001,
            "Closest ratio should be very close to phi (convergence=${info.convergence})"
        )
    }

    @Test
    fun testRatioInfo_dataClass() {
        val info = RatioInfo(
            numerator = 5,
            denominator = 3,
            ratio = 1.666666,
            convergence = 0.048632
        )

        assertEquals(5, info.numerator)
        assertEquals(3, info.denominator)
        assertEquals(1.666666, info.ratio, EPSILON)
        assertEquals(0.048632, info.convergence, EPSILON)
    }
}
