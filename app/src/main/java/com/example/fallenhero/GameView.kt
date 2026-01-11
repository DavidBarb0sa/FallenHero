package com.example.fallenhero

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView

class GameView : SurfaceView, Runnable {

    private var gameThread: Thread? = null
    private var isPlaying = false
    private var isGameOver = false

    private lateinit var surfaceHolder: SurfaceHolder
    private lateinit var canvas: Canvas
    private lateinit var paint: Paint
    private lateinit var laserPaint: Paint

    private lateinit var player: Player
    private lateinit var boom: Boom
    private lateinit var laser: Laser

    private val enemies = ArrayList<Enemy>()

    // Input state
    private var isTouching = false

    //GAME STATE
    private var score = 0
    var onGameOver: ((Int) -> Unit)? = null

    //SOUND

    constructor(context: Context, width: Int, height: Int) : super(context) {
        init(context, width, height)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr)

    private fun init(context: Context, width: Int, height: Int) {
        surfaceHolder = holder
        paint = Paint()
        laserPaint = Paint().apply { color = Color.MAGENTA }

        player = Player(context, width, height)
        boom = Boom(context, width, height)
        laser = Laser(height)

        repeat(3) { enemies.add(Enemy(context, width, height)) }

        SoundManager.init(context)
    }

    //GAME LOOP

    override fun run() {
        while (isPlaying) {
            update()
            drawGame()
            Thread.sleep(17)
        }
    }

    fun resume() {
        isPlaying = true
        gameThread = Thread(this)
        gameThread!!.start()
    }

    fun pause() {
        isPlaying = false
        gameThread?.join()
    }

    private fun update() {
        // Player always boosts if the screen is being touched.
        player.isBoosting = isTouching

        // Update player & laser based on the input.
        player.update()
        laser.update(isTouching, player)

        // Parallax speed for background elements
        val backgroundSpeed = 10

        // Enemies
        for (enemy in enemies) {
            enemy.update(backgroundSpeed)

            var enemyDestroyed = false

            // 1. Check for laser collision
            laser.collisionRect?.let {
                if (laser.isActive && Rect.intersects(it, enemy.collisionBox)) {
                    triggerExplosion(enemy.x, enemy.y)
                    score += 10
                    enemy.x = -300 // Move enemy off-screen
                    enemyDestroyed = true
                }
            }

            // 2. If not destroyed by laser, check for player collision
            if (!enemyDestroyed && Rect.intersects(enemy.collisionBox, player.collisionBox)) {
                triggerExplosion(enemy.x, enemy.y)
                enemy.x = -300 // Move enemy off-screen
                player.health--

                if (player.health <= 0 && !isGameOver) {
                    triggerGameOver()
                    return
                }
            }
        }

        boom.update()
    }

    private fun drawGame() {
        if (!surfaceHolder.surface.isValid) return

        canvas = surfaceHolder.lockCanvas()
        canvas.drawColor(Color.BLACK)

        // Enemies
        for (enemy in enemies) {
            canvas.drawBitmap(enemy.bitmap, enemy.x.toFloat(), enemy.y.toFloat(), paint)
        }

        // Player (no rotation)
        canvas.drawBitmap(player.bitmap, player.x.toFloat(), player.y.toFloat(), paint)

        // Laser beam
        laser.draw(canvas, laserPaint)

        // Boom
        canvas.drawBitmap(boom.bitmap, boom.x.toFloat(), boom.y.toFloat(), paint)

        // UI - Score and Lives
        paint.color = Color.WHITE
        paint.textSize = 60f
        canvas.drawText("Score: $score", 50f, 80f, paint)

        val livesText = "Vidas: ${player.health}"
        val textWidth = paint.measureText(livesText)
        canvas.drawText(livesText, width - textWidth - 50f, 80f, paint)

        // UI - Laser Energy Bar
        val barHeight = 20f
        val barMarginHorizontal = 100f
        val barMarginVertical = 10f
        val barWidth = width - (barMarginHorizontal * 2)
        val energyPercentage = laser.getEnergyPercentage()

        paint.color = Color.GRAY
        canvas.drawRect(barMarginHorizontal, barMarginVertical, barMarginHorizontal + barWidth, barMarginVertical + barHeight, paint)

        laserPaint.alpha = 200 // Slightly transparent
        canvas.drawRect(barMarginHorizontal, barMarginVertical, barMarginHorizontal + (barWidth * energyPercentage), barMarginVertical + barHeight, laserPaint)

        surfaceHolder.unlockCanvasAndPost(canvas)
    }

    // INPUT
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                isTouching = true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_CANCEL -> {
                isTouching = false
            }
        }
        return true
    }

    //HELPERS

    private fun triggerExplosion(x: Int, y: Int) {
        boom.isOnScreen = true
        boom.x = x
        boom.y = y
        SoundManager.playExplosion()
    }

    private fun triggerGameOver() {
        isGameOver = true
        isPlaying = false
        SoundManager.playGameOver()
        post { onGameOver?.invoke(score) }
    }
}
