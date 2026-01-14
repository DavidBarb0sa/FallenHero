package com.example.fallenhero.model.entities

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import com.example.fallenhero.R
import java.util.Random

class Orb(context: Context, private val screenWidth: Int, private val screenHeight: Int) {
    var bitmap: Bitmap
    var x: Int = 0
    var y: Int = 0
    var speed = 15

    val width: Int
    val height: Int

    var collisionBox: Rect
    private val random = Random()

    init {
        val originalBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.orb)
        val scaleFactor = 0.3f
        width = (originalBitmap.width * scaleFactor).toInt()
        height = (originalBitmap.height * scaleFactor).toInt()
        bitmap = Bitmap.createScaledBitmap(originalBitmap, width, height, false)

        reset()
        collisionBox = Rect(x, y, x + width, y + height)
    }

    fun update() {
        x -= speed
        if (x < -width) {
            // If orb goes off-screen without being collected, reset it to appear again later
            reset()
        }

        // Update collision box
        collisionBox.left = x
        collisionBox.top = y
        collisionBox.right = x + width
        collisionBox.bottom = y + height
    }

    fun reset() {
        // Spawn off-screen to the right at a random height, with a random delay
        x = screenWidth + random.nextInt(5000) + 1000
        y = random.nextInt(screenHeight - height)
    }
}
