package com.stevegiralt.spirals.fibonacci

/**
 * Pure Fibonacci sequence generator.
 *
 * Generates the Fibonacci sequence starting with [1, 1] and each subsequent
 * number being the sum of the two preceding ones.
 */
object FibonacciGenerator {

    /**
     * Generates the first n Fibonacci numbers.
     *
     * @param n The number of Fibonacci numbers to generate (must be > 0)
     * @return List of n Fibonacci numbers starting with [1, 1, 2, 3, 5, 8, ...]
     * @throws IllegalArgumentException if n <= 0
     */
    fun generateSequence(n: Int): List<Int> {
        require(n > 0) { "n must be greater than 0, got: $n" }

        if (n == 1) return listOf(1)
        if (n == 2) return listOf(1, 1)

        val fib = mutableListOf(1, 1)
        for (i in 2 until n) {
            fib.add(fib[i - 1] + fib[i - 2])
        }

        return fib
    }
}
