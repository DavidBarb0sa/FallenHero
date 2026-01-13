package com.example.fallenhero.model.gameplay

import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.example.fallenhero.model.entities.Boom
import com.example.fallenhero.model.entities.Bullet
import com.example.fallenhero.model.entities.Enemy
import com.example.fallenhero.model.entities.HorizontalLaser
import com.example.fallenhero.model.entities.Laser
import com.example.fallenhero.model.entities.Orb
import com.example.fallenhero.model.entities.Player
import com.example.fallenhero.R
import com.example.fallenhero.model.entities.Shield
import com.example.fallenhero.model.entities.ShieldItem
import com.example.fallenhero.model.entities.ShooterEnemy
import com.example.fallenhero.model.audio.SoundManager

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
    private lateinit var horizontalLaser: HorizontalLaser // The orb power-up
    private lateinit var shield: Shield // The player's active shield effect
    private lateinit var shieldItem: ShieldItem // The new collectible shield item
    private val orbs = ArrayList<Orb>() // Orbs for the horizontal laser
    private val enemies = ArrayList<Enemy>()
    private val shooterEnemies = ArrayList<ShooterEnemy>()

    // UI Bitmaps
    private lateinit var heartBitmap: Bitmap
    private lateinit var heartlessBitmap: Bitmap

    private val bulletPool = ArrayList<Bullet>()
    private val maxBullets = 20

    // Orb Power-up system (restored)
    private val powerUpButtonBitmaps = ArrayList<Bitmap>()
    private lateinit var powerUpButtonRect: Rect
    private val buttonPaint = Paint()

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
        horizontalLaser = HorizontalLaser(width) // Restore horizontal laser
        shield = Shield(context) // Shield effect
        shieldItem = ShieldItem(context, width, height) // The collectible shield item

        // Load heart bitmaps for UI
        val originalHeart = BitmapFactory.decodeResource(context.resources, R.drawable.heart)
        val scaleFactor = 0.6f // Reduced scale factor
        val heartWidth = (originalHeart.width * scaleFactor).toInt()
        val heartHeight = (originalHeart.height * scaleFactor).toInt()
        heartBitmap = Bitmap.createScaledBitmap(originalHeart, heartWidth, heartHeight, false)
        val originalHeartless = BitmapFactory.decodeResource(context.resources, R.drawable.heartless)
        heartlessBitmap = Bitmap.createScaledBitmap(originalHeartless, heartWidth, heartHeight, false)

        for (i in 0 until maxBullets) {
            bulletPool.add(Bullet())
        }

        // Restore orbs
        orbs.add(Orb(context, width, height))
        
        repeat(2) { enemies.add(Enemy(context, width, height)) }
        shooterEnemies.add(ShooterEnemy(context, width, height))

        // Restore power-up button for orbs
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
        SoundManager.playBgMusic() // Começa a música de fundo
    }

    fun pause() {
        isPlaying = false
        gameThread?.join()
        SoundManager.stopBgMusic() // Para a música de fundo
    }

    private fun update() {
        background.update()
        player.isBoosting = isFlying
        player.update(horizontalLaser.isActive)

        verticalLaser.update(isFlying, player)
        shield.isActive = player.hasShield

        // Check if the horizontal laser just deactivated to stop the sound
        if (horizontalLaser.update(player)) {
            SoundManager.stopLaserPowerUp()
        }

        // --- System 1: Orb Collection for Horizontal Laser ---
        isPowerUpAvailable = orbsCollected >= orbsNeededForPowerup

        if (!isPowerUpAvailable) {
            for (orb in orbs) {
                orb.update()
                if (Collision.checkPlayerEllipseCollision(player, orb.collisionBox)) {
                    if (orbsCollected < orbsNeededForPowerup) {
                        orbsCollected++
                    }
                    orb.reset()
                }
            }
        }

        // --- System 2: Shield Item Logic ---
        if (!player.hasShield) {
            shieldItem.update()
            if (Collision.checkPlayerEllipseCollision(player, shieldItem.collisionBox)) {
                player.hasShield = true
                SoundManager.playShield() // Play sound on pickup
                shieldItem.reset()
            }
        }

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
                val bullet = bulletPool.firstOrNull { !it.isActive }
                if (bullet != null) {
                    bullet.reset(shooter, player)
                    SoundManager.playLaser() // Play sound on shoot
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
                    bullet.isActive = false
                    continue
                }

                if (Collision.checkPlayerEllipseCollision(player, bullet.collisionBox)) {
                    bullet.isActive = false
                    handlePlayerDamage()
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
            triggerExplosion(enemyX, enemyY, playSound = true); score += 150; destroyed = true
        }
        if (!destroyed && verticalLaser.isActive && verticalLaser.collisionRect != null && Rect.intersects(verticalLaser.collisionRect!!, enemyBox)) {
            triggerExplosion(enemyX, enemyY, playSound = true); score += 100; destroyed = true
        }
        if (!destroyed && Collision.checkPlayerEllipseCollision(player, enemyBox)) {
            destroyed = true // Enemy is always destroyed on collision with player

            if (player.hasShield) {
                // Shield takes the hit, play only shield sound
                handlePlayerDamage()
            } else {
                // No shield, trigger silent explosion and then take damage
                triggerExplosion(enemyX, enemyY, playSound = false)
                handlePlayerDamage()
            }
        }

        if (destroyed) {
            when (enemy) {
                is Enemy -> enemy.x = -300
                is ShooterEnemy -> enemy.x = -300
            }
        }
    }

    private fun handlePlayerDamage() {
        if (player.hasShield) {
            player.hasShield = false
            SoundManager.playShield() // Play sound when shield is hit
        } else {
            SoundManager.playHurt() // Play hurt sound
            player.health--
            if (player.health <= 0 && !isGameOver) {
                triggerGameOver()
            }
        }
    }

    private fun drawGame() {
        if (!surfaceHolder.surface.isValid) return

        canvas = surfaceHolder.lockCanvas()
        
        background.draw(canvas)

        // Draw collectibles
        if (!isPowerUpAvailable) {
            for (orb in orbs) {
                canvas.drawBitmap(orb.bitmap, orb.x.toFloat(), orb.y.toFloat(), paint)
            }
        }
        if (!player.hasShield) {
            canvas.drawBitmap(shieldItem.bitmap, shieldItem.x.toFloat(), shieldItem.y.toFloat(), paint)
        }
        
        for (enemy in enemies) {
            canvas.drawBitmap(enemy.bitmap, enemy.x.toFloat(), enemy.y.toFloat(), paint)
        }
        for (shooter in shooterEnemies) {
            canvas.drawBitmap(shooter.bitmap, shooter.x.toFloat(), shooter.y.toFloat(), paint)
        }
        
        // Draw player sprite
        canvas.drawBitmap(player.bitmap, player.x.toFloat(), player.y.toFloat(), paint)

        shield.draw(canvas, player, paint)
        
        for (bullet in bulletPool) {
            if(bullet.isActive) {
                bullet.draw(canvas)
            }
        }
        
        // Draw both lasers
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

        // Draw hearts for player health
        val heartMargin = 5f // Reduced margin to bring hearts closer
        val heartWidth = heartBitmap.width.toFloat()
        val startX = screenWidth - 50f - (3 * (heartWidth + heartMargin))

        for (i in 0 until 3) {
            val xPos = startX + i * (heartWidth + heartMargin)
            val yPos = 40f // Align with score roughly
            
            val bitmapToDraw = if (i < player.health) {
                heartBitmap
            } else {
                heartlessBitmap
            }
            canvas.drawBitmap(bitmapToDraw, xPos, yPos, paint)
        }

        val barHeight = 20f
        val barMarginHorizontal = 500f
        val barMarginVertical = 50f
        val barWidth = screenWidth - (barMarginHorizontal * 2)
        val energyPercentage = verticalLaser.getEnergyPercentage()
        paint.color = Color.GRAY
        canvas.drawRect(barMarginHorizontal, barMarginVertical, barMarginHorizontal + barWidth, barMarginVertical + barHeight, paint)
        laserPaint.alpha = 200
        canvas.drawRect(barMarginHorizontal, barMarginVertical, barMarginHorizontal + (barWidth * energyPercentage), barMarginVertical + barHeight, laserPaint)

        // Draw the orb power-up button
        val buttonIndex = orbsCollected.coerceIn(0, powerUpButtonBitmaps.size - 1)
        val currentButtonBitmap = powerUpButtonBitmaps[buttonIndex]
        canvas.drawBitmap(currentButtonBitmap, powerUpButtonRect.left.toFloat(), powerUpButtonRect.top.toFloat(), buttonPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val touchX = event.x
        val touchY = event.y

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                // Check for orb power-up button click
                if (powerUpButtonRect.contains(touchX.toInt(), touchY.toInt())) {
                    if (isPowerUpAvailable) {
                        horizontalLaser.activate()
                        SoundManager.playLaserPowerUp() // Start looping sound
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

    private fun triggerExplosion(x: Int, y: Int, playSound: Boolean = true) {
        boom.isOnScreen = true
        val yOffset = 50 // Makes the explosion appear a bit higher
        boom.x = x
        boom.y = y - yOffset
        if (playSound) {
            SoundManager.playExplosion()
        }
    }

    private fun triggerGameOver() {
        isGameOver = true
        isPlaying = false
        SoundManager.stopAll() // Stop all sounds before playing game over sounds
        SoundManager.playGameOver()
        post { onGameOver?.invoke(score) }
    }
}
