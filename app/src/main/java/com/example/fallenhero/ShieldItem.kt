package com.example.fallenhero

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import java.util.Random

class ShieldItem(context: Context, private val screenWidth: Int, private val screenHeight: Int) {

    val bitmap: Bitmap
    var x: Int
    var y: Int
    private var speed: Int

    val width: Int
    val height: Int
    var collisionBox: Rect
    private val random = Random()

    init {
        // Load the shield sprite and scale it down to be a collectible item
        val originalBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.shield)
        val scaleFactor = 0.5f // Make it smaller than the player effect
        width = (originalBitmap.width * scaleFactor).toInt()
        height = (originalBitmap.height * scaleFactor).toInt()
        bitmap = Bitmap.createScaledBitmap(originalBitmap, width, height, false)

        speed = 15
        // Start off-screen
        x = screenWidth + random.nextInt(10000) // Increased initial random range
        y = random.nextInt(screenHeight - height)

        collisionBox = Rect(x, y, x + width, y + height)
    }

    fun update() {
        // Move from right to left
        x -= speed

        // If it goes off-screen to the left, reset its position to the right
        if (x + width < 0) {
            reset()
        }

        // Update collision box
        collisionBox.left = x
        collisionBox.top = y
        collisionBox.right = x + width
        collisionBox.bottom = y + height
    }

    fun reset() {
        // Move it far off-screen to the right to wait for its next appearance
        // Increased the range significantly to make it appear less often.
        x = screenWidth + random.nextInt(10000) + 5000 
        y = random.nextInt(screenHeight - height)
    }
}
