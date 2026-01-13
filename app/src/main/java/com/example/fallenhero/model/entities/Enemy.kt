package com.example.fallenhero.model.entities

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import com.example.fallenhero.R
import java.util.Random

public class Enemy {

    var x = 0
    var y = 0
    var speed = 0
    var maxY = 0
    var minY = 0
    var maxX = 0
    var minX = 0

    var bitmap: Bitmap

    var generator = Random()

    var collisionBox : Rect

    constructor(context: Context, with: Int, height: Int) {

        val originalBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.mob1)
        val scaleFactor = 0.75f
        val newWidth = (originalBitmap.width * scaleFactor).toInt()
        val newHeight = (originalBitmap.height * scaleFactor).toInt()
        bitmap = Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, false)

        minX = 0
        maxX = with

        maxY = height - bitmap.height
        minY = 0

        speed = generator.nextInt(6) + 10

        x = maxX
        if (maxY > 0) {
            y = generator.nextInt(maxY)
        } else {
            y = 0
        }

        collisionBox = Rect(x, y, bitmap.width, bitmap.height)

    }

    fun update(playerSpeed : Int){
        x -= speed
        x -= playerSpeed

        if (x < -bitmap.width){
            x = maxX
            if (maxY > 0) {
                y = generator.nextInt(maxY)
            } else {
                y = 0
            }
            speed = generator.nextInt(6) + 10
        }

        collisionBox.left = x
        collisionBox.top = y
        collisionBox.right = x + bitmap.width
        collisionBox.bottom = y + bitmap.height
    }


}
