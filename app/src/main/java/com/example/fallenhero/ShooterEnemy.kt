package com.example.fallenhero

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import java.util.Random

class ShooterEnemy(context: Context, private val screenWidth: Int, private val screenHeight: Int) {

    var bitmap: Bitmap
    var x: Int
    var y: Int
    var speed: Int

    val width: Int
    val height: Int
    var collisionBox: Rect

    // Shooting mechanics
    private var shootCooldown = 0
    private val shootInterval = 120 // Approx. 2 seconds (120 frames at 60fps)
    private val random = Random()

    init {
        val originalBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.mob2)
        val scaleFactor = 1f
        width = (originalBitmap.width * scaleFactor).toInt()
        height = (originalBitmap.height * scaleFactor).toInt()
        bitmap = Bitmap.createScaledBitmap(originalBitmap, width, height, false)

        x = screenWidth
        // Spawn on the ground
        y = screenHeight - height
        speed = random.nextInt(10) + 10

        collisionBox = Rect(x, y, x + width, y + height)
        // Start with a random cooldown so they don't all fire at once
        shootCooldown = random.nextInt(shootInterval)
    }

    fun update(speedBoost: Int) {
        x -= speed + speedBoost

        if (x < -width) {
            reset()
        }

        // Update collision box
        collisionBox.left = x
        collisionBox.top = y
        collisionBox.right = x + width
        collisionBox.bottom = y + height

        // Countdown the shoot timer
        if (shootCooldown > 0) {
            shootCooldown--
        }
    }

    fun canShoot(): Boolean {
        return shootCooldown <= 0
    }

    fun shoot(player: Player): Bullet {
        // Reset cooldown after shooting
        shootCooldown = shootInterval
        // Create a bullet starting from the enemy's center, aimed at the player's center
        val startX = x.toFloat()
        val startY = y.toFloat() + height / 2f
        val targetX = player.x.toFloat() + player.width / 2f
        val targetY = player.y.toFloat() + player.height / 2f
        return Bullet(startX, startY, targetX, targetY)
    }

    private fun reset() {
        x = screenWidth + random.nextInt(200)
        // Respawn on the ground
        y = screenHeight - height
        speed = random.nextInt(10) + 10
    }
}
