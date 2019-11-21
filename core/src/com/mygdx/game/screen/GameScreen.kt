package com.mygdx.game.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.g2d.*
import com.badlogic.gdx.math.*
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.Pool
import com.badlogic.gdx.utils.TimeUtils
import com.mygdx.game.Game
import com.mygdx.game.settings.*
import com.mygdx.game.util.generateWallMatrix
import com.mygdx.game.util.getZonesForCircle
import com.mygdx.game.util.getZonesForRectangle
import ktx.app.KtxScreen
import ktx.graphics.use
import com.mygdx.game.util.inFrustum
import frontendServer.Server
import java.util.concurrent.ConcurrentHashMap
import com.mygdx.game.model.pickup.Pickup
import com.mygdx.game.model.agent.Opponent
import com.mygdx.game.model.agent.Agent
import com.mygdx.game.model.agent.Player
import com.mygdx.game.model.explosion.BarrelExplosion
import com.mygdx.game.model.explosion.BazookaExplosion
import com.mygdx.game.model.obstacles.ExplosiveBarrel
import com.mygdx.game.model.obstacles.Wall
import com.mygdx.game.model.projectile.*
import ktx.assets.pool
import ktx.collections.iterate
import com.mygdx.game.model.projectile.Projectile
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


private enum class ShakeDirection { RIGHT, LEFT }

