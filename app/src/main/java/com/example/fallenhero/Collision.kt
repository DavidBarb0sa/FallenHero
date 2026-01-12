package com.example.fallenhero

import android.graphics.Rect
import kotlin.math.cos
import kotlin.math.sin

object Collision {
    private const val PLAYER_HITBOX_ANGLE_DEGREES = 15f

    // --- Optimization: Pre-calculate constant values ---
    private val angleRad = -Math.toRadians(PLAYER_HITBOX_ANGLE_DEGREES.toDouble())
    private val cosAngle = cos(angleRad).toFloat()
    private val sinAngle = sin(angleRad).toFloat()
    // -----------------------------------------------------

    fun checkPlayerCollision(player: Player, other: Rect): Boolean {
        // --- Optimization: Broad-Phase Check ---
        // First, do a cheap check to see if the simple bounding boxes are even close.
        // If not, we can exit immediately without doing expensive math.
        if (!Rect.intersects(player.collisionBox, other)) {
            return false
        }
        // ----------------------------------------

        // --- Narrow-Phase Check (The expensive math) ---
        // This part now only runs if the objects are close to each other.

        val ellipseCenterX = player.x + player.width / 2f
        val ellipseCenterY = player.y + player.height / 2f
        val ellipseRadiusX = player.width / 2f
        val ellipseRadiusY = player.height / 2f

        val translatedRectCenterX = other.exactCenterX() - ellipseCenterX
        val translatedRectCenterY = other.exactCenterY() - ellipseCenterY

        val rotatedRectCenterX = translatedRectCenterX * cosAngle - translatedRectCenterY * sinAngle
        val rotatedRectCenterY = translatedRectCenterX * sinAngle + translatedRectCenterY * cosAngle

        val rectHalfWidth = other.width() / 2f
        val rectHalfHeight = other.height() / 2f

        val closestX = rotatedRectCenterX.coerceIn(-rectHalfWidth, rectHalfWidth)
        val closestY = rotatedRectCenterY.coerceIn(-rectHalfHeight, rectHalfHeight)

        val distanceX = rotatedRectCenterX - closestX
        val distanceY = rotatedRectCenterY - closestY

        // Optimization: Using x*x is faster than x.pow(2)
        val ellipseEquationResult = (distanceX * distanceX) / (ellipseRadiusX * ellipseRadiusX) + (distanceY * distanceY) / (ellipseRadiusY * ellipseRadiusY)

        return ellipseEquationResult <= 1
    }
}