package com.example.fallenhero

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView

class GameView(context: Context, private val screenWidth: Int, private val screenHeight: Int) : SurfaceView(context), Runnable {

    private var gameThread: Thread? = null
    private var isPlaying = false
    private var isGameOver = false

    private lateinit var surfaceHolder: SurfaceHolder
    private lateinit var canvas: Canvas
    private lateinit var paint: Paint
    private lateinit var laserPaint: Paint

    // Game Objects
    private lateinit var player: Player
    private lateinit var boom: Boom
    private lateinit var verticalLaser: Laser // Renamed for clarity
    private lateinit var horizontalLaser: HorizontalLaser
    private val orbs = ArrayList<Orb>()
    private val enemies = ArrayList<Enemy>()

    // Input state
    private var isTouching = false

    // Game State
    private var score = 0
    private var orbsCollected = 0
    private val orbsNeededForPowerup = 3
    var onGameOver: ((Int) -> Unit)? = null

    init {
        init(context, screenWidth, screenHeight)
    }

    private fun init(context: Context, width: Int, height: Int) {
        surfaceHolder = holder
        paint = Paint()
        laserPaint = Paint().apply { color = Color.MAGENTA }

        player = Player(context, width, height)
        boom = Boom(context, width, height)
        verticalLaser = Laser(height)
        horizontalLaser = HorizontalLaser(width)

        // Add one orb to the game
        orbs.add(Orb(context, width, height))

        repeat(3) { enemies.add(Enemy(context, width, height)) }

        SoundManager.init(context)
    }

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
        player.isBoosting = isTouching
        // Pass the power-up state to the player's update method
        player.update(horizontalLaser.isActive)

        // Update lasers and power-ups
        verticalLaser.update(isTouching, player)
        horizontalLaser.update(player)

        val backgroundSpeed = 10

        // Update Orbs and check for collision with player
        for (orb in orbs) {
            orb.update()
            if (Rect.intersects(player.collisionBox, orb.collisionBox)) {
                orbsCollected++
                orb.reset() // Reset the orb so it reappears

                if (orbsCollected >= orbsNeededForPowerup) {
                    horizontalLaser.activate() // Activate the power-up
                    orbsCollected = 0 // Reset the counter
                }
            }
        }

        // Update Enemies
        for (enemy in enemies) {
            enemy.update(backgroundSpeed)

            var enemyDestroyed = false

            // 1. Check for horizontal laser (power-up) collision
            if (horizontalLaser.isActive && horizontalLaser.collisionRect != null) {
                if (Rect.intersects(horizontalLaser.collisionRect!!, enemy.collisionBox)) {
                    triggerExplosion(enemy.x, enemy.y)
                    score += 20 // More points for a power-up kill
                    enemy.x = -300 // Move enemy off-screen
                    enemyDestroyed = true
                }
            }

            // 2. If not destroyed, check for vertical laser collision
            if (!enemyDestroyed && verticalLaser.isActive && verticalLaser.collisionRect != null) {
                if (Rect.intersects(verticalLaser.collisionRect!!, enemy.collisionBox)) {
                    triggerExplosion(enemy.x, enemy.y)
                    score += 100
                    enemy.x = -300
                    enemyDestroyed = true
                }
            }

            // 3. If still not destroyed, check for player collision
            if (!enemyDestroyed && Rect.intersects(enemy.collisionBox, player.collisionBox)) {
                triggerExplosion(enemy.x, enemy.y)
                enemy.x = -300
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

        // Draw Orbs
        for (orb in orbs) {
            canvas.drawBitmap(orb.bitmap, orb.x.toFloat(), orb.y.toFloat(), paint)
        }

        // Draw Enemies
        for (enemy in enemies) {
            canvas.drawBitmap(enemy.bitmap, enemy.x.toFloat(), enemy.y.toFloat(), paint)
        }

        // Draw Player
        canvas.drawBitmap(player.bitmap, player.x.toFloat(), player.y.toFloat(), paint)

        // Draw Lasers
        verticalLaser.draw(canvas, laserPaint)
        horizontalLaser.draw(canvas)

        // Draw Boom
        canvas.drawBitmap(boom.bitmap, boom.x.toFloat(), boom.y.toFloat(), paint)

        // UI - Score, Orbs, and Lives
        paint.color = Color.WHITE
        paint.textSize = 60f
        canvas.drawText("Score: $score", 50f, 80f, paint)

        paint.textSize = 40f
        canvas.drawText("Orbs: $orbsCollected / $orbsNeededForPowerup", 50f, 130f, paint)

        val livesText = "Vidas: ${player.health}"
        val textWidth = paint.measureText(livesText)
        canvas.drawText(livesText, width - textWidth - 50f, 80f, paint)

        // UI - Laser Energy Bar
        val barHeight = 20f
        val barMarginHorizontal = 100f
        val barMarginVertical = 10f
        val barWidth = width - (barMarginHorizontal * 2)
        val energyPercentage = verticalLaser.getEnergyPercentage()

        paint.color = Color.GRAY
        canvas.drawRect(barMarginHorizontal, barMarginVertical, barMarginHorizontal + barWidth, barMarginVertical + barHeight, paint)

        laserPaint.alpha = 200 // Slightly transparent
        canvas.drawRect(barMarginHorizontal, barMarginVertical, barMarginHorizontal + (barWidth * energyPercentage), barMarginVertical + barHeight, laserPaint)

        surfaceHolder.unlockCanvasAndPost(canvas)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> isTouching = true
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_CANCEL -> isTouching = false
        }
        return true
    }

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
