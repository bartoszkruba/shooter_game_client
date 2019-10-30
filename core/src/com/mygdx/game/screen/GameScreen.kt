package com.mygdx.game.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Intersector
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.Array
import com.mygdx.game.Game
import com.mygdx.game.model.*
import com.mygdx.game.settings.*
import ktx.app.KtxScreen
import ktx.assets.pool
import ktx.collections.iterate
import ktx.graphics.use
import java.util.*
import kotlin.collections.HashMap

class GameScreen(
        val game: Game,
        val batch: SpriteBatch,
        val font: BitmapFont,
        val camera: OrthographicCamera) : KtxScreen {

    val playerTexture = Texture(Gdx.files.internal("images/player_placeholder.png"))

    val player = Player(
            WINDOW_WIDTH / 2 - PLAYER_SPRITE_WIDTH / 2,
            WINDOW_HEIGHT / 2 - PLAYER_SPRITE_HEIGHT / 2, playerTexture)
    val mousePosition = Vector2()

    val pistolProjectilePool = pool { PistolProjectile() }

    val projectiles = Array<Projectile>()
    val opponents = HashMap<String, Opponent>()
    val walls = Array<Wall>()

    init {
        repeat(50) { opponents.put(UUID.randomUUID().toString(), generateRandomOpponent()) }
        generateWalls()
    }

    private var pressedKeys = 0

    override fun render(delta: Float) {
        getMousePosInGameWorld()
        setPlayerRotation()
        calculatePistolProjectilesPosition(delta)
        checkControls(delta)

        setCameraPosition()
        camera.update()
        batch.projectionMatrix = camera.combined

        batch.use {
            drawProjectiles(it)
            drawOpponents(it)
            drawPlayer(it, player)
            drawWalls(it)
        }
    }


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

        pressedKeys = 0

        if (Gdx.input.isKeyPressed(Input.Keys.W)) pressedKeys++
        if (Gdx.input.isKeyPressed(Input.Keys.S)) pressedKeys++
        if (Gdx.input.isKeyPressed(Input.Keys.A)) pressedKeys++
        if (Gdx.input.isKeyPressed(Input.Keys.D)) pressedKeys++

        if (pressedKeys > 1) movementSpeed = (movementSpeed.toDouble() * 0.7).toInt()

        if (Gdx.input.isKeyPressed(Input.Keys.W))
            movePlayer(player.bounds.x, player.bounds.y + movementSpeed * delta)

        if (Gdx.input.isKeyPressed(Input.Keys.S))
            movePlayer(player.bounds.x, player.bounds.y - movementSpeed * delta)

        if (Gdx.input.isKeyPressed(Input.Keys.A))
            movePlayer(player.bounds.x - movementSpeed * delta, player.bounds.y)

        if (Gdx.input.isKeyPressed(Input.Keys.D))
            movePlayer(player.bounds.x + movementSpeed * delta, player.bounds.y)
    }

    private fun movePlayer(x: Float, y: Float) = player.setPosition(
            MathUtils.clamp(x, WALL_SPRITE_WIDTH, MAP_WIDTH - WALL_SPRITE_WIDTH - PLAYER_SPRITE_WIDTH),
            MathUtils.clamp(y, WALL_SPRITE_HEIGHT, MAP_HEIGHT - WALL_SPRITE_HEIGHT - PLAYER_SPRITE_HEIGHT))


    fun calculatePistolProjectilesPosition(delta: Float) {
        projectiles.iterate { projectile, iterator ->
            projectile.setPosition(
                    projectile.bounds.x + projectile.velocity.x * delta * projectile.speed,
                    projectile.bounds.y + projectile.velocity.y * delta * projectile.speed)

            if (projectile.bounds.x < 0 || projectile.bounds.x > MAP_WIDTH ||
                    projectile.bounds.y < 0 || projectile.bounds.y > MAP_HEIGHT) {
                // todo should check projectile type
                pistolProjectilePool.free(projectile as PistolProjectile)
                iterator.remove()
                return
            }

            for (opponent in opponents.values) {
                if (Intersector.overlaps(projectile.bounds, opponent.bounds)) {
                    // todo should check projectile type
                    pistolProjectilePool.free(projectile as PistolProjectile)
                    iterator.remove()
                    return
                }
            }
        }
    }

    private fun spawnPistolProjectile(x: Float, y: Float, xSpeed: Float, ySpeed: Float) {
        val projectile = pistolProjectilePool.obtain()
        projectile.setPosition(x, y)
        projectile.velocity.set(xSpeed, ySpeed)
        projectiles.add(projectile)
    }

    private fun drawPlayer(batch: Batch, agent: Agent) = agent.sprite.draw(batch)

    private fun drawProjectiles(batch: Batch) = projectiles.forEach { it.sprite.draw(batch) }

    private fun drawOpponents(batch: Batch) = opponents.values.forEach { it.sprite.draw(batch) }

    private fun drawWalls(batch: Batch) = walls.forEach { it.sprite.draw(batch) }

    fun generateRandomOpponent(): Opponent {
        val minPosition = Vector2(WALL_SPRITE_WIDTH, WALL_SPRITE_HEIGHT)
        val maxPosition = Vector2(
                MAP_WIDTH - PLAYER_SPRITE_WIDTH - WALL_SPRITE_WIDTH,
                MAP_HEIGHT - PLAYER_SPRITE_HEIGHT - WALL_SPRITE_HEIGHT)

        return Opponent(MathUtils.random(minPosition.x, maxPosition.x), MathUtils.random(minPosition.y, maxPosition.y)
                , 0f, 0f, playerTexture)
    }

    private fun generateWalls() {
        for (i in 0 until MAP_HEIGHT step WALL_SPRITE_HEIGHT.toInt()) {
            walls.add(Wall(0f, i.toFloat()))
            walls.add(Wall(MAP_WIDTH - WALL_SPRITE_WIDTH, i.toFloat()))
        }
        for (i in WALL_SPRITE_WIDTH.toInt() until MAP_WIDTH - WALL_SPRITE_WIDTH.toInt() step WALL_SPRITE_WIDTH.toInt()) {
            walls.add(Wall(i.toFloat(), 0f))
            walls.add(Wall(i.toFloat(), MAP_HEIGHT - WALL_SPRITE_HEIGHT))
        }
    }

    private fun setCameraPosition() {
        if (player.bounds.x > WINDOW_WIDTH / 2f && player.bounds.x < MAP_WIDTH - WINDOW_WIDTH / 2f)
            camera.position.x = player.bounds.x

        if (player.bounds.y > WINDOW_HEIGHT / 2f && player.bounds.y < MAP_HEIGHT - WINDOW_HEIGHT / 2f)
            camera.position.y = player.bounds.y
    }

    override fun dispose() {
        playerTexture.dispose()
        super.dispose()
    }
}