package com.mygdx.game.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.Array
import com.mygdx.game.Game
import com.mygdx.game.model.PistolProjectile
import com.mygdx.game.model.Player
import com.mygdx.game.settings.PISTOL_BULLET_SPEED
import com.mygdx.game.settings.PLAYER_MOVEMENT_SPEED
import com.mygdx.game.settings.WINDOW_HEIGHT
import com.mygdx.game.settings.WINDOW_WIDTH
import ktx.app.KtxScreen
import ktx.assets.pool
import ktx.collections.iterate
import ktx.graphics.use

class GameScreen(
        val game: Game,
        val batch: SpriteBatch,
        val font: BitmapFont,
        val camera: OrthographicCamera) : KtxScreen {

    val player = Player(WINDOW_WIDTH / 2f - 16f, WINDOW_HEIGHT / 2f - 32f)
    val mousePosition = Vector2()

    val pistolProjectilePool = pool { PistolProjectile() }
    val pistolProjectiles = Array<PistolProjectile>()

    override fun render(delta: Float) {
        camera.update()
        getMousePosInGameWorld()
        setPlayerRotation()
        calculatePistolProjectilesPosition(delta)
        checkControls(delta)

        batch.projectionMatrix = camera.combined

        batch.use {
            drawPlayer(it, player)
            drawProjectiles(it)
        }
    }

    private fun drawPlayer(batch: Batch, player: Player) = player.sprite.draw(batch)

    private fun getMousePosInGameWorld() {
        val position = camera.unproject(Vector3(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f))
        mousePosition.x = position.x
        mousePosition.y = position.y
    }

    private fun setPlayerRotation() {
        val originX = player.sprite.originX + player.sprite.x
        val originY = player.sprite.originY + player.sprite.y
        val angle = MathUtils.atan2(mousePosition.y - originY, mousePosition.x - originX) * MathUtils.radDeg
        player.facingDirectionAngle = angle
    }

    private fun checkControls(delta: Float) {
        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT) && player.canShoot()) {
            player.shoot()
            val xCentre = player.bounds.x + player.bounds.width / 2f
            val yCentre = player.bounds.y + player.bounds.height / 2f
            spawnPistolProjectile(
                    xCentre, yCentre,
                    MathUtils.cosDeg(player.facingDirectionAngle),
                    MathUtils.sinDeg(player.facingDirectionAngle))
        }

        var movementSpeed = PLAYER_MOVEMENT_SPEED

        var pressedkeys = 0

        if (Gdx.input.isKeyPressed(Input.Keys.W)) pressedkeys++
        if (Gdx.input.isKeyPressed(Input.Keys.S)) pressedkeys++
        if (Gdx.input.isKeyPressed(Input.Keys.A)) pressedkeys++
        if (Gdx.input.isKeyPressed(Input.Keys.D)) pressedkeys++

        if (pressedkeys > 1) movementSpeed = (movementSpeed.toDouble() * 0.7).toInt()

        if (Gdx.input.isKeyPressed(Input.Keys.W))
            player.setPosition(player.bounds.x, player.bounds.y + movementSpeed * delta)

        if (Gdx.input.isKeyPressed(Input.Keys.S))
            player.setPosition(player.bounds.x, player.bounds.y - movementSpeed * delta)

        if (Gdx.input.isKeyPressed(Input.Keys.A))
            player.setPosition(player.bounds.x - movementSpeed * delta, player.bounds.y)

        if (Gdx.input.isKeyPressed(Input.Keys.D))
            player.setPosition(player.bounds.x + movementSpeed * delta, player.bounds.y)
    }

    fun calculatePistolProjectilesPosition(delta: Float) {
        pistolProjectiles.iterate { projectile, iterator ->
            projectile.setPosition(
                    projectile.bounds.x + projectile.speed.x * delta * PISTOL_BULLET_SPEED,
                    projectile.bounds.y + projectile.speed.y * delta * PISTOL_BULLET_SPEED)
        }
    }

    private fun spawnPistolProjectile(x: Float, y: Float, xSpeed: Float, ySpeed: Float) {
        val projectile = pistolProjectilePool.obtain()
        projectile.setPosition(x, y)
        projectile.speed.set(xSpeed, ySpeed)
        pistolProjectiles.add(projectile)
    }

    private fun drawProjectiles(batch: Batch) {
        pistolProjectiles.iterate { projectile, iterator ->
            projectile.sprite.draw(batch)
        }
    }
}