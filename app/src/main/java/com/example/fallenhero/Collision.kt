package com.example.fallenhero

import android.graphics.Rect
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

object Collision {
    // O ângulo em graus que a hitbox do jogador vai ter.
    private const val PLAYER_HITBOX_ANGLE_DEGREES = 15f

    fun checkPlayerCollision(player: Player, other: Rect): Boolean {
        // Propriedades da elipse do jogador
        val ellipseCenterX = player.x + player.width / 2f
        val ellipseCenterY = player.y + player.height / 2f
        val ellipseRadiusX = player.width / 2f
        val ellipseRadiusY = player.height / 2f

        // Converte o ângulo para radianos para as funções matemáticas.
        // Usamos o ângulo negativo para "desfazer" a rotação no outro objeto.
        val angleRad = -Math.toRadians(PLAYER_HITBOX_ANGLE_DEGREES.toDouble())

        val cosAngle = cos(angleRad).toFloat()
        val sinAngle = sin(angleRad).toFloat()

        // 1. Transladar o centro do retângulo para o sistema de coordenadas da elipse
        // (para que o centro da elipse seja a nova origem).
        val translatedRectCenterX = other.exactCenterX() - ellipseCenterX
        val translatedRectCenterY = other.exactCenterY() - ellipseCenterY

        // 2. Rodar o centro do retângulo transladado em torno da nova origem.
        val rotatedRectCenterX = translatedRectCenterX * cosAngle - translatedRectCenterY * sinAngle
        val rotatedRectCenterY = translatedRectCenterX * sinAngle + translatedRectCenterY * cosAngle

        // 3. Encontrar o ponto mais próximo no retângulo (agora alinhado aos eixos)
        // em relação à origem (que é o centro da elipse).
        val rectHalfWidth = other.width() / 2f
        val rectHalfHeight = other.height() / 2f

        val closestX = rotatedRectCenterX.coerceIn(-rectHalfWidth, rectHalfWidth)
        val closestY = rotatedRectCenterY.coerceIn(-rectHalfHeight, rectHalfHeight)

        // 4. Encontrar a distância deste ponto mais próximo ao centro do retângulo rodado.
        val distanceX = rotatedRectCenterX - closestX
        val distanceY = rotatedRectCenterY - closestY

        // 5. Usar a equação da elipse para verificar se o ponto está dentro da elipse.
        // (x^2 / a^2) + (y^2 / b^2) <= 1
        val ellipseEquationResult = (distanceX.pow(2)) / (ellipseRadiusX.pow(2)) + (distanceY.pow(2)) / (ellipseRadiusY.pow(2))

        return ellipseEquationResult <= 1
    }
}
