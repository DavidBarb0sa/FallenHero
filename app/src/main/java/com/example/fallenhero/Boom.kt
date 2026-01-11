package com.example.fallenhero;

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory

public class Boom {

    var x = 0
    var y = 0
    var maxY = 0
    var minY = 0
    var maxX = 0
    var minX = 0
    var bitmap: Bitmap
    var isOnScreen  = false
    var framesCount = 0

    constructor(context: Context, with: Int, height: Int) {

        bitmap = BitmapFactory
            .decodeResource(
                context.resources,
                R.drawable.boom)

        minX = 0
        maxX = with

        maxY = height - bitmap.height
        minY = 0

        x = -300
        y = -300

    }

    fun update(){
        if (isOnScreen) {
            framesCount += 1
            if (framesCount > 4) {
                isOnScreen = false
                framesCount = 0
            }
        }else{
            x = -300
            y = -300
        }
    }



}
