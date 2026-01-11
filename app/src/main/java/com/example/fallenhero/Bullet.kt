package com.example.fallenhero

import kotlin.math.cos
import kotlin.math.sin

class Bullet {

    var active = false

    var x = 0f
    var y = 0f

    private var vx = 0f
    private var vy = 0f

    private val speed = 25f

    fun fire(startX: Int, startY: Int, angle: Float) {
        x = startX.toFloat()
        y = startY.toFloat()

        vx = cos(Math.toRadians(angle.toDouble())).toFloat() * speed
        vy = sin(Math.toRadians(angle.toDouble())).toFloat() * speed

        active = true
    }

    fun update() {
        if (!active) return

        x += vx
        y += vy
    }

    fun deactivate() {
        active = false
    }

    fun isOutOfScreen(screenWidth: Int, screenHeight: Int): Boolean {
        return x < 0 || x > screenWidth || y < 0 || y > screenHeight
    }
}
