package com.example.fallenhero;

import android.content.Context
import java.util.Random


public class Star {

    var x = 0
    var y = 0
    var speed = 0
    var maxY = 0
    var minY = 0
    var maxX = 0
    var minX = 0

    var generator = Random()

    constructor(context: Context, with: Int, height: Int) {


        minX = 0
        maxX = with

        maxY = height
        minY = 0

        speed = generator.nextInt(12) + 4

        x = generator.nextInt(maxX)
        y = generator.nextInt(maxY)

    }

    fun update(playerSpeed: Int){
        x -= speed
        x -= playerSpeed
        if (x<0){
            x = maxX
            y = generator.nextInt(maxY)
            speed = generator.nextInt(15) + 1
        }
    }

    var starWidth : Int = 1
        get(){
            return generator.nextInt(6) + 4
        }

}
