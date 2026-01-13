package com.example.fallenhero.model.entities

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect

class HorizontalLaser(private val screenWidth: Int) {

    var collisionRect: Rect? = null
    var isActive = false
        private set

    private val laserHeight = 20f
    private val totalDuration = 180 // Approx 3 seconds (180 frames / 60fps)
    private var currentDuration = 0

    private val paint = Paint().apply {
        color = Color.CYAN
        alpha = 220
    }

    fun activate() {
        if (!isActive) {
            isActive = true
            currentDuration = totalDuration
        }
    }

    /**
     * Updates the laser's state.
     * @return true if the laser just deactivated, false otherwise.
     */
    fun update(player: Player): Boolean {
        if (isActive) {
            currentDuration--

            if (currentDuration <= 0) {
                isActive = false
                collisionRect = null
                return true // Signal that the laser just turned off
            } else {
                val yOffset = 55f
                val laserTop = player.y + (player.height / 2f) - (laserHeight / 2f) - yOffset
                val laserBottom = laserTop + laserHeight
                collisionRect = Rect(player.x + player.width, laserTop.toInt(), screenWidth, laserBottom.toInt())
            }
        } else {
            collisionRect = null
        }
        return false // Laser did not turn off on this frame
    }

    fun draw(canvas: Canvas) {
        if (isActive && collisionRect != null) {
            canvas.drawRect(collisionRect!!, paint)
        }
    }
}
