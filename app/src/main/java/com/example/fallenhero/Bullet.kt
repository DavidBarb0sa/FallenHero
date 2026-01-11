package com.example.fallenhero

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import kotlin.math.sqrt

class Bullet(startX: Float, startY: Float, targetX: Float, targetY: Float) {
    var x: Float = startX
    var y: Float = startY
    private var velocityX: Float
    private var velocityY: Float
    val width = 25
    val height = 10

    var collisionBox: Rect
    private val paint = Paint().apply { color = Color.YELLOW }


    init {
        val dx = targetX - startX
        val dy = targetY - startY
        val distance = sqrt(dx * dx + dy * dy)
        val speed = 20f

        velocityX = (dx / distance) * speed
        velocityY = (dy / distance) * speed

        collisionBox = Rect(x.toInt(), y.toInt(), (x + width).toInt(), (y + height).toInt())
    }

    fun update() {
        x += velocityX
        y += velocityY

        collisionBox.left = x.toInt()
        collisionBox.top = y.toInt()
        collisionBox.right = (x + width).toInt()
        collisionBox.bottom = (y + height).toInt()
    }

    fun draw(canvas: Canvas) {
        canvas.drawRect(collisionBox, paint)
    }

    fun isOffScreen(screenWidth: Int, screenHeight: Int): Boolean {
        return x < -width || x > screenWidth || y < -height || y > screenHeight
    }
}
