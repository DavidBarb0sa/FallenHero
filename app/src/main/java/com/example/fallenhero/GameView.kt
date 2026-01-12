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
    private lateinit var background: Background
    private lateinit var player: Player
    private lateinit var boom: Boom
    private lateinit var verticalLaser: Laser
    private lateinit var horizontalLaser: HorizontalLaser
    private val orbs = ArrayList<Orb>()
    private val enemies = ArrayList<Enemy>()
    private val shooterEnemies = ArrayList<ShooterEnemy>()
    
    // --- Object Pool for Bullets ---
    private val bulletPool = ArrayList<Bullet>()
    private val maxBullets = 20 // The maximum number of bullets on screen at once

    // Power-up Button
    private val powerUpButtonBitmaps = ArrayList<Bitmap>()
    private lateinit var powerUpButtonRect: Rect
    private val buttonPaint = Paint()

    // Input state
    private var isFlying = false

    // Game State
    private var score = 0
    private var orbsCollected = 0
    private val orbsNeededForPowerup = 3
    private var isPowerUpAvailable = false
    var onGameOver: ((Int) -> Unit)? = null

    init {
        init(context, screenWidth, screenHeight)
    }

    private fun init(context: Context, width: Int, height: Int) {
        surfaceHolder = holder
        paint = Paint()
        laserPaint = Paint().apply { color = Color.MAGENTA }

        background = Background(context, width, height)
        player = Player(context, width, height)
        boom = Boom(context, width, height)
        verticalLaser = Laser(height)
        horizontalLaser = HorizontalLaser(width)

        // Create the object pool for bullets
        for (i in 0 until maxBullets) {
            bulletPool.add(Bullet())
        }

        orbs.add(Orb(context, width, height))
        repeat(2) { enemies.add(Enemy(context, width, height)) }
        shooterEnemies.add(ShooterEnemy(context, width, height))

        // Load button sprites
        val buttonResIds = listOf(R.drawable.btn0, R.drawable.btn1, R.drawable.btn2, R.drawable.btn3)
        var buttonWidth = 0
        var buttonHeight = 0
        for (resId in buttonResIds) {
            val originalBitmap = BitmapFactory.decodeResource(context.resources, resId)
            if (buttonWidth == 0) {
                buttonWidth = (originalBitmap.width * 1.0f).toInt()
                buttonHeight = (originalBitmap.height * 1.0f).toInt()
            }
            powerUpButtonBitmaps.add(Bitmap.createScaledBitmap(originalBitmap, buttonWidth, buttonHeight, false))
        }
        val buttonMargin = 40
        val buttonX = width - buttonWidth - buttonMargin
        val buttonY = height - buttonHeight - buttonMargin
        powerUpButtonRect = Rect(buttonX, buttonY, buttonX + buttonWidth, buttonY + buttonHeight)

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
        background.update()
        player.isBoosting = isFlying
        player.update(horizontalLaser.isActive)

        verticalLaser.update(isFlying, player)
        horizontalLaser.update(player)

        // Orb collection
        for (orb in orbs) {
            orb.update()
            if (Collision.checkPlayerCollision(player, orb.collisionBox)) {
                if (orbsCollected < orbsNeededForPowerup) {
                    orbsCollected++
                }
                orb.reset()
            }
        }
        isPowerUpAvailable = orbsCollected >= orbsNeededForPowerup

        // Enemies and bullets
        handleStandardEnemies()
        handleShooterEnemies()
        handleBullets()

        boom.update()
    }

    private fun handleStandardEnemies() {
        for (enemy in enemies) {
            enemy.update(10)
            checkCollisions(enemy)
        }
    }

    private fun handleShooterEnemies() {
        for (shooter in shooterEnemies) {
            shooter.update(10)
            checkCollisions(shooter)

            if (shooter.canShoot(player)) {
                // Find an inactive bullet in the pool.
                val bullet = bulletPool.firstOrNull { !it.isActive }
                if (bullet != null) {
                    // Tell the bullet to reset itself using the shooter and player.
                    // NO NEW OBJECTS ARE CREATED HERE.
                    bullet.reset(shooter, player)
                    // Tell the shooter that a shot was fired to reset its cooldown.
                    shooter.onShotFired()
                }
            }
        }
    }

    private fun handleBullets() {
        for (bullet in bulletPool) {
            if (bullet.isActive) {
                bullet.update()

                if (bullet.isOffScreen(screenWidth, screenHeight)) {
                    bullet.isActive = false // Deactivate instead of removing
                    continue
                }

                if (Collision.checkPlayerCollision(player, bullet.collisionBox)) {
                    player.health--
                    bullet.isActive = false // Deactivate on impact
                    if (player.health <= 0 && !isGameOver) {
                        triggerGameOver()
                        return
                    }
                }
            }
        }
    }

    private fun checkCollisions(enemy: Any) {
        val enemyBox: Rect
        var enemyX: Int
        var enemyY: Int

        when (enemy) {
            is Enemy -> { enemyBox = enemy.collisionBox; enemyX = enemy.x; enemyY = enemy.y }
            is ShooterEnemy -> { enemyBox = enemy.collisionBox; enemyX = enemy.x; enemyY = enemy.y }
            else -> return
        }

        var destroyed = false

        if (horizontalLaser.isActive && horizontalLaser.collisionRect != null && Rect.intersects(horizontalLaser.collisionRect!!, enemyBox)) {
            triggerExplosion(enemyX, enemyY); score += 150; destroyed = true
        }
        if (!destroyed && verticalLaser.isActive && verticalLaser.collisionRect != null && Rect.intersects(verticalLaser.collisionRect!!, enemyBox)) {
            triggerExplosion(enemyX, enemyY); score += 100; destroyed = true
        }
        if (!destroyed && Collision.checkPlayerCollision(player, enemyBox)) {
            triggerExplosion(enemyX, enemyY); player.health--; destroyed = true
            if (player.health <= 0 && !isGameOver) { triggerGameOver(); return }
        }

        if (destroyed) {
            when (enemy) {
                is Enemy -> enemy.x = -300
                is ShooterEnemy -> enemy.x = -300
            }
        }
    }

    private fun drawGame() {
        if (!surfaceHolder.surface.isValid) return

        canvas = surfaceHolder.lockCanvas()
        
        background.draw(canvas)

        for (orb in orbs) canvas.drawBitmap(orb.bitmap, orb.x.toFloat(), orb.y.toFloat(), paint)
        
        for (enemy in enemies) canvas.drawBitmap(enemy.bitmap, enemy.x.toFloat(), enemy.y.toFloat(), paint)
        for (shooter in shooterEnemies) canvas.drawBitmap(shooter.bitmap, shooter.x.toFloat(), shooter.y.toFloat(), paint)
        
        canvas.drawBitmap(player.bitmap, player.x.toFloat(), player.y.toFloat(), paint)
        
        // Draw only active bullets from the pool
        for (bullet in bulletPool) {
            bullet.draw(canvas)
        }
        
        verticalLaser.draw(canvas, laserPaint)
        horizontalLaser.draw(canvas)

        canvas.drawBitmap(boom.bitmap, boom.x.toFloat(), boom.y.toFloat(), paint)

        drawUI()

        surfaceHolder.unlockCanvasAndPost(canvas)
    }

    private fun drawUI() {
        paint.color = Color.WHITE
        paint.textSize = 60f
        canvas.drawText("Score: $score", 50f, 80f, paint)

        val livesText = "Vidas: ${player.health}"
        val textWidth = paint.measureText(livesText)
        canvas.drawText(livesText, screenWidth - textWidth - 50f, 80f, paint)

        val barHeight = 20f
        val barMarginHorizontal = 500f
        val barMarginVertical = 50f
        val barWidth = screenWidth - (barMarginHorizontal * 2)
        val energyPercentage = verticalLaser.getEnergyPercentage()
        paint.color = Color.GRAY
        canvas.drawRect(barMarginHorizontal, barMarginVertical, barMarginHorizontal + barWidth, barMarginVertical + barHeight, paint)
        laserPaint.alpha = 200
        canvas.drawRect(barMarginHorizontal, barMarginVertical, barMarginHorizontal + (barWidth * energyPercentage), barMarginVertical + barHeight, laserPaint)

        // Draw the correct power-up button based on orbs collected
        val buttonIndex = orbsCollected.coerceIn(0, powerUpButtonBitmaps.size - 1)
        val currentButtonBitmap = powerUpButtonBitmaps[buttonIndex]
        canvas.drawBitmap(currentButtonBitmap, powerUpButtonRect.left.toFloat(), powerUpButtonRect.top.toFloat(), buttonPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val touchX = event.x
        val touchY = event.y

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                if (powerUpButtonRect.contains(touchX.toInt(), touchY.toInt())) {
                    if (isPowerUpAvailable) {
                        horizontalLaser.activate()
                        orbsCollected = 0
                        isPowerUpAvailable = false
                    }
                } else {
                    isFlying = true
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_CANCEL -> {
                isFlying = false
            }
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
