// SoundManager.kt
package com.example.fallenhero

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool

object SoundManager {
    private lateinit var soundPool: SoundPool
    var soundIdExplosion = 0
    var soundIdGameOver = 0
    var soundIdGameOverMusic = 0
    var soundIdLaser = 0
    var soundIdLaserPowerUp = 0
    var soundIdShield = 0
    var soundIdHurt = 0 // New ID for the hurt sound

    private var streamIdLaserPowerUp = 0 // To control the looping sound

    fun init(context: Context) {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(7) // Increased streams for more sounds
            .setAudioAttributes(audioAttributes)
            .build()

        soundIdExplosion = soundPool.load(context, R.raw.shoot, 1)
        soundIdGameOver = soundPool.load(context, R.raw.lego, 1)
        soundIdGameOverMusic = soundPool.load(context, R.raw.gameover, 1)
        soundIdLaser = soundPool.load(context, R.raw.laser, 1)
        soundIdLaserPowerUp = soundPool.load(context, R.raw.laserpowerup, 1)
        soundIdShield = soundPool.load(context, R.raw.shield, 1)
        soundIdHurt = soundPool.load(context, R.raw.hurt, 1) // Load the new sound
    }

    fun playExplosion() {
        soundPool.play(soundIdExplosion, 1f, 1f, 0, 0, 1f)
    }

    fun playLaser() {
        soundPool.play(soundIdLaser, 0.5f, 0.5f, 0, 0, 1f)
    }

    fun playLaserPowerUp() {
        if (streamIdLaserPowerUp == 0) { // Prevent multiple loops
            streamIdLaserPowerUp = soundPool.play(soundIdLaserPowerUp, 0.7f, 0.7f, 1, -1, 1f) // loop = -1
        }
    }

    fun stopLaserPowerUp() {
        if (streamIdLaserPowerUp != 0) {
            soundPool.stop(streamIdLaserPowerUp)
            streamIdLaserPowerUp = 0
        }
    }

    fun playShield() {
        soundPool.play(soundIdShield, 0.8f, 0.8f, 0, 0, 1f)
    }

    // New function to play the hurt sound
    fun playHurt() {
        soundPool.play(soundIdHurt, 1f, 1f, 0, 0, 1f)
    }

    fun playGameOver() {
        soundPool.play(soundIdGameOver, 1f, 1f, 1, 0, 1f)
        soundPool.play(soundIdGameOverMusic, 1f, 1f, 1, 0, 1f)
    }

    fun stopAll() {
        soundPool.autoPause()
    }

    fun release() {
        soundPool.release()
    }
}
