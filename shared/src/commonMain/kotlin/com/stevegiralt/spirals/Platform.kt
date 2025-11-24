package com.stevegiralt.spirals

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform