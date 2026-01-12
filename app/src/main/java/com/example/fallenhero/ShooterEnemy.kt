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

    // Hitbox Adjustment
    private val hitboxInsetX = 40 // Shrinks the hitbox from the left and right
    private val hitboxInsetTop = 50 // Shrinks the hitbox from the top

    // Shooting mechanics
    private var shootCooldown = 0
    private val shootInterval = 120 // Approx. 2 seconds (120 frames at 60fps)
    private val minShootingDistance = 500 // Don't shoot if the player is closer than this on the X-axis
    private val random = Random()

    init {
        val originalBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.mob2)
        val scaleFactor = 1f
        width = (originalBitmap.width * scaleFactor).toInt()
        height = (originalBitmap.height * scaleFactor).toInt()
        bitmap = Bitmap.createScaledBitmap(originalBitmap, width, height, false)

        x = screenWidth
        y = screenHeight - height + 50
        speed = random.nextInt(10) + 10

        // Initialize the collision box with the insets
        collisionBox = Rect(
            x + hitboxInsetX,
            y + hitboxInsetTop,
            x + width - hitboxInsetX,
            y + height
        )

        shootCooldown = random.nextInt(shootInterval)
    }

    fun update(speedBoost: Int) {
        x -= speed + speedBoost

        if (x < -width) {
            reset()
        }

        // Update collision box with the insets
        collisionBox.left = x + hitboxInsetX
        collisionBox.top = y + hitboxInsetTop
        collisionBox.right = x + width - hitboxInsetX
        collisionBox.bottom = y + height

        if (shootCooldown > 0) {
            shootCooldown--
        }
    }

    fun canShoot(player: Player): Boolean {
        val distanceToPlayer = this.x - player.x
        // The enemy can shoot as long as the player is further than the minimum distance
        return shootCooldown <= 0 && distanceToPlayer > minShootingDistance
    }

    /**
     * This function simply resets the cooldown timer after a shot has been fired.
     */
    fun onShotFired() {
        shootCooldown = shootInterval
    }

    private fun reset() {
        x = screenWidth + random.nextInt(200)
        y = screenHeight - height + 50
        speed = random.nextInt(10) + 10
    }
}