class GameScreen(
        val game: Game,
        private val batch: SpriteBatch,
        private val assets: AssetManager,
        private val camera: OrthographicCamera,
        private val font: BitmapFont) : KtxScreen {

    private val playerAtlas = assets.get<TextureAtlas>("images/player/player.atlas")
    private val bazookaExplosionAtlas = assets.get<TextureAtlas>("images/bazooka/bazooka_explosion.atlas")

    private val projectileTexture = assets.get("images/projectile.png", Texture::class.java)
    private val wallTexture = assets.get("images/brickwall2.jpg", Texture::class.java)
    private val healthBarTexture = assets.get("images/healthBar3.png", Texture::class.java)

    private val pistolTexture = assets.get("images/pistol.png", Texture::class.java)
    private val machineGunTexture = assets.get("images/machine_gun.png", Texture::class.java)
    private val shotgunTexture = assets.get("images/shotgun.png", Texture::class.java)
    private val bazookaTexture = assets.get("images/bazooka.png", Texture::class.java)

    private val music = assets.get("music/ingame_music.ogg", Music::class.java)

    private val deathSound = assets.get("sounds/deathSound.wav", Sound::class.java)
    private val damageSound = assets.get("sounds/damage.mp3", Sound::class.java)

    private val pistolShotSoundEffect = assets.get("sounds/pistol_shot.wav", Sound::class.java)
    private val shotgunShotSoundEffect = assets.get("sounds/shotgun_shot.wav", Sound::class.java)
    private val machineGunShotSoundEffect = assets.get("sounds/machine_gun_shot.wav", Sound::class.java)
    private val bazookaShotSoundEffect = assets.get("sounds/bazooka_shot.mp3", Sound::class.java)
    private val dryfire = assets.get("sounds/dryfire.mp3", Sound::class.java)

    private val bazookaExplosionSoundEffect = assets.get("sounds/bazooka_explosion.mp3", Sound::class.java)

    private val reloadSoundEffect = assets.get("sounds/reload_sound.mp3", Sound::class.java)

    private val groundTexture = assets.get("images/ground.jpg", Texture::class.java)
    private val bloodOnTheFloorTexture = assets.get("images/blood-onTheFloor.png", Texture::class.java)
    private val explosiveBarrelTexture = assets.get("images/explosive_barrel.png", Texture::class.java)

    private val cursor = Pixmap(Gdx.files.internal("images/crosshair.png"))

    private var shouldPlayReload = false
    private var opponents = ConcurrentHashMap<String, Opponent>()
    private var wWasPressed = false
    private var aWasPressed = false
    private var dWasPressed = false
    private var sWasPressed = false
    private var rWasPressed = false
    private var mouseWasPressed = false
    private var forIf = true
    private var scoreboardFont = BitmapFont()
    private var playersOnScoreboardFont = BitmapFont()

    private val playerTextures: Array<Texture> = Array()
    private val mousePosition = Vector2()
    private val walls = Array<Wall>()
    private val wallMatrix: HashMap<String, Array<Wall>>

    private val projectiles: ConcurrentHashMap<String, Projectile>

    private val pistolProjectilePool: Pool<PistolProjectile>
    private val machineGunProjectilePool: Pool<MachineGunProjectile>
    private val shotgunProjectilePool: Pool<ShotgunProjectile>
    private val bazookaProjectilePool: Pool<BazookaProjectile>

    private val bazookaExplosionPool: Pool<BazookaExplosion>
    private val barrelExplosionPool: Pool<BarrelExplosion>

    private val bazookaExplosions: Array<BazookaExplosion>
    private val barrelExplosions: Array<BarrelExplosion>

    private val pickups: ConcurrentHashMap<String, Pickup>
    private val explosiveBarrels: ConcurrentHashMap<String, ExplosiveBarrel>
    private var imgpos = 0.0
    private var imgposdir = 0.1
    private var showMiniMap = 0

    private val ground = Array<Sprite>()
    private lateinit var player: Player

    private var bloodOnTheFloor = ArrayList<Blood>()
    private val bloodOnTheFloorPool = pool { Blood(bloodOnTheFloorTexture) }
    private var shouldDeathSoundPlay = false

    private var playerOnScoreboardTable: ConcurrentHashMap<String, Agent> = ConcurrentHashMap()

    private var shakeDirection = ShakeDirection.RIGHT
    private var lastExplosion = 0L

    init {
        playerTextures.add(assets.get("images/player/up.png", Texture::class.java))
        playerTextures.add(assets.get("images/player/down.png", Texture::class.java))
        playerTextures.add(assets.get("images/player/left.png", Texture::class.java))
        playerTextures.add(assets.get("images/player/right.png", Texture::class.java))
        wallMatrix = generateWallMatrix()

        for (i in 0 until (MAP_HEIGHT % GROUND_TEXTURE_HEIGHT + 1).toInt()) {
            for (j in 0 until (MAP_WIDTH % GROUND_TEXTURE_WIDTH + 1).toInt()) {
                val groundSprite = Sprite(groundTexture)
                groundSprite.setPosition(i * GROUND_TEXTURE_WIDTH, j * GROUND_TEXTURE_HEIGHT)
                groundSprite.setSize(GROUND_TEXTURE_WIDTH, GROUND_TEXTURE_HEIGHT)
                ground.add(groundSprite)
            }
        }
        Server.configSocketEvents(projectileTexture, pistolTexture, machineGunTexture, shotgunTexture, bazookaTexture,
                playerAtlas, healthBarTexture, bazookaExplosionAtlas, wallMatrix, wallTexture, explosiveBarrelTexture, walls)

        projectiles = Server.projectiles
        opponents = Server.opponents

        pistolProjectilePool = Server.pistolProjectilePool
        machineGunProjectilePool = Server.machineGunProjectilePool
        shotgunProjectilePool = Server.shotgunProjectilePool
        bazookaProjectilePool = Server.bazookaProjectilePool

        bazookaExplosionPool = Server.bazookaExplosionPool
        bazookaExplosions = Server.bazookaExplosions

        barrelExplosionPool = Server.barrelExplosionPool
        barrelExplosions = Server.barrelExplosions

        pickups = Server.pickups
        explosiveBarrels = Server.explosiveBarrels
    }

    private var pressedKeys = 0
    override fun render(delta: Float) {
        if (Server.shouldPlayDeathSound) {
            deathSound.play()
            Server.shouldPlayDeathSound = false
        }

        shouldPlayReload = Server.shouldPlayReload
        if (Server.getPlayer() != null) {
            player = Server.getPlayer()!!
            playerOnScoreboardTable = Server.playerOnScoreboardTable
        }

        Gdx.gl.glClearColor(45f / 255f, 40f / 255f, 50f / 255f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        if (::player.isInitialized) {
            updateServerRotation()
            updateServerMoves()
            updateServerMouse()
            getMousePosInGameWorld()
            setPlayerRotation()

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
                ground.forEach { sprite -> if (inFrustum(camera, sprite)) sprite.draw(it) }
                drawBloodOnTheFloor(it)
                drawPickups(it)
                drawProjectiles(it)
                drawOpponents(it)
                // drawCursor(it)
                moveOpponents(delta)
                drawPlayer(it, player)
                checkPlayerGotShot(it)
                checkOpponentsGotShot(it)
                removeUnnecessaryBloodOnTheFloor()
                if (shouldPlayReload) {
                    reloadSoundEffect.play()
                    shouldPlayReload = false
                    Server.shouldPlayReload = false
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
                drawMagazineInfo(it)
                checkAllPlayersOnMap(it)
                scoreboard(it)
                drawScoresOnBoard(it)
            }
        }
    }

    private fun drawScoresOnBoard(batch: SpriteBatch) {
        if (Gdx.input.isKeyPressed(Input.Keys.TAB)) {
            var t = 1.14f
            var sortedByKills = ArrayList<Agent>()
            playerOnScoreboardTable.values.forEach {
                sortedByKills.add(it)
            }

            sortedByKills = ArrayList(sortedByKills.sortedWith(compareBy { it.kills }).reversed())

            for (it in sortedByKills) {
                if (it.id == player.id) playersOnScoreboardFont.color = Color.GREEN else playersOnScoreboardFont.color = Color.RED
                t += 0.05f
                playersOnScoreboardFont.draw(batch, "${sortedByKills.indexOf(it) + 1}", WINDOW_WIDTH / 3.4f, WINDOW_HEIGHT / t)
                playersOnScoreboardFont.draw(batch, it.name, WINDOW_WIDTH / 2.4f, WINDOW_HEIGHT / t)
                playersOnScoreboardFont.draw(batch, "${it.kills}", WINDOW_WIDTH / 1.73f, WINDOW_HEIGHT / t)
                playersOnScoreboardFont.draw(batch, "${it.deaths}", WINDOW_WIDTH / 1.43f, WINDOW_HEIGHT / t)
            }

        }
    }

    private fun scoreboard(batch: SpriteBatch) {
        if (Gdx.input.isKeyPressed(Input.Keys.TAB)) {
            val scoreboard = assets.get("scoreboard/scoreboardBackground.png", Texture::class.java)
            val c = batch.color
            batch.setColor(c.r, c.g, c.b, .3f)
            batch.draw(scoreboard, 0f, 0f, WINDOW_WIDTH, WINDOW_HEIGHT)

            batch.setColor(c.r, c.g, c.b, .6f)
            batch.draw(scoreboard, WINDOW_WIDTH / 3.8f, WINDOW_HEIGHT / 20f, WINDOW_WIDTH / 2, WINDOW_HEIGHT / 1.1f)

            val table = assets.get("scoreboard/scoreboardTable.png", Texture::class.java)
            batch.setColor(c.r, c.g, c.b, .8f)
            batch.draw(table, WINDOW_WIDTH / 3.8f, WINDOW_HEIGHT / 14f, WINDOW_WIDTH / 2, WINDOW_HEIGHT / 1.15f)

            scoreboardFont.draw(batch, "RANK           PLAYER              KILLS            DEATHS", WINDOW_WIDTH / 3.4f, WINDOW_HEIGHT / 1.09f)
            scoreboardFont.data.setScale(1.7f)
        }
    }

    private fun removeUnnecessaryBloodOnTheFloor() {
        val iterator = bloodOnTheFloor.iterator()
        while (iterator.hasNext()) {
            val value = iterator.next()
            if (value.transparent < 0) {
                bloodOnTheFloorPool.free(value)
                iterator.remove()
            }
        }
    }

    private fun drawBloodOnTheFloor(batch: Batch) {
        this.bloodOnTheFloor.forEach {
            if (it.gotShot && it.transparent >= 0f) {
                it.bloodOnTheFloorSprite.draw(batch, it.transparent)
                it.changeTransparent()
            }
        }
    }

    private fun checkOpponentsGotShot(batch: Batch) {
        opponents.values.forEach {
            if (it.gotShot && !it.isDead) {
                drawBloodOnPlayerBody(batch, it.bounds.x - 10f, it.bounds.y)
                bloodOnTheFloor.add(
                        bloodOnTheFloorPool.obtain().apply {
                            bloodOnTheFloorSprite.setPosition(it.bounds.x - 20f, it.bounds.y - 50f)
                            gotShot = true
                            transparent = 1f
                        }
                )
            }
        }
    }

    private fun checkPlayerGotShot(batch: Batch) {
        if (player.gotShot && !player.isDead) {
            drawBloodOnPlayerBody(batch, player.bounds.x - 10f, player.bounds.y)
            bloodOnTheFloor.add(
                    bloodOnTheFloorPool.obtain().apply {
                        bloodOnTheFloorSprite.setPosition(player.bounds.x - 20f, player.bounds.y - 50f)
                        gotShot = true
                        transparent = 1f
                    }
            )
        }
    }

    private fun drawBloodOnPlayerBody(batch: Batch, x: Float, y: Float) {
        val blood = assets.get("images/blood-animation.png", Texture::class.java)
        batch.draw(blood, x, y, 65f, 65f)
    }


    private fun checkAllPlayersOnMap(batch: Batch) {
        if (!player.isDead) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.M)) showMiniMap++
            if (showMiniMap == 2) showMiniMap = 0
            if (showMiniMap != 1) {
                font.draw(batch, "Press \"M\" to show map", WINDOW_WIDTH - 160f, 15f)
            }

            if (showMiniMap == 1) {
                imgpos += (imgposdir / 3)
                if (imgpos < 0.0) imgposdir = -imgposdir
                if (imgpos > 1.0) imgposdir = -imgposdir
                val miniMapSize = 200f
                val playerSize = 6f
                val playerPosPercentageX = (player.bounds.x / MAP_WIDTH.toFloat()) * miniMapSize
                val playerPosPercentageY = (player.bounds.y / MAP_HEIGHT.toFloat()) * miniMapSize

                font.draw(batch, "Press \"M\" to hide map", WINDOW_WIDTH - 160f, 15f)

                val miniMapexture = assets.get("images/miniMap.png", Texture::class.java)
                val c = batch.color
                batch.setColor(c.r, c.g, c.b, .5f)
                batch.draw(miniMapexture, 0f, 0f, miniMapSize, miniMapSize)
                val meInMiniMapexture = assets.get("images/meInMiniMap.png", Texture::class.java)
                batch.setColor(c.r, c.g, c.b, 1f)

                batch.draw(meInMiniMapexture,
                        playerPosPercentageX - playerSize / 2f,
                        playerPosPercentageY - playerSize / 2f,
                        playerSize,
                        playerSize)


                val playersInMiniMapexture = assets.get("images/opponentsInMiniMap.png", Texture::class.java)
                batch.setColor(c.r, c.g, c.b, imgpos.toFloat())
                opponents.values.forEach {
                    if (!it.isDead)
                        batch.draw(playersInMiniMapexture,
                                ((it.bounds.x / MAP_WIDTH.toFloat()) * miniMapSize) - playerSize / 2f,
                                ((it.bounds.y / MAP_HEIGHT.toFloat()) * miniMapSize) - playerSize / 2f,
                                playerSize,
                                playerSize)
                }
            }
        }
    }

    private fun checkRestart() {
        if (player.isDead) {
            if (Gdx.input.isButtonPressed((Input.Buttons.LEFT))) {
                Server.restart()
            }
        }
    }

    private fun drawGameOver(batch: Batch) {
        if (player.isDead) {
            if (shouldDeathSoundPlay) {
                deathSound.play()
                shouldDeathSoundPlay = false
            }
            val gameOverTexture = assets.get("images/gameOver.png", Texture::class.java)
            val c: Color = batch.color
            batch.setColor(c.r, c.g, c.b, .7f)
            batch.draw(gameOverTexture, 0f, 0f, WINDOW_WIDTH, WINDOW_HEIGHT)
        } else {
            shouldDeathSoundPlay = true
        }
    }

    private fun updateServerRotation() {
        if (forIf) {
            forIf = false
            val b = true
            val thread = Thread {
                while (b) {
                    Server.playerRotation("degrees", player.facingDirectionAngle)
                    Thread.sleep(100)
                }
            }
            thread.start()
        }
    }

    private fun updateServerMouse() {
        val isMouseWPressed = Gdx.input.isButtonPressed((Input.Buttons.LEFT))
        val wWasReleased = mouseWasPressed && !isMouseWPressed
        mouseWasPressed = isMouseWPressed

        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT) && player.weapon.bulletsInChamber < 1
                && player.weapon.canShoot()) {
            dryfire.play()
            player.weapon.shoot()
        }

        if (Gdx.input.isButtonJustPressed((Input.Buttons.LEFT))) {
            Server.mouseStart()
        }
        if (wWasReleased) {
            Server.mouseStop()
        }
    }

    private fun updateServerMoves() {
        val isWPressed = Gdx.input.isKeyPressed(Input.Keys.W)
        val isAPressed = Gdx.input.isKeyPressed(Input.Keys.A)
        val isSPressed = Gdx.input.isKeyPressed(Input.Keys.S)
        val isDPressed = Gdx.input.isKeyPressed(Input.Keys.D)
        val isRPressed = Gdx.input.isKeyPressed(Input.Keys.R)

        val wWasReleased = wWasPressed && !isWPressed
        val aWasReleased = aWasPressed && !isAPressed
        val sWasReleased = sWasPressed && !isSPressed
        val dWasReleased = dWasPressed && !isDPressed
        val rWasReleased = rWasPressed && !isRPressed

        wWasPressed = isWPressed
        aWasPressed = isAPressed
        sWasPressed = isSPressed
        dWasPressed = isDPressed
        rWasPressed = isRPressed

        checkKeyJustPressed(Input.Keys.W, "W")
        checkKeyJustReleased(wWasReleased, "W")

        checkKeyJustPressed(Input.Keys.A, "A")
        checkKeyJustReleased(aWasReleased, "A")

        checkKeyJustPressed(Input.Keys.S, "S")
        checkKeyJustReleased(sWasReleased, "S")

        checkKeyJustPressed(Input.Keys.D, "D")
        checkKeyJustReleased(dWasReleased, "D")

        checkKeyJustPressed(Input.Keys.R, "R")
        checkKeyJustReleased(rWasReleased, "R")

        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) Server.pickWeapon()
    }

    private fun checkKeyJustPressed(keyNumber: Int, keyLetter: String) {
        if (Gdx.input.isKeyJustPressed(keyNumber)) {
            Server.startKey(keyLetter, true)
        }
    }

    private fun checkKeyJustReleased(keyJustPressed: Boolean, key: String) {
        if (keyJustPressed) {
            Server.stopKey(key, true)
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
        for (entry in opponents.entries) {

            if (agentOutSideViewport(entry.value)) opponents.remove(entry.key)

            val oldX = entry.value.bounds.x
            val oldY = entry.value.bounds.y
            entry.value.isMoving = entry.value.velocity.x != 0.0f || entry.value.velocity.y != 0.0f
            entry.value.setPosition(
                    entry.value.bounds.x + entry.value.velocity.x * delta,
                    entry.value.bounds.y + entry.value.velocity.y * delta)
            val zones = getZonesForRectangle(player.bounds)

            var collided = false
            for (i in 0 until zones.size) {
                for (j in 0 until wallMatrix[zones[i]]!!.size) {
                    if (Intersector.overlaps(wallMatrix[zones[i]]!![j].bounds, player.bounds)) {
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
            for (j in 0 until wallMatrix[zones[i]]!!.size) {
                if (Intersector.overlaps(wallMatrix[zones[i]]!![j].bounds, player.bounds)) {
                    player.setPosition(oldX, oldY)
                    collided = true
                    break
                }
                if (collided) break
            }
        }
        for (barrel in explosiveBarrels) {
            if (Intersector.overlaps(barrel.value.bounds, player.bounds)) {
                player.setPosition(oldX, oldY)
//                collided = true
                break
            }
        }
    }


    private fun calculateProjectilePositions(delta: Float) {
        var removed = false
        for (entry in projectiles.entries) {
            removed = false
            if (entry.value.justFired) {
                entry.value.justFired = false
                when {
                    entry.value is ShotgunProjectile -> shotgunShotSoundEffect.play(0.2f)
                    entry.value is MachineGunProjectile -> machineGunShotSoundEffect.play()
                    entry.value is BazookaProjectile -> bazookaShotSoundEffect.play()
                    else -> pistolShotSoundEffect.play(1.5f)
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
        for (opponent in opponents.entries) {
            if (Intersector.overlaps(projectile.bounds, opponent.value.bounds) && !opponent.value.isDead &&
                    projectile.agentId != opponent.value.id) {
                removeProjectile(projectile, key)
                if (!opponent.value.isDead) damageSound.play()
                return true
            }
        }
        return false
    }

    private fun checkPlayerCollision(projectile: Projectile, key: String): Boolean {
        if (Intersector.overlaps(projectile.bounds, player.bounds) && projectile.agentId != player.id) {
            removeProjectile(projectile, key)
            if (!player.isDead) damageSound.play()
            return true
        }
        return false
    }

    private fun checkBarrelCollisions(projectile: Projectile, key: String): Boolean {
        for (barrel in explosiveBarrels) {
            if (Intersector.overlaps(projectile.bounds, barrel.value.bounds)) {
                removeProjectile(projectile, key)
                return true
            }
        }
        return false
    }

    private fun checkWallsCollisions(projectile: Projectile, key: String): Boolean {
        for (zone in getZonesForCircle(projectile.bounds)) {
            if (wallMatrix[zone] != null) for (i in 0 until wallMatrix[zone]!!.size) {
                if (Intersector.overlaps(projectile.bounds, wallMatrix[zone]!![i].bounds)) {
                    removeProjectile(projectile, key)
                    return true
                }
            }
        }
        return false
    }

    private fun removeProjectile(projectile: Projectile, key: String) {
        when (projectile) {
            is PistolProjectile -> pistolProjectilePool.free(projectile)
            is MachineGunProjectile -> machineGunProjectilePool.free(projectile)
            is ShotgunProjectile -> shotgunProjectilePool.free(projectile)
            is BazookaProjectile -> bazookaProjectilePool.free(projectile)
        }
        projectiles.remove(key)
    }

    private fun drawPlayer(batch: Batch, agent: Agent) {
        if (!player.isDead && player.currentHealth >= 10) {
            setPlayerRotation()
            agent.sprite.draw(batch)
            agent.healthBarSprite.draw(batch)
            font.draw(batch, player.name, player.bounds.x + 10f, player.bounds.y + 88f)
        }
    }

    private fun drawProjectiles(batch: Batch) = projectiles.values.forEach {
        it.sprite.draw(batch)
    }

    private fun drawExplosions(batch: Batch) {
        bazookaExplosions.iterate { explosion, iterator ->
            if (explosion.justSpawned) {
                lastExplosion = TimeUtils.millis()
                bazookaExplosionSoundEffect.play()
                explosion.justSpawned = false
            }
            explosion.animate()
            if (explosion.isFinished()) {
                bazookaExplosionPool.free(explosion)
                iterator.remove()
            }
            explosion.sprite.draw(batch)
        }
        barrelExplosions.iterate { explosion, iterator ->
            if (explosion.justSpawned) {
                lastExplosion = TimeUtils.millis()
                bazookaExplosionSoundEffect.play()
                explosion.justSpawned = false
            }
            explosion.animate()
            if (explosion.isFinished()) {
                barrelExplosionPool.free(explosion)
                iterator.remove()
            }
            explosion.sprite.draw(batch)
        }
    }

    private fun drawOpponents(batch: Batch) {
        opponents.values.forEach {
            if (!it.isDead) {
                it.healthBarSprite.draw(batch)
                it.sprite.draw(batch)
                font.draw(batch, it.name, it.bounds.x + 10f, it.bounds.y + 88f)
            }
        }
    }

    private fun drawWalls(batch: Batch) {
        for (i in 0 until walls.size) if (inFrustum(camera, walls[i])) walls[i].draw(batch)
    }

    private fun drawExplosiveBarrels(batch: Batch) {
        for (barrel in explosiveBarrels.values) barrel.draw(batch)
    }

    private fun drawPickups(batch: Batch) {
        for (pickup in pickups.values) pickup.sprite.draw(batch)
    }

    private fun drawMagazineInfo(batch: Batch) {
        if (player.weapon.bulletsInChamber != -1 && !player.isDead) {
            font.draw(batch, "${player.weapon.type}, Ammo: ${player.weapon.bulletsInChamber}/${player.weapon.maxBulletsInChamber}",
                    WINDOW_WIDTH - 150f,
                    WINDOW_HEIGHT - 55f)
            font.data.setScale(1f, 1f)
        } else {
            if (!player.isDead)
                font.draw(batch, "Reloading...",
                        WINDOW_WIDTH - 150f,
                        WINDOW_HEIGHT - 55f)
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
        music.isLooping = true
        music.volume = 0.4f
        music.play()
    }
}