package com.example.fallenhero

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
            // You might want to play a sound effect here
            // SoundManager.playPowerUpSound()
        }
    }

    fun update(player: Player) {
        if (isActive) {
            // Countdown the duration
            currentDuration--

            if (currentDuration <= 0) {
                // Deactivate when time runs out
                isActive = false
                collisionRect = null
            } else {
                // This offset will move the laser up. Adjust the value for perfect alignment.
                val yOffset = 55f

                // Update the laser's position to follow the player, including the offset
                val laserTop = player.y + (player.height / 2f) - (laserHeight / 2f) - yOffset
                val laserBottom = laserTop + laserHeight
                
                // The laser starts from the player and goes to the edge of the screen
                collisionRect = Rect(player.x + player.width, laserTop.toInt(), screenWidth, laserBottom.toInt())
            }
        } else {
            collisionRect = null
        }
    }

    fun draw(canvas: Canvas) {
        if (isActive && collisionRect != null) {
            canvas.drawRect(collisionRect!!, paint)
        }
    }
}
