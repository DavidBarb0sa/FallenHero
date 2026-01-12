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

    fun init(context: Context) {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()

        soundIdExplosion = soundPool.load(context, R.raw.shoot, 1)
        soundIdGameOver = soundPool.load(context, R.raw.lego, 1)
        soundIdGameOverMusic = soundPool.load(context, R.raw.gameover, 1)
    }

    fun playExplosion() {
        soundPool.play(soundIdExplosion, 1f, 1f, 0, 0, 1f) // Corrected to max volume
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
