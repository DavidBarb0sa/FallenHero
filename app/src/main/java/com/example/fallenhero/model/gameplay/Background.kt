package com.example.fallenhero.model.gameplay

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Shader
import com.example.fallenhero.R

class Background(context: Context, private val screenWidth: Int, private val screenHeight: Int) {

    private val backgroundPaint: Paint
    private var xOffset = 0
    private val speed = 10

    init {
        val originalBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.bg)

        // Scale the bitmap to fit the screen height while maintaining aspect ratio
        val aspectRatio = originalBitmap.width.toFloat() / originalBitmap.height.toFloat()
        val scaledWidth = (screenHeight * aspectRatio).toInt()
        val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, scaledWidth, screenHeight, true)

        // Create a BitmapShader with the scaled bitmap, repeating it on the X-axis
        val shader = BitmapShader(scaledBitmap, Shader.TileMode.REPEAT, Shader.TileMode.CLAMP)

        // Initialize the Paint object with the shader
        backgroundPaint = Paint().apply {
            this.shader = shader
        }
    }

    fun update() {
        // Just update the offset. The shader handles the wrapping.
        xOffset -= speed
    }

    fun draw(canvas: Canvas) {
        // Save the current canvas state
        canvas.save()
        // Translate the canvas by the current offset to create the scroll effect
        canvas.translate(xOffset.toFloat(), 0f)
        // Draw a rectangle that covers the screen, filling it with the tiled background
        canvas.drawPaint(backgroundPaint)
        // Restore the canvas to its original state for other game objects
        canvas.restore()
    }
}
