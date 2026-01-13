package com.example.fallenhero.model.entities

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.fallenhero.R

class Player(context: Context, screenWidth: Int, screenHeight: Int) {

    var x = 0
    var y = 0
    private var maxY = 0
    private var minY = 0

    var isBoosting = false
    var health = 3
    var hasShield = false
    var bitmap: Bitmap

    // Bitmaps for different states
    private val playerBitmap: Bitmap
    private val playerBoostingBitmap: Bitmap

    // Physics variables for Jetpack Joyride style movement
    private var velocityY = 0f
    private val gravity = 3f  // Gravity applied to the player
    private val lift = -7f    // Upward force when boosting

    val width : Int
    val height : Int

    // Elliptical collision properties
    var centerX: Float = 0f
    var centerY: Float = 0f
    val radiusX: Float
    val radiusY: Float
    val rotationAngle: Float = 15.0f // The angle to tilt the ellipse, in degrees

    init {
        // Load and scale the default player sprite
        val originalBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.player)
        val scaleFactor = 0.5f
        val newWidth = (originalBitmap.width * scaleFactor).toInt()
        val newHeight = (originalBitmap.height * scaleFactor).toInt()
        playerBitmap = Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, false)

        // Load and scale the boosting player sprite
        val originalBoostingBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.player2)
        playerBoostingBitmap = Bitmap.createScaledBitmap(originalBoostingBitmap, newWidth, newHeight, false)

        // Set the initial bitmap
        bitmap = playerBitmap

        width = bitmap.width
        height = bitmap.height

        // Initialize ellipse properties
        radiusX = width / 2.0f
        radiusY = height / 2.0f

        maxY = screenHeight - bitmap.height
        minY = 0

        x = 75
        y = 50

        // Set initial center
        centerX = x + radiusX
        centerY = y + radiusY
    }

    fun update(isPowerUpActive: Boolean){
        // Switch bitmap based on boosting state or if power up is active
        bitmap = if (isPowerUpActive || isBoosting) {
            playerBoostingBitmap
        } else {
            playerBitmap
        }

        // Apply gravity every frame
        velocityY += gravity

        // Apply lift if the player is boosting
        if (isBoosting) {
            velocityY += lift
        }

        // Update player's vertical position based on velocity
        y += velocityY.toInt()

        // Keep the player within the screen bounds
        if (y < minY) {
            y = minY
            velocityY = 0f // Stop upward velocity at the ceiling
        }
        if (y > maxY) {
            y = maxY
            velocityY = 0f // Stop downward velocity at the floor
        }

        // Update the ellipse center for the new position
        centerX = x + radiusX
        centerY = y + radiusY
    }
}
