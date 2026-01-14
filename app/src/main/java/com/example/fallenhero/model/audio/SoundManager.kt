package com.example.fallenhero.model.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.util.Log
import com.example.fallenhero.R

object SoundManager {
    // --- SoundPool para efeitos sonoros curtos ---
    private lateinit var soundPool: SoundPool
    private var soundIdExplosion = 0
    private var soundIdLego = 0
    private var soundIdGameOverSound = 0
    private var soundIdLaser = 0
    private var soundIdLaserPowerUp = 0
    private var soundIdShield = 0
    private var soundIdHurt = 0
    private var streamIdLaserPowerUp = 0

    // --- MediaPlayer para a música de fundo longa ---
    private var bgMusicPlayer: MediaPlayer? = null

    fun init(context: Context) {
        // 1. Configuração do SoundPool para os EFEITOS SONOROS
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(7)
            .setAudioAttributes(audioAttributes)
            .build()

        // Carrega os efeitos sonoros curtos (sem a música)
        soundIdExplosion = soundPool.load(context, R.raw.shoot, 1)
        soundIdLego = soundPool.load(context, R.raw.lego, 1)
        soundIdGameOverSound = soundPool.load(context, R.raw.gameover, 1)
        soundIdLaser = soundPool.load(context, R.raw.laser, 1)
        soundIdLaserPowerUp = soundPool.load(context, R.raw.laserpowerup, 1)
        soundIdShield = soundPool.load(context, R.raw.shield, 1)
        soundIdHurt = soundPool.load(context, R.raw.hurt, 1)


        // 2. Configuração do MediaPlayer para a MÚSICA DE FUNDO
        if (bgMusicPlayer == null) {
            try {
                bgMusicPlayer = MediaPlayer.create(context, R.raw.bgmusic)
                bgMusicPlayer?.isLooping = true
                bgMusicPlayer?.setVolume(0.4f, 0.4f)
                Log.d("SoundManager", "MediaPlayer para a música de fundo criado com sucesso.")
            } catch (e: Exception) {
                Log.e("SoundManager", "FALHA CRÍTICA ao criar o MediaPlayer para a música de fundo.", e)
            }
        }
    }

    // --- Funções do SoundPool (usando as variáveis diretas que já funcionavam) ---
    fun playExplosion() {
        soundPool.play(soundIdExplosion, 1f, 1f, 0, 0, 1f)
    }

    fun playLaser() {
        soundPool.play(soundIdLaser, 0.5f, 0.5f, 0, 0, 1f)
    }

    fun playLaserPowerUp() {
        if (streamIdLaserPowerUp == 0) {
            streamIdLaserPowerUp = soundPool.play(soundIdLaserPowerUp, 0.7f, 0.7f, 1, -1, 1f)
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

    fun playHurt() {
        soundPool.play(soundIdHurt, 1f, 1f, 0, 0, 1f)
    }

    // --- Funções do MediaPlayer para a música ---
    fun playBgMusic() {
        if (bgMusicPlayer?.isPlaying == false) {
            Log.d("SoundManager", "A iniciar a música de fundo com MediaPlayer.")
            try {
                bgMusicPlayer?.start()
            } catch (e: IllegalStateException) {
                Log.e("SoundManager", "Erro ao iniciar o MediaPlayer.", e)
            }
        }
    }

    fun stopBgMusic() {
        if (bgMusicPlayer?.isPlaying == true) {
            Log.d("SoundManager", "A parar a música de fundo com MediaPlayer.")
            bgMusicPlayer?.pause()
        }
    }

    // --- Funções de Controlo Geral ---
    fun playGameOver() {
        soundPool.play(soundIdLego, 1f, 1f, 1, 0, 1f)
        soundPool.play(soundIdGameOverSound, 1f, 1f, 1, 0, 1f)
    }

    fun stopAll() {
        stopLaserPowerUp()
        soundPool.autoPause()
        stopBgMusic()
        Log.d("SoundManager", "Todos os sons parados.")
    }

    fun release() {
        Log.d("SoundManager", "A libertar todos os recursos de áudio.")
        soundPool.release()
        bgMusicPlayer?.release()
        bgMusicPlayer = null
    }
}
