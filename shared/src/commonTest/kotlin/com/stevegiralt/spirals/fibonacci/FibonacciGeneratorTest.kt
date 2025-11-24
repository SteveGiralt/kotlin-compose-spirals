package com.stevegiralt.spirals.fibonacci

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class FibonacciGeneratorTest {

    @Test
    fun testGenerateSequence_n1() {
        val result = FibonacciGenerator.generateSequence(1)
        assertEquals(listOf(1), result, "n=1 should return [1]")
    }

    @Test
    fun testGenerateSequence_n2() {
        val result = FibonacciGenerator.generateSequence(2)
        assertEquals(listOf(1, 1), result, "n=2 should return [1, 1]")
    }

    @Test
    fun testGenerateSequence_n3() {
        val result = FibonacciGenerator.generateSequence(3)
        assertEquals(listOf(1, 1, 2), result, "n=3 should return [1, 1, 2]")
    }

    @Test
    fun testGenerateSequence_n5() {
        val result = FibonacciGenerator.generateSequence(5)
        assertEquals(
            listOf(1, 1, 2, 3, 5),
            result,
            "n=5 should return [1, 1, 2, 3, 5]"
        )
    }

    @Test
    fun testGenerateSequence_n8() {
        val result = FibonacciGenerator.generateSequence(8)
        assertEquals(
            listOf(1, 1, 2, 3, 5, 8, 13, 21),
            result,
            "n=8 should return [1, 1, 2, 3, 5, 8, 13, 21]"
        )
    }

    @Test
    fun testGenerateSequence_n10() {
        val result = FibonacciGenerator.generateSequence(10)
        assertEquals(
            listOf(1, 1, 2, 3, 5, 8, 13, 21, 34, 55),
            result,
            "n=10 should return correct sequence"
        )
    }

    @Test
    fun testGenerateSequence_n13() {
        val result = FibonacciGenerator.generateSequence(13)
        assertEquals(13, result.size, "n=13 should return 13 numbers")
        assertEquals(1, result[0], "First number should be 1")
        assertEquals(1, result[1], "Second number should be 1")
        assertEquals(233, result[12], "13th Fibonacci number should be 233")
    }

    @Test
    fun testGenerateSequence_invalidInput() {
        assertFailsWith<IllegalArgumentException>(
            message = "n=0 should throw IllegalArgumentException"
        ) {
            FibonacciGenerator.generateSequence(0)
        }

        assertFailsWith<IllegalArgumentException>(
            message = "n=-1 should throw IllegalArgumentException"
        ) {
            FibonacciGenerator.generateSequence(-1)
        }
    }

    @Test
    fun testGenerateSequence_fibonacciProperty() {
        // Verify the Fibonacci property: F(n) = F(n-1) + F(n-2)
        val result = FibonacciGenerator.generateSequence(10)

        for (i in 2 until result.size) {
            assertEquals(
                result[i],
                result[i - 1] + result[i - 2],
                "Fibonacci property should hold at index $i"
            )
        }
    }
}
