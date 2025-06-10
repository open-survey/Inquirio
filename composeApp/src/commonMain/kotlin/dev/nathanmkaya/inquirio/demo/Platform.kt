package dev.nathanmkaya.inquirio.demo

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform