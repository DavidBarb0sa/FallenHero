package com.example.fallenhero.model.entities

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import com.example.fallenhero.R

class Shield(context: Context) {

    private val bitmap: Bitmap
    var isActive = false

    init {
        val originalBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.shield)
        // Scale the bitmap to be smaller for the player effect
        val scaleFactor = 0.5f
        val newWidth = (originalBitmap.width * scaleFactor).toInt()
        val newHeight = (originalBitmap.height * scaleFactor).toInt()
        bitmap = Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, false)
    }

    /**
     * Draws the shield centered over the player if it's active.
     */
    fun draw(canvas: Canvas, player: Player, paint: Paint) {
        if (isActive) {
            // Calculate the position to center the shield bitmap over the player's bitmap
            val drawX = player.x - (bitmap.width - player.width) / 2f
            val drawY = player.y - (bitmap.height - player.height) / 2f
            canvas.drawBitmap(bitmap, drawX, drawY, paint)
        }
    }
}
