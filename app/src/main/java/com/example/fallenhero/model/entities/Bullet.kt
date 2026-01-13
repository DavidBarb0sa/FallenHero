package com.example.fallenhero.model.entities

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import kotlin.math.sqrt

class Bullet() {
    var x: Float = 0f
    var y: Float = 0f
    private var velocityX: Float = 0f
    private var velocityY: Float = 0f
    val width = 25
    val height = 10

    var collisionBox: Rect
    private val paint = Paint().apply { color = Color.YELLOW }

    var isActive = false

    init {
        collisionBox = Rect(0, 0, width, height)
    }

    fun update() {
        if (!isActive) return

        x += velocityX
        y += velocityY

        collisionBox.left = x.toInt()
        collisionBox.top = y.toInt()
        collisionBox.right = (x + width).toInt()
        collisionBox.bottom = (y + height).toInt()
    }

    fun draw(canvas: Canvas) {
        if (!isActive) return
        canvas.drawRect(collisionBox, paint)
    }

    /**
     * This method now takes the shooter and the target to calculate its own trajectory.
     * It no longer requires pre-calculated coordinates, eliminating the need for ShotData.
     */
    fun reset(shooter: ShooterEnemy, target: Player) {
        val startX = shooter.x.toFloat()
        val startY = shooter.y.toFloat() + shooter.height / 2f
        val targetX = target.x.toFloat() + target.width / 2f
        val targetY = target.y.toFloat() + target.height / 2f

        this.x = startX
        this.y = startY

        val dx = targetX - startX
        val dy = targetY - startY
        val distance = sqrt(dx * dx + dy * dy)
        val speed = 30f

        velocityX = (dx / distance) * speed
        velocityY = (dy / distance) * speed

        isActive = true
        update() // Call update once to set initial collision box position
    }

    fun isOffScreen(screenWidth: Int, screenHeight: Int): Boolean {
        return x < -width || x > screenWidth || y < -height || y > screenHeight
    }
}
