package com.example.fallenhero.model.gameplay

import android.graphics.Rect
import com.example.fallenhero.model.entities.Player

object Collision {

    /**
     * Checks for collision between the player's rotated elliptical hitbox and a rectangular object.
     */
    fun checkPlayerEllipseCollision(player: Player, rect: Rect): Boolean {
        // Find the closest point on the rectangle to the center of the ellipse
        val closestX = clamp(player.centerX, rect.left.toFloat(), rect.right.toFloat())
        val closestY = clamp(player.centerY, rect.top.toFloat(), rect.bottom.toFloat())

        // --- Start: Rotation Logic ---

        // Translate the closest point so that the ellipse's center is the origin
        val translatedX = closestX - player.centerX
        val translatedY = closestY - player.centerY

        // Convert the player's rotation angle to radians
        val angleRad = Math.toRadians(player.rotationAngle.toDouble())
        val cosAngle = Math.cos(angleRad).toFloat()
        val sinAngle = Math.sin(angleRad).toFloat()

        // "Un-rotate" the translated point to align it with the ellipse's axes
        val unrotatedX = translatedX * cosAngle + translatedY * sinAngle
        val unrotatedY = -translatedX * sinAngle + translatedY * cosAngle

        // --- End: Rotation Logic ---

        // Use the standard ellipse equation on the un-rotated point
        // (x/a)^2 + (y/b)^2 <= 1
        val distanceSquared = (unrotatedX * unrotatedX) / (player.radiusX * player.radiusX) +
                              (unrotatedY * unrotatedY) / (player.radiusY * player.radiusY)

        return distanceSquared <= 1
    }

    /**
     * Helper function to clamp a value between a minimum and maximum.
     */
    private fun clamp(value: Float, min: Float, max: Float): Float {
        return Math.max(min, Math.min(max, value))
    }
}
