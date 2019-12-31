package com.mygdx.game.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.g2d.*
import com.badlogic.gdx.math.*
import com.badlogic.gdx.utils.TimeUtils
import com.mygdx.game.Game
import com.mygdx.game.assets.*
import com.mygdx.game.settings.*
import com.mygdx.game.util.getZonesForCircle
import com.mygdx.game.util.getZonesForRectangle
import ktx.app.KtxScreen
import ktx.graphics.use
import com.mygdx.game.util.inFrustum
import com.mygdx.game.client.Client
import com.mygdx.game.client.KeyMappings
import com.mygdx.game.model.agent.Agent
import com.mygdx.game.model.agent.Player
import com.mygdx.game.model.projectile.*
import ktx.collections.iterate
import com.mygdx.game.model.projectile.Projectile
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.collections.ArrayList

private enum class ShakeDirection { RIGHT, LEFT }

class GameScreen(
        val game: Game,
        private val batch: SpriteBatch,
        private val assets: AssetManager,
        private val camera: OrthographicCamera,
        private val font: BitmapFont) : KtxScreen {

    private val atlases = Atlases(assets)
    private val textures = Textures(assets)
    private val musics = Musics(assets)
    private val sounds = Sounds(assets)

    private val pools = Pools(textures, atlases)
    private val gameObj = GameObjects(textures)

    private val cursor = Pixmap(Gdx.files.internal(TextureAssets.MouseCrossHair.path))

    private var shouldPlayReload = false
    private var wWasPressed = false
    private var aWasPressed = false
    private var dWasPressed = false
    private var sWasPressed = false
    private var rWasPressed = false
    private var lmWasPressed = false
    private var scoreboardFont = BitmapFont()
    private var playersOnScoreboardFont = BitmapFont()

    private val mousePosition = Vector2()
    private var imgpos = 0.0
    private var imgposdir = 0.1
    private var showMiniMap = false

    private lateinit var player: Player

    private var shouldDeathSoundPlay = false

    private var shakeDirection = ShakeDirection.RIGHT
    private var lastExplosion = 0L

    init {
        Client.configSocketEvents(textures, atlases, pools, gameObj)
        playerRotationUpdateLoop()
    }

    private var pressedKeys = 0
    override fun render(delta: Float) {
        if (Client.shouldPlayDeathSound) {
            sounds.deathSound.play()
            Client.shouldPlayDeathSound = false
        }

        shouldPlayReload = Client.shouldPlayReload
        if (Client.getPlayer() != null) {
            player = Client.getPlayer()!!
        }

        Gdx.gl.glClearColor(45f / 255f, 40f / 255f, 50f / 255f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        if (::player.isInitialized) {
            getMousePosInGameWorld()
            setPlayerRotation()
//            updateServerMouse()
            sendControlsData()

            calculateProjectilePositions(delta)
            checkControls(delta)
            setCameraPosition()
            if (TimeUtils.millis() - lastExplosion < 1000) shakeCamera()
            checkRestart()
        }

        camera.update()
        batch.projectionMatrix = camera.combined

        if (::player.isInitialized) {
            batch.use {
                gameObj.ground.forEach { sprite -> if (inFrustum(camera, sprite)) sprite.draw(it) }
                drawBloodOnTheFloor(it)
                drawPickups(it)
                drawProjectiles(it)
                drawOpponents(it)
                moveOpponents(delta)
                drawPlayer(it, player)
                checkPlayerGotShot(it)
                checkOpponentsGotShot(it)
                removeUnnecessaryBloodOnTheFloor()
                if (shouldPlayReload) {
                    sounds.reloadSoundEffect.play()
                    shouldPlayReload = false
                    Client.shouldPlayReload = false
                }
                drawExplosiveBarrels(it)
                drawWalls(it)
                drawExplosions(it)
            }
        }

        val uiMatrix = camera.combined.cpy()
        uiMatrix.setToOrtho2D(0f, 0f, WINDOW_WIDTH, WINDOW_HEIGHT)

        batch.projectionMatrix = uiMatrix

        if (::player.isInitialized) {
            batch.use {
                drawGameOver(it)
                drawWeaponInfo(it)
                renderMiniMap(it)
                scoreboard(it)
                drawScoresOnBoard(it)
            }
        }
    }

    private fun drawScoresOnBoard(batch: SpriteBatch) {
        if (Gdx.input.isKeyPressed(Input.Keys.TAB)) {
            var t = 1.14f
            var sortedByKills = ArrayList<Agent>()
            gameObj.playerOnScoreboardTable.values.forEach { sortedByKills.add(it) }

            sortedByKills = ArrayList(sortedByKills.sortedWith(compareBy { it.kills }).reversed())

            for (it in sortedByKills) {
                playersOnScoreboardFont.color = if (it.id == player.id) Color.GREEN else Color.RED
                t += 0.05f

                playersOnScoreboardFont.draw(batch, "${sortedByKills.indexOf(it) + 1}",
                        WINDOW_WIDTH / 3.4f, WINDOW_HEIGHT / t)

                playersOnScoreboardFont.draw(batch, it.name, WINDOW_WIDTH / 2.4f, WINDOW_HEIGHT / t)
                playersOnScoreboardFont.draw(batch, "${it.kills}", WINDOW_WIDTH / 1.73f, WINDOW_HEIGHT / t)
                playersOnScoreboardFont.draw(batch, "${it.deaths}", WINDOW_WIDTH / 1.43f, WINDOW_HEIGHT / t)
            }
        }
    }

    private fun scoreboard(batch: SpriteBatch) {
        if (Gdx.input.isKeyPressed(Input.Keys.TAB)) {
//            val scoreboard = assets.get("scoreboard/scoreboardBackground.png", Texture::class.java)
            val c = batch.color
            batch.setColor(c.r, c.g, c.b, .3f)
            batch.draw(textures.scoreboardBackground, 0f, 0f, WINDOW_WIDTH, WINDOW_HEIGHT)

            batch.setColor(c.r, c.g, c.b, .6f)
            batch.draw(textures.scoreboardBackground, WINDOW_WIDTH / 3.8f, WINDOW_HEIGHT / 20f,
                    WINDOW_WIDTH / 2, WINDOW_HEIGHT / 1.1f)

//            val table = assets.get("scoreboard/scoreboardTable.png", Texture::class.java)
            batch.setColor(c.r, c.g, c.b, .8f)
            batch.draw(textures.scoreBoardTable, WINDOW_WIDTH / 3.8f, WINDOW_HEIGHT / 14f, WINDOW_WIDTH / 2,
                    WINDOW_HEIGHT / 1.15f)

            scoreboardFont.draw(batch, "RANK           PLAYER              KILLS            DEATHS",
                    WINDOW_WIDTH / 3.4f, WINDOW_HEIGHT / 1.09f)
            scoreboardFont.data.setScale(1.7f)
        }
    }

    private fun removeUnnecessaryBloodOnTheFloor() {
        val iterator = gameObj.bloodOnTheFloor.iterator()
        while (iterator.hasNext()) {
            val value = iterator.next()
            if (value.transparent < 0) {
                pools.bloodOnTheFloorPool.free(value)
                iterator.remove()
            }
        }
    }

    private fun drawBloodOnTheFloor(batch: Batch) {
        gameObj.bloodOnTheFloor.forEach {
            if (it.gotShot && it.transparent >= 0f) {
                it.bloodOnTheFloorSprite.draw(batch, it.transparent)
                it.changeTransparent()
            }
        }
    }

    private fun checkOpponentsGotShot(batch: Batch) {
        gameObj.opponents.values.forEach {
            if (it.gotShot && !it.isDead) {
                drawBloodOnPlayerBody(batch, it.bounds.x - 10f, it.bounds.y)
                gameObj.bloodOnTheFloor.add(
                        pools.bloodOnTheFloorPool.obtain().apply {
                            bloodOnTheFloorSprite.setPosition(it.bounds.x - 20f, it.bounds.y - 50f)
                            gotShot = true
                            transparent = 1f
                        }
                )
            }
        }
    }

    //todo two times same code - break into own func
    private fun checkPlayerGotShot(batch: Batch) {
        if (player.gotShot && !player.isDead) {
            drawBloodOnPlayerBody(batch, player.bounds.x - 10f, player.bounds.y)
            gameObj.bloodOnTheFloor.add(
                    pools.bloodOnTheFloorPool.obtain().apply {
                        bloodOnTheFloorSprite.setPosition(player.bounds.x - 20f, player.bounds.y - 50f)
                        gotShot = true
                        transparent = 1f
                    }
            )
        }
    }

    private fun drawBloodOnPlayerBody(batch: Batch, x: Float, y: Float) {
        batch.draw(textures.bloodAnimation, x, y, 65f, 65f)
    }


    private fun renderMiniMap(batch: Batch) {
        if (!player.isDead) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.M)) showMiniMap = !showMiniMap

            if (!showMiniMap) return

            imgpos += (imgposdir / 3)
            if (imgpos < 0.0) imgposdir = -imgposdir
            if (imgpos > 1.0) imgposdir = -imgposdir

            val playerSize = 6f
            val playerPosPercentageX = (player.bounds.x / MAP_WIDTH.toFloat()) * MINIMAP_SIZE
            val playerPosPercentageY = (player.bounds.y / MAP_HEIGHT.toFloat()) * MINIMAP_SIZE

            val c = batch.color
            batch.setColor(c.r, c.g, c.b, .5f)
            batch.draw(textures.minimap, 0f, 0f, MINIMAP_SIZE, MINIMAP_SIZE)
            batch.setColor(c.r, c.g, c.b, 1f)

            batch.draw(textures.playerOnMinimap, playerPosPercentageX - playerSize / 2f,
                    playerPosPercentageY - playerSize / 2f, playerSize, playerSize)

            batch.setColor(c.r, c.g, c.b, imgpos.toFloat())
            for (opponent in gameObj.opponents.values) if (!opponent.isDead) {
                batch.draw(textures.opponentOnMinimap,
                        ((opponent.bounds.x / MAP_WIDTH.toFloat()) * MINIMAP_SIZE) - playerSize / 2f,
                        ((opponent.bounds.y / MAP_HEIGHT.toFloat()) * MINIMAP_SIZE) - playerSize / 2f,
                        playerSize,
                        playerSize)
            }
        }
    }

    private fun checkRestart() {
        if (player.isDead) {
            if (Gdx.input.isButtonJustPressed((Input.Buttons.LEFT))) {
                Client.broadcastRestart()
            }
        }
    }

    private fun drawGameOver(batch: Batch) {
        if (player.isDead) {
            if (shouldDeathSoundPlay) {
                sounds.deathSound.play()
                shouldDeathSoundPlay = false
            }
            val c: Color = batch.color
            batch.setColor(c.r, c.g, c.b, .7f)
            batch.draw(textures.gameOver, 0f, 0f, WINDOW_WIDTH, WINDOW_HEIGHT)
        } else {
            shouldDeathSoundPlay = true
        }
    }

    private fun playerRotationUpdateLoop() = GlobalScope.launch {
        while (true) {
            if (::player.isInitialized) {
                Client.brodcastPlayerFacingDirection()
                delay(100)
            }
        }
    }

    private fun sendControlsData() {
        val wIsPressed = Gdx.input.isKeyPressed(Input.Keys.W)
        val aIsPressed = Gdx.input.isKeyPressed(Input.Keys.A)
        val sIsPressed = Gdx.input.isKeyPressed(Input.Keys.S)
        val dIsPressed = Gdx.input.isKeyPressed(Input.Keys.D)
        val rIsPressed = Gdx.input.isKeyPressed(Input.Keys.R)
        val lmIsPressed = Gdx.input.isButtonPressed((Input.Buttons.LEFT))

        if (wWasPressed && !wIsPressed) Client.broadcastKeyReleased(KeyMappings.UP)
        else if (wIsPressed && !wWasPressed) Client.broadcastKeyPressed(KeyMappings.UP)

        if (sWasPressed && !sIsPressed) Client.broadcastKeyReleased(KeyMappings.DOWN)
        else if (sIsPressed && !sWasPressed) Client.broadcastKeyPressed(KeyMappings.DOWN)

        if (aWasPressed && !aIsPressed) Client.broadcastKeyReleased(KeyMappings.LEFT)
        else if (aIsPressed && !aWasPressed) Client.broadcastKeyPressed(KeyMappings.LEFT)

        if (dWasPressed && !dIsPressed) Client.broadcastKeyReleased(KeyMappings.RIGHT)
        else if (dIsPressed && !dWasPressed) Client.broadcastKeyPressed(KeyMappings.RIGHT)

        if (rWasPressed && !rIsPressed) Client.broadcastKeyReleased(KeyMappings.RELOAD)
        else if (rIsPressed && rWasPressed) Client.broadcastKeyPressed(KeyMappings.RELOAD)

        if (lmWasPressed && !lmIsPressed) Client.broadcastKeyReleased(KeyMappings.LEFT_MOUSE)
        else if (lmIsPressed && !lmWasPressed) Client.broadcastKeyPressed(KeyMappings.LEFT_MOUSE)

        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) Client.broadcastKeyPressed(KeyMappings.PICK_WEAPON)

        if (lmIsPressed && player.weapon.bulletsInChamber < 1 && player.weapon.canShoot()) {
            sounds.dryfire.play()
            player.weapon.shoot()
        }

        wWasPressed = wIsPressed
        aWasPressed = aIsPressed
        sWasPressed = sIsPressed
        dWasPressed = dIsPressed
        rWasPressed = rIsPressed
        lmWasPressed = lmIsPressed
    }

    private fun getMousePosInGameWorld() {
        val position = camera.unproject(Vector3(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f))
        mousePosition.x = position.x
        mousePosition.y = position.y
    }

    private fun setPlayerRotation() {
        val originX = player.sprite.originX + player.sprite.x
        val originY = player.sprite.originY + player.sprite.y
        var angle = MathUtils.atan2(mousePosition.y - originY, mousePosition.x - originX) * MathUtils.radDeg
        if (angle < 0) angle += 360f
        if (angle > 360) angle = 0f
        player.setAngle(angle)
    }

    private fun checkControls(delta: Float) {
        var movementSpeed = PLAYER_MOVEMENT_SPEED
        pressedKeys = 0
        if (!player.isDead) {

            if (Gdx.input.isKeyPressed(Input.Keys.W)) {
                pressedKeys++
            }
            if (Gdx.input.isKeyPressed(Input.Keys.S)) {
                pressedKeys++
            }
            if (Gdx.input.isKeyPressed(Input.Keys.A)) {
                pressedKeys++
            }
            if (Gdx.input.isKeyPressed(Input.Keys.D)) {
                pressedKeys++
            }

            if (pressedKeys > 1) movementSpeed = (movementSpeed.toDouble() * 0.7).toInt()

            player.isMoving = pressedKeys > 0

            if (Gdx.input.isKeyPressed(Input.Keys.W))
                movePlayer(player.bounds.x, player.bounds.y + movementSpeed * delta, player.bounds.x, player.bounds.y)

            if (Gdx.input.isKeyPressed(Input.Keys.S))
                movePlayer(player.bounds.x, player.bounds.y - movementSpeed * delta, player.bounds.x, player.bounds.y)

            if (Gdx.input.isKeyPressed(Input.Keys.A))
                movePlayer(player.bounds.x - movementSpeed * delta, player.bounds.y, player.bounds.x, player.bounds.y)

            if (Gdx.input.isKeyPressed(Input.Keys.D))
                movePlayer(player.bounds.x + movementSpeed * delta, player.bounds.y, player.bounds.x, player.bounds.y)
        }

    }

    private fun moveOpponents(delta: Float) {
        for (entry in gameObj.opponents.entries) {

            if (agentOutSideViewport(entry.value)) gameObj.opponents.remove(entry.key)

            val oldX = entry.value.bounds.x
            val oldY = entry.value.bounds.y
            entry.value.isMoving = entry.value.velocity.x != 0.0f || entry.value.velocity.y != 0.0f
            entry.value.setPosition(
                    entry.value.bounds.x + entry.value.velocity.x * delta,
                    entry.value.bounds.y + entry.value.velocity.y * delta)
            val zones = getZonesForRectangle(player.bounds)

            var collided = false
            for (i in 0 until zones.size) {
                for (j in 0 until gameObj.wallMatrix[zones[i]]!!.size) {
                    if (Intersector.overlaps(gameObj.wallMatrix[zones[i]]!![j].bounds, player.bounds)) {
                        entry.value.setPosition(oldX, oldY)
                        collided = true
                        break
                    }
                    if (collided) break
                }
            }
        }
    }

    private fun agentOutSideViewport(agent: Agent) =
            agent.bounds.x + 0.5 * PLAYER_SPRITE_WIDTH < camera.position.x - WINDOW_WIDTH ||
                    agent.bounds.x + 0.5 * PLAYER_SPRITE_WIDTH > camera.position.x + WINDOW_WIDTH ||
                    agent.bounds.y + 0.5 * PLAYER_SPRITE_HEIGHT < camera.position.y - WINDOW_HEIGHT ||
                    agent.bounds.y + 0.5 * PLAYER_SPRITE_HEIGHT > camera.position.y + WINDOW_HEIGHT


    private fun movePlayer(x: Float, y: Float, oldX: Float, oldY: Float) {
        player.setPosition(
                MathUtils.clamp(x, WALL_SPRITE_WIDTH, MAP_WIDTH - WALL_SPRITE_WIDTH - PLAYER_SPRITE_WIDTH),
                MathUtils.clamp(y, WALL_SPRITE_HEIGHT, MAP_HEIGHT - WALL_SPRITE_HEIGHT - PLAYER_SPRITE_HEIGHT))
        val zones = getZonesForRectangle(player.bounds)
        var collided = false
        for (i in 0 until zones.size) {
            if (collided) return
            for (j in 0 until gameObj.wallMatrix[zones[i]]!!.size) {
                if (Intersector.overlaps(gameObj.wallMatrix[zones[i]]!![j].bounds, player.bounds)) {
                    player.setPosition(oldX, oldY)
                    collided = true
                    break
                }
                if (collided) break
            }
        }
        for (barrel in gameObj.explosiveBarrels) {
            if (Intersector.overlaps(barrel.value.bounds, player.bounds)) {
                player.setPosition(oldX, oldY)
                break
            }
        }
    }


    private fun calculateProjectilePositions(delta: Float) {
        var removed = false
        for (entry in gameObj.projectiles.entries) {
            removed = false
            if (entry.value.justFired) {
                entry.value.justFired = false
                when (entry.value) {
                    is ShotgunProjectile -> sounds.shotgunShotSoundEffect.play(0.2f)
                    is MachineGunProjectile -> sounds.machineGunShotSoundEffect.play()
                    is BazookaProjectile -> sounds.bazookaShotSoundEffect.play()
                    else -> sounds.pistolShotSoundEffect.play(1.5f)
                }
            }
            entry.value.setPosition(
                    entry.value.bounds.x + entry.value.velocity.x * delta * entry.value.speed,
                    entry.value.bounds.y + entry.value.velocity.y * delta * entry.value.speed)

            removed = checkIfOutsideMap(entry.value, entry.key)
            if (!removed) removed = checkIfOutsideViewport(entry.value, entry.key)
            if (!removed) removed = checkOpponentCollisions(entry.value, entry.key)
            if (!removed) removed = checkPlayerCollision(entry.value, entry.key)
            if (!removed) removed = checkBarrelCollisions(entry.value, entry.key)
            if (!removed) checkWallsCollisions(entry.value, entry.key)
        }
    }

    private fun checkIfOutsideViewport(projectile: Projectile, key: String): Boolean {
        if (
                projectile.bounds.x < camera.position.x - WINDOW_WIDTH ||
                projectile.bounds.x > camera.position.x + WINDOW_WIDTH ||
                projectile.bounds.y < camera.position.y - WINDOW_HEIGHT ||
                projectile.bounds.y > camera.position.y + WINDOW_HEIGHT) {

            removeProjectile(projectile, key)
            return true
        }
        return false
    }

    private fun checkIfOutsideMap(projectile: Projectile, key: String): Boolean {
        if (projectile.bounds.x < 0 || projectile.bounds.x > MAP_WIDTH ||
                projectile.bounds.y < 0 || projectile.bounds.y > MAP_HEIGHT) {
            removeProjectile(projectile, key)
            return true
        }
        return false
    }

    private fun checkOpponentCollisions(projectile: Projectile, key: String): Boolean {
        for (opponent in gameObj.opponents.entries) {
            if (Intersector.overlaps(projectile.bounds, opponent.value.bounds) && !opponent.value.isDead &&
                    projectile.agentId != opponent.value.id) {
                removeProjectile(projectile, key)
                if (!opponent.value.isDead) sounds.damageSound.play()
                return true
            }
        }
        return false
    }

    private fun checkPlayerCollision(projectile: Projectile, key: String): Boolean {
        if (Intersector.overlaps(projectile.bounds, player.bounds) && projectile.agentId != player.id) {
            removeProjectile(projectile, key)
            if (!player.isDead) sounds.damageSound.play()
            return true
        }
        return false
    }

    private fun checkBarrelCollisions(projectile: Projectile, key: String): Boolean {
        for (barrel in gameObj.explosiveBarrels) {
            if (Intersector.overlaps(projectile.bounds, barrel.value.bounds)) {
                removeProjectile(projectile, key)
                return true
            }
        }
        return false
    }

    private fun checkWallsCollisions(projectile: Projectile, key: String): Boolean {
        for (zone in getZonesForCircle(projectile.bounds)) {
            if (gameObj.wallMatrix[zone] != null) for (i in 0 until gameObj.wallMatrix[zone]!!.size) {
                if (Intersector.overlaps(projectile.bounds, gameObj.wallMatrix[zone]!![i].bounds)) {
                    removeProjectile(projectile, key)
                    return true
                }
            }
        }
        return false
    }

    private fun removeProjectile(projectile: Projectile, key: String) {
        when (projectile) {
            is PistolProjectile -> pools.pistolProjectilePool.free(projectile)
            is MachineGunProjectile -> pools.machineGunProjectilePool.free(projectile)
            is ShotgunProjectile -> pools.shotgunProjectilePool.free(projectile)
            is BazookaProjectile -> pools.bazookaProjectilePool.free(projectile)
        }
        gameObj.projectiles.remove(key)
    }

    private fun drawPlayer(batch: Batch, agent: Agent) {
        if (!player.isDead && player.currentHealth >= 10) {
            setPlayerRotation()
            agent.sprite.draw(batch)
            agent.healthBarSprite.draw(batch)
            font.draw(batch, player.name, player.bounds.x + 10f, player.bounds.y + 88f)
        }
    }

    private fun drawProjectiles(batch: Batch) = gameObj.projectiles.values.forEach {
        it.sprite.draw(batch)
    }

    private fun drawExplosions(batch: Batch) {
        gameObj.bazookaExplosions.iterate { explosion, iterator ->
            if (explosion.justSpawned) {
                lastExplosion = TimeUtils.millis()
                sounds.explosionSoundEffect.play()
                explosion.justSpawned = false
            }
            explosion.animate()
            if (explosion.isFinished()) {
                pools.bazookaExplosionPool.free(explosion)
                iterator.remove()
            }
            explosion.sprite.draw(batch)
        }
        gameObj.barrelExplosions.iterate { explosion, iterator ->
            if (explosion.justSpawned) {
                lastExplosion = TimeUtils.millis()
                sounds.explosionSoundEffect.play()
                explosion.justSpawned = false
            }
            explosion.animate()
            if (explosion.isFinished()) {
                pools.barrelExplosionPool.free(explosion)
                iterator.remove()
            }
            explosion.sprite.draw(batch)
        }
    }

    private fun drawOpponents(batch: Batch) {
        gameObj.opponents.values.forEach {
            if (!it.isDead) {
                it.healthBarSprite.draw(batch)
                it.sprite.draw(batch)
                font.draw(batch, it.name, it.bounds.x + 10f, it.bounds.y + 88f)
            }
        }
    }

    private fun drawWalls(batch: Batch) {
        for (i in 0 until gameObj.walls.size) if (inFrustum(camera, gameObj.walls[i])) gameObj.walls[i].draw(batch)
    }

    private fun drawExplosiveBarrels(batch: Batch) {
        for (barrel in gameObj.explosiveBarrels.values) barrel.draw(batch)
    }

    private fun drawPickups(batch: Batch) {
        for (pickup in gameObj.pickups.values) pickup.sprite.draw(batch)
    }

    private fun drawWeaponInfo(batch: Batch) {
        batch.setColor(batch.color.r, batch.color.g, batch.color.b, 0.5f)

        batch.draw(textures.weaponUIPanel, WINDOW_WIDTH - 175f, WINDOW_HEIGHT - 80f, 175f, 80f)
        val weaponName = when (player.weapon.type) {
            ProjectileType.PISTOL -> "Pistol"
            ProjectileType.MACHINE_GUN -> "Machine Gun"
            ProjectileType.SHOTGUN -> "Shotgun"
            ProjectileType.BAZOOKA -> "Bazooka"
            else -> "Unknown"
        }
        font.draw(batch, weaponName, WINDOW_WIDTH - 100f, WINDOW_HEIGHT - 17f)

        val weaponTexture = when (player.weapon.type) {
            ProjectileType.MACHINE_GUN -> textures.machineGunTexture
            ProjectileType.SHOTGUN -> textures.shotgunTexture
            ProjectileType.BAZOOKA -> textures.bazookaTexture
            else -> textures.pistolTexture
        }
        batch.setColor(batch.color.r, batch.color.g, batch.color.b, 1f)
        batch.draw(weaponTexture, WINDOW_WIDTH - 165f, WINDOW_HEIGHT - weaponTexture.height - 14f)

        if (player.weapon.bulletsInChamber != -1 && !player.isDead) {

            font.draw(batch, "Ammo: ${player.weapon.bulletsInChamber}/${player.weapon.maxBulletsInChamber}",
                    WINDOW_WIDTH - 100f, WINDOW_HEIGHT - 47f)

            font.data.setScale(1f, 1f)
        } else {
            if (!player.isDead) font.draw(batch, "Reloading...", WINDOW_WIDTH - 100f, WINDOW_HEIGHT - 47f)
            font.data.setScale(1f, 1f)
        }
    }

    private var shake = 1
    private var shakeLoop = TimeUtils.millis()

    private fun shakeCamera() {
        if (TimeUtils.millis() - shakeLoop > 50f) {
            shakeLoop = TimeUtils.millis()
            shake = 1
            shakeDirection = if (shakeDirection == ShakeDirection.LEFT) ShakeDirection.RIGHT
            else ShakeDirection.LEFT
        }

        when (shakeDirection) {
            ShakeDirection.RIGHT -> {
                camera.position.x += shake++
            }
            ShakeDirection.LEFT -> {
                camera.position.x -= shake++
            }
        }
    }

    private fun setCameraPosition() {
        camera.position.x = MathUtils.clamp(player.bounds.x, WINDOW_WIDTH / 2f, MAP_WIDTH - WINDOW_WIDTH / 2f)
        camera.position.y = MathUtils.clamp(player.bounds.y, WINDOW_HEIGHT / 2f, MAP_HEIGHT - WINDOW_HEIGHT / 2f)
    }

    fun playGameScreenMusic() {
        Gdx.graphics.setCursor(Gdx.graphics.newCursor(cursor, 16, 16))
        musics.music.isLooping = true
        musics.music.volume = 0.4f
//        musics.music.play()
    }
}