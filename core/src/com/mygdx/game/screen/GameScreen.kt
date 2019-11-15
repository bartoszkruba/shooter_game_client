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
import com.mygdx.game.Game
import com.mygdx.game.model.*
import com.mygdx.game.settings.*
import com.mygdx.game.util.generateWallMatrix
import com.mygdx.game.util.getZonesForCircle
import com.mygdx.game.util.getZonesForRectangle
import ktx.app.KtxScreen
import ktx.graphics.use
import com.mygdx.game.util.inFrustum
import frontendServer.Server
import java.util.concurrent.ConcurrentHashMap
import com.mygdx.game.model.Pickup
import com.mygdx.game.model.Projectile
import com.mygdx.game.model.Opponent
import ktx.assets.pool

class GameScreen(
        val game: Game,
        private val batch: SpriteBatch,
        private val assets: AssetManager,
        private val camera: OrthographicCamera,
        private val font: BitmapFont) : KtxScreen {

    private val playerAtlas = assets.get<TextureAtlas>("images/player/player.atlas")
    private val projectileTexture = assets.get("images/projectile.png", Texture::class.java)
    private val wallTexture = assets.get("images/brickwall2.jpg", Texture::class.java)
    private val healthBarTexture = assets.get("images/healthBar3.png", Texture::class.java)
    private val pistolTexture = assets.get("images/pistol.png", Texture::class.java)
    private val machineGunTexture = assets.get("images/machine_gun.png", Texture::class.java)
    private val music = assets.get("music/ingame_music.ogg", Music::class.java)
    private val deathSound = assets.get("sounds/deathSound.wav", Sound::class.java)
    private val pistolShotSoundEffect = assets.get("sounds/pistol_shot.wav", Sound::class.java)
    private val reloadSoundEffect = assets.get("sounds/reload_sound.mp3", Sound::class.java)
    private val groundTexture = assets.get("images/ground.jpg", Texture::class.java)
    private val cursor = Pixmap(Gdx.files.internal("images/crosshair.png"))
    private val bloodOnTheFloorTexture = assets.get("images/blood-onTheFloor.png", Texture::class.java)
    private var shouldPlayReload = false
    private var opponents = ConcurrentHashMap<String, Opponent>()
    private var wWasPressed = false
    private var aWasPressed = false
    private var dWasPressed = false
    private var sWasPressed = false
    private var rWasPressed = false
    private var mouseWasPressed = false
    private var forIf = true

    val playerTextures: Array<Texture> = Array<Texture>()
    val mousePosition = Vector2()
    val walls = Array<Wall>()
    val wallMatrix: HashMap<String, Array<Wall>>

    var projectiles = ConcurrentHashMap<String, Projectile>()

    lateinit var pistolProjectilePool: Pool<PistolProjectile>
    lateinit var machineGunProjectilePool: Pool<MachineGunProjectile>

    var pickups = ConcurrentHashMap<String, Pickup>()
    var imgpos = 0.0
    var imgposdir = 0.1
    var showMiniMap = 0

    private val ground = Array<Sprite>()
    lateinit var player: Player

    var bloodOnTheFloor = ArrayList<Blood>()
    private val bloodOnTheFloorPool = pool { Blood(bloodOnTheFloorTexture) }
    var shouldDeathSoundPlay = false


    init {
        Gdx.graphics.setCursor(Gdx.graphics.newCursor(cursor, 16, 16));

        playerTextures.add(assets.get("images/player/up.png", Texture::class.java))
        playerTextures.add(assets.get("images/player/down.png", Texture::class.java))
        playerTextures.add(assets.get("images/player/left.png", Texture::class.java))
        playerTextures.add(assets.get("images/player/right.png", Texture::class.java))
        wallMatrix = generateWallMatrix()
        generateWalls()
        music.isLooping = true
        music.volume = 0.2f
        //music.play()

        for (i in 0 until (MAP_HEIGHT % GROUND_TEXTURE_HEIGHT + 1).toInt()) {
            for (j in 0 until (MAP_WIDTH % GROUND_TEXTURE_WIDTH + 1).toInt()) {
                val groundSprite = Sprite(groundTexture)
                groundSprite.setPosition(i * GROUND_TEXTURE_WIDTH, j * GROUND_TEXTURE_HEIGHT)
                groundSprite.setSize(GROUND_TEXTURE_WIDTH, GROUND_TEXTURE_HEIGHT)
                ground.add(groundSprite)
            }
        }
        Server.connectionSocket()
        Server.configSocketEvents(projectileTexture, pistolTexture, machineGunTexture, playerAtlas, healthBarTexture,
                wallMatrix, wallTexture, walls)
    }

    private var pressedKeys = 0
    override fun render(delta: Float) {
        if (Server.getPlayer() != null) {
            player = Server.getPlayer()!!
        }
        projectiles = Server.projectiles
        opponents = Server.opponents
        pistolProjectilePool = Server.pistolProjectilePool
        shouldPlayReload = Server.shouldPlayReload
        machineGunProjectilePool = Server.machineGunProjectilePool
        pickups = Server.pickups

        Gdx.gl.glClearColor(45f / 255f, 40f / 255f, 50f / 255f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (::player.isInitialized) {
            updateServerRotation()
            updateServerMoves()
            updateServerMouse()
            getMousePosInGameWorld()
            setPlayerRotation()

            calculateProjectilePositions(delta)
            checkControls(delta)
            setCameraPosition()
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
                drawWalls(it)
                scoreboard(it)
            }
        }

        val uiMatrix = camera.combined.cpy();
        uiMatrix.setToOrtho2D(0f, 0f, WINDOW_WIDTH, WINDOW_HEIGHT)

        batch.projectionMatrix = uiMatrix

        if (::player.isInitialized) {
            batch.use {
                drawGameOver(it)
                drawMagazineInfo(it)
                checkAllPlayersOnMap(it)
            }
        }
    }

    private fun scoreboard(batch: SpriteBatch) {
        if (Gdx.input.isKeyPressed(Input.Keys.TAB)){
            val scoreboard = assets.get("scoreboard/scoreboardBackground.png", Texture::class.java)
            val c = batch.color;
            batch.setColor(c.r, c.g, c.b, .3f)
            batch.draw(scoreboard, 0f, 0f, WINDOW_WIDTH, WINDOW_HEIGHT + 500f);

            batch.setColor(c.r, c.g, c.b, .6f)
            batch.draw(scoreboard, WINDOW_WIDTH / 3.8f, WINDOW_HEIGHT / 4.3f , WINDOW_WIDTH / 2, WINDOW_HEIGHT / 1.2f);
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
            if (it.gotShot) {
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
        if (player.gotShot) {
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
        batch.draw(blood, x, y, 65f, 65f);
    }


    private fun checkAllPlayersOnMap(batch: Batch) {
        if (!player.isDead) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.M)) showMiniMap++
            if (showMiniMap == 2) showMiniMap = 0
            if (showMiniMap != 1) {
                font.draw(batch, "Press \"M\" to show map", WINDOW_WIDTH - 160f, 15f);
            }

            if (showMiniMap == 1) {
                imgpos += (imgposdir / 3);
                if (imgpos < 0.0) imgposdir = -imgposdir;
                if (imgpos > 1.0) imgposdir = -imgposdir;
                val miniMapSize = 200f;
                val playerSize = 6f;
                val playerPosPercentageX = (player.bounds.x / MAP_WIDTH.toFloat()) * miniMapSize;
                val playerPosPercentageY = (player.bounds.y / MAP_HEIGHT.toFloat()) * miniMapSize;

                font.draw(batch, "Press \"M\" to hide map", WINDOW_WIDTH - 160f, 15f);

                val miniMapexture = assets.get("images/miniMap.png", Texture::class.java)
                val c = batch.color;
                batch.setColor(c.r, c.g, c.b, .5f)
                batch.draw(miniMapexture, 0f, 0f, miniMapSize, miniMapSize);
                val meInMiniMapexture = assets.get("images/meInMiniMap.png", Texture::class.java)
                batch.setColor(c.r, c.g, c.b, 1f)

                batch.draw(meInMiniMapexture,
                        playerPosPercentageX - playerSize / 2f,
                        playerPosPercentageY - playerSize / 2f,
                        playerSize,
                        playerSize);


                val playersInMiniMapexture = assets.get("images/opponentsInMiniMap.png", Texture::class.java)
                batch.setColor(c.r, c.g, c.b, imgpos.toFloat())
                opponents.values.forEach {
                    batch.draw(playersInMiniMapexture,
                            ((it.bounds.x / MAP_WIDTH.toFloat()) * miniMapSize) - playerSize / 2f,
                            ((it.bounds.y / MAP_HEIGHT.toFloat()) * miniMapSize) - playerSize / 2f,
                            playerSize,
                            playerSize);
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
            val c: Color = batch.color;
            batch.setColor(c.r, c.g, c.b, .7f)
            batch.draw(gameOverTexture, 0f, 0f, WINDOW_WIDTH, WINDOW_HEIGHT);
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
        val isMouseWPressed = Gdx.input.isButtonPressed((Input.Buttons.LEFT));
        val wWasReleased = mouseWasPressed && !isMouseWPressed;
        mouseWasPressed = isMouseWPressed;

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
        for (opponent in opponents.values) {
            val oldX = opponent.bounds.x
            val oldY = opponent.bounds.y
            opponent.isMoving = opponent.velocity.x != 0.0f || opponent.velocity.y != 0.0f
            opponent.setPosition(
                    opponent.bounds.x + opponent.velocity.x * delta,
                    opponent.bounds.y + opponent.velocity.y * delta)
            val zones = getZonesForRectangle(player.bounds)

            var collided = false
            for (i in 0 until zones.size) {
                for (j in 0 until wallMatrix[zones[i]]!!.size) {
                    if (Intersector.overlaps(wallMatrix[zones[i]]!![j].bounds, player.bounds)) {
                        opponent.setPosition(oldX, oldY)
                        collided = true
                        break
                    }
                    if (collided) break
                }
            }
        }
    }

    private fun movePlayer(x: Float, y: Float, oldX: Float, oldY: Float) {
        player.setPosition(
                MathUtils.clamp(x, WALL_SPRITE_WIDTH, MAP_WIDTH - WALL_SPRITE_WIDTH - PLAYER_SPRITE_WIDTH),
                MathUtils.clamp(y, WALL_SPRITE_HEIGHT, MAP_HEIGHT - WALL_SPRITE_HEIGHT - PLAYER_SPRITE_HEIGHT))
        val zones = getZonesForRectangle(player.bounds)
        var collided = false
        for (i in 0 until zones.size) {
            for (j in 0 until wallMatrix[zones[i]]!!.size) {
                if (Intersector.overlaps(wallMatrix[zones[i]]!![j].bounds, player.bounds)) {
                    player.setPosition(oldX, oldY)
                    collided = true
                    break
                }
                if (collided) break
            }
        }
    }


    private fun calculateProjectilePositions(delta: Float) {
        var removed = false
        for (entry in projectiles.entries) {
            removed = false
            if (entry.value.justFired) {
                entry.value.justFired = false
                pistolShotSoundEffect.play()
            }
            entry.value.setPosition(
                    entry.value.bounds.x + entry.value.velocity.x * delta * entry.value.speed,
                    entry.value.bounds.y + entry.value.velocity.y * delta * entry.value.speed)

            removed = checkIfOutsideMap(entry.value, entry.key)
            if (!removed) removed = checkIfOutsideViewport(entry.value, entry.key)
            if (!removed) removed = checkOpponentCollisions(entry.value, entry.key)
            if (!removed) removed = checkPlayerCollision(entry.value, entry.key)
            if (!removed) removed = checkWallsCollisions(entry.value, entry.key)
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
            if (Intersector.overlaps(projectile.bounds, opponent.value.bounds) && !opponent.value.isDead) {
                removeProjectile(projectile, key)
                return true
            }
        }
        return false
    }

    private fun checkPlayerCollision(projectile: Projectile, key: String): Boolean {
        if (Intersector.overlaps(projectile.bounds, player.bounds)) {
            removeProjectile(projectile, key)
            return true
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
        if (projectile is PistolProjectile)
            pistolProjectilePool.free(projectile)
        else if (projectile is MachineGunProjectile)
            machineGunProjectilePool.free(projectile)
        projectiles.remove(key)
    }

    private fun drawPlayer(batch: Batch, agent: Agent) {
        if (!player.isDead && player.currentHealth >= 10) {
            setPlayerRotation()
            agent.sprite.draw(batch)
            agent.healthBarSprite.draw(batch)
            font.draw(batch, player.name, player.bounds.x + 10f, player.bounds.y + 88f);
        }
    }

    private fun drawProjectiles(batch: Batch) = projectiles.values.forEach {
        it.sprite.draw(batch)
    }

    private fun drawOpponents(batch: Batch) {
        opponents.values.forEach {
            if (!it.isDead) {
                it.healthBarSprite.draw(batch);
                it.sprite.draw(batch)
                font.draw(batch, it.name, it.bounds.x + 10f, it.bounds.y + 88f);
            }
        }
    }

    private fun drawWalls(batch: Batch) {
        for (i in 0 until walls.size) if (inFrustum(camera, walls[i])) walls[i].draw(batch)
    }

    private fun drawPickups(batch: Batch) {
        for (pickup in pickups.values) pickup.sprite.draw(batch)
    }

    private fun drawMagazineInfo(batch: Batch) {
        if (player.weapon.bulletsInChamber != -1 && !player.isDead) {
            font.draw(batch, "${player.weapon.type}, Ammo: ${player.weapon.bulletsInChamber}/${player.weapon.maxBulletsInChamber}",
                    WINDOW_WIDTH - 150f,
                    WINDOW_HEIGHT - 55f)
            font.getData().setScale(1f, 1f);
        } else {
            if (!player.isDead)
                font.draw(batch, "Reloading...",
                        WINDOW_WIDTH - 150f,
                        WINDOW_HEIGHT - 55f)
            font.getData().setScale(1f, 1f);
        }
    }

    private fun generateWalls() {
        for (i in 0 until MAP_HEIGHT step WALL_SPRITE_HEIGHT.toInt()) {
            walls.add(Wall(0f, i.toFloat(), wallTexture))
            walls.add(Wall(MAP_WIDTH - WALL_SPRITE_WIDTH, i.toFloat(), wallTexture))
        }
        for (i in WALL_SPRITE_WIDTH.toInt() until MAP_WIDTH - WALL_SPRITE_WIDTH.toInt() step WALL_SPRITE_WIDTH.toInt()) {
            walls.add(Wall(i.toFloat(), 0f, wallTexture))
            walls.add(Wall(i.toFloat(), MAP_HEIGHT - WALL_SPRITE_HEIGHT, wallTexture))
        }
    }

    private fun setCameraPosition() {
        camera.position.x = MathUtils.clamp(player.bounds.x, WINDOW_WIDTH / 2f, MAP_WIDTH - WINDOW_WIDTH / 2f)
        camera.position.y = MathUtils.clamp(player.bounds.y, WINDOW_HEIGHT / 2f, MAP_HEIGHT - WINDOW_HEIGHT / 2f)
    }
}