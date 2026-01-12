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
    private val bullets = ArrayList<Bullet>()

    // Power-up Button
    private lateinit var powerUpButtonBitmap: Bitmap
    private lateinit var powerUpButtonRect: Rect
    private val buttonPaint = Paint()

    // Input state
    private var isFlying = false // Replaces isTouching

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

        orbs.add(Orb(context, width, height))
        repeat(2) { enemies.add(Enemy(context, width, height)) } // 2 normal enemies
        shooterEnemies.add(ShooterEnemy(context, width, height)) // 1 shooter enemy

        // Load and position the power-up button
        val btnScale = 1f
        powerUpButtonBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.btn0)
        val buttonWidth = (powerUpButtonBitmap.width * btnScale).toInt()
        val buttonHeight = (powerUpButtonBitmap.height * btnScale).toInt()
        powerUpButtonBitmap = Bitmap.createScaledBitmap(powerUpButtonBitmap, buttonWidth, buttonHeight, false)

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

        // Orb collection logic
        for (orb in orbs) {
            orb.update()
            if (Collision.checkPlayerCollision(player, orb.collisionBox)) {
                if (!isPowerUpAvailable) { // Prevent collecting more orbs than needed
                    orbsCollected++
                }
                orb.reset()
            }
        }
        isPowerUpAvailable = orbsCollected >= orbsNeededForPowerup

        // Enemy and bullet logic
        val backgroundSpeed = 10
        handleStandardEnemies()
        handleShooterEnemies(backgroundSpeed)
        handleBullets()

        boom.update()
    }

    private fun handleStandardEnemies() {
        for (enemy in enemies) {
            enemy.update(10)
            checkCollisions(enemy)
        }
    }

    private fun handleShooterEnemies(backgroundSpeed: Int) {
        for (shooter in shooterEnemies) {
            shooter.update(backgroundSpeed)
            checkCollisions(shooter)

            if (shooter.canShoot(player)) {
                bullets.add(shooter.shoot(player))
            }
        }
    }

    private fun handleBullets() {
        val iterator = bullets.iterator()
        while (iterator.hasNext()) {
            val bullet = iterator.next()
            bullet.update()
            if (bullet.isOffScreen(screenWidth, screenHeight)) {
                iterator.remove()
                continue
            }

            if (Collision.checkPlayerCollision(player, bullet.collisionBox)) {
                player.health--
                iterator.remove()
                if (player.health <= 0 && !isGameOver) {
                    triggerGameOver()
                    return
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
            triggerExplosion(enemyX, enemyY); score += 20; destroyed = true
        }
        if (!destroyed && verticalLaser.isActive && verticalLaser.collisionRect != null && Rect.intersects(verticalLaser.collisionRect!!, enemyBox)) {
            triggerExplosion(enemyX, enemyY); score += 10; destroyed = true
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
        
        // Draw enemies
        for (enemy in enemies) {
            canvas.drawBitmap(enemy.bitmap, enemy.x.toFloat(), enemy.y.toFloat(), paint)
        }
        for (shooter in shooterEnemies) {
            canvas.drawBitmap(shooter.bitmap, shooter.x.toFloat(), shooter.y.toFloat(), paint)
        }
        
        // Draw player
        canvas.drawBitmap(player.bitmap, player.x.toFloat(), player.y.toFloat(), paint)
        
        for (bullet in bullets) bullet.draw(canvas)
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

        paint.textSize = 40f
        canvas.drawText("Orbs: $orbsCollected / $orbsNeededForPowerup", 50f, 130f, paint)

        val livesText = "Vidas: ${player.health}"
        val textWidth = paint.measureText(livesText)
        canvas.drawText(livesText, screenWidth - textWidth - 50f, 80f, paint)

        val barHeight = 20f
        val barMarginHorizontal = 100f
        val barMarginVertical = 10f
        val barWidth = screenWidth - (barMarginHorizontal * 2)
        val energyPercentage = verticalLaser.getEnergyPercentage()
        paint.color = Color.GRAY
        canvas.drawRect(barMarginHorizontal, barMarginVertical, barMarginHorizontal + barWidth, barMarginVertical + barHeight, paint)
        laserPaint.alpha = 200
        canvas.drawRect(barMarginHorizontal, barMarginVertical, barMarginHorizontal + (barWidth * energyPercentage), barMarginVertical + barHeight, laserPaint)

        // Draw Power-up button
        if (isPowerUpAvailable) {
            buttonPaint.alpha = 255 // Fully opaque
        } else {
            buttonPaint.alpha = 100 // Semi-transparent
        }
        canvas.drawBitmap(powerUpButtonBitmap, powerUpButtonRect.left.toFloat(), powerUpButtonRect.top.toFloat(), buttonPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val touchX = event.x
        val touchY = event.y

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                // Check if the power-up button was pressed
                if (powerUpButtonRect.contains(touchX.toInt(), touchY.toInt())) {
                    if (isPowerUpAvailable) {
                        horizontalLaser.activate()
                        orbsCollected = 0
                        isPowerUpAvailable = false
                        // You could play a power-up activation sound here
                    }
                } else {
                    // If not pressing the button, then fly
                    isFlying = true
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_CANCEL -> {
                // Stop flying when touch is released
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
