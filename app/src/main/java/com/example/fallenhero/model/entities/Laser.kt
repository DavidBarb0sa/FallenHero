package com.example.fallenhero.model.entities

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect

class Laser(private val screenHeight: Int) {

    var collisionRect: Rect? = null
    var isActive = false
        private set

    private val laserWidth = 15f

    // Energy mechanics
    private val maxEnergy = 100f
    var currentEnergy = maxEnergy
        private set
    private val depletionRate = 0.5f
    private val rechargeRate = 0.2f

    // Recharge cooldown mechanics
    private val rechargeCooldownDuration = 15 // Recharges after 15 frames
    private var rechargeCooldownTimer = 0

    fun update(isBoosting: Boolean, player: Player) {
        if (isBoosting) {
            // Player is touching the screen.
            // Reset the recharge cooldown timer every time the player boosts.
            rechargeCooldownTimer = rechargeCooldownDuration

            if (currentEnergy > 0) {
                // If there's energy, activate the laser and consume energy.
                isActive = true
                currentEnergy -= depletionRate
                if (currentEnergy < 0) currentEnergy = 0f

                val laserLeft = player.x + (player.width / 2f) - (laserWidth / 2f)
                val laserTop = player.y + player.height.toFloat()
                val laserRight = laserLeft + laserWidth
                val laserBottom = screenHeight.toFloat()
                collisionRect = Rect(laserLeft.toInt(), laserTop.toInt(), laserRight.toInt(), laserBottom.toInt())
            } else {
                // If there's no energy, the laser is inactive.
                isActive = false
                collisionRect = null
            }
        } else {
            // Player is not touching the screen.
            isActive = false
            collisionRect = null

            // Handle cooldown first.
            if (rechargeCooldownTimer > 0) {
                rechargeCooldownTimer--
            } else {
                // Once cooldown is over, recharge the laser.
                if (currentEnergy < maxEnergy) {
                    currentEnergy += rechargeRate
                    if (currentEnergy > maxEnergy) currentEnergy = maxEnergy
                }
            }
        }
    }

    fun draw(canvas: Canvas, paint: Paint) {
        if (isActive && collisionRect != null) {
            canvas.drawRect(collisionRect!!, paint)
        }
    }

    fun getEnergyPercentage(): Float {
        return currentEnergy / maxEnergy
    }
}
