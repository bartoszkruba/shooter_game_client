package com.mygdx.game.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.*
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.SnapshotArray
import com.mygdx.game.Game
import com.mygdx.game.model.*
import com.mygdx.game.settings.*
import io.socket.client.IO
import io.socket.client.Socket
import ktx.app.KtxScreen
import ktx.assets.pool
import ktx.graphics.use
import org.json.JSONObject
import kotlin.collections.HashMap
import com.mygdx.game.model.Opponent
import java.util.concurrent.ConcurrentHashMap

import kotlin.math.tan


class GameScreen(
        val game: Game,
        private val batch: SpriteBatch,
        private val assets: AssetManager,
        private val camera: OrthographicCamera,
        private val font: BitmapFont) : KtxScreen {

    private val playerTexture: Texture = assets.get("images/player.png", Texture::class.java)
    private val projectileTexture = assets.get("images/projectile.png", Texture::class.java)
    private val wallTexture = assets.get("images/brickwall2.jpg", Texture::class.java)
    private val healthBarTexture = assets.get("images/healthBar3.png", Texture::class.java)
    private val pistolTexture = assets.get("images/pistol.png", Texture::class.java)
    private val machineGunTexture = assets.get("images/machine_gun.png", Texture::class.java)
    private val music = assets.get("music/music.wav", Music::class.java)
    private val pistolShotSoundEffect = assets.get("sounds/pistol_shot.wav", Sound::class.java)
    private val reloadSoundEffect = assets.get("sounds/reload_sound.mp3", Sound::class.java)

    private var shouldPlayReload = false

    private lateinit var socket: Socket
    private val opponents = ConcurrentHashMap<String, Opponent>()
    private var timer: Float = 0.0f
    private var wWasPressed = false
    private var aWasPressed = false
    private var dWasPressed = false
    private var sWasPressed = false
    private var rWasPressed = false
    private var mouseWasPressed = false
    private var forIf = true

    lateinit var player: Player
    val playerTextures: Array<Texture> = Array<Texture>()
    val mousePosition = Vector2()
    val pistolProjectilePool = pool { PistolProjectile(texture = projectileTexture) }
    val machineGunProjectilePool = pool { MachineGunProjectile(texture = projectileTexture) }
    val walls = Array<Wall>()

    val projectiles = ConcurrentHashMap<String, Projectile>()

    val pistolPickupPool = pool { PistolPickup(texture = pistolTexture) }
    val machineGunPickupPool = pool { MachineGunPickup(texture = machineGunTexture) }

    val pickups = ConcurrentHashMap<String, Pickup>()

    init {
        playerTextures.add(assets.get("images/player/up.png", Texture::class.java))
        playerTextures.add(assets.get("images/player/down.png", Texture::class.java))
        playerTextures.add(assets.get("images/player/left.png", Texture::class.java))
        playerTextures.add(assets.get("images/player/right.png", Texture::class.java))
        generateWalls()
        music.isLooping = true
        music.volume = 0f
        music.play()
    }

    private var pressedKeys = 0

    override fun render(delta: Float) {

        Gdx.gl.glClearColor(45f / 255f, 40f / 255f, 50f / 255f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (::player.isInitialized) {
            updateServerRotation()
            updateServerMoves()
            updateServerMouse()
            getMousePosInGameWorld()
            setPlayerRotation()
            calculatePistolProjectilesPosition(delta)
            checkControls(delta)
            setCameraPosition()
            updateServerPlayerHealth()
            updateServerOpponentsHealth()
        }

        camera.update()
        batch.projectionMatrix = camera.combined

        if (::player.isInitialized) {
            batch.use {
                drawPickups(it)
                drawProjectiles(it)
                drawOpponents(it)
                moveOpponents(delta)
                drawPlayer(it, player)
                if (shouldPlayReload) {
                    reloadSoundEffect.play()
                    shouldPlayReload = false
                }
                drawWalls(it)
            }
        }

        val uiMatrix = camera.combined.cpy();
        uiMatrix.setToOrtho2D(0f, 0f, WINDOW_WIDTH, WINDOW_HEIGHT)

        batch.projectionMatrix = uiMatrix

        if (::player.isInitialized) {
            batch.use {
                drawMagazineInfo(it)
            }
        }
    }


    private fun updateServerOpponentsHealth() {
        if (opponents.values.isNotEmpty()) {
            val data = JSONObject()
            for (opponent in opponents.values) {
                data.put("health", opponent.currentHealth)
                data.put("id", opponent.id)
                socket.emit("currentPlayerHealth", data)
            }
        }
    }

    private fun updateServerPlayerHealth() {
        val data = JSONObject()
        data.put("health", player.currentHealth)
        data.put("id", player.id)
        socket.emit("currentPlayerHealth", data)
    }

    private fun updateServerRotation() {
        if (forIf) {
            forIf = false
            val b = true
            val thread = Thread {
                while (b) {
                    val data = JSONObject()
                    data.put("degrees", player.facingDirectionAngle)
                    socket.emit("playerRotation", data)
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
            val data = JSONObject()
            data.put("Mouse", true)
            socket.emit("mouseStart", data)
        }
        if (wWasReleased) {
            val data = JSONObject()
            data.put("Mouse", true)
            socket.emit("mouseStop", data)
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

        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) socket.emit("pickWeapon")
    }

    private fun checkKeyJustPressed(keyNumber: Int, keyLetter: String) {
        if (Gdx.input.isKeyJustPressed(keyNumber)) {
            val data = JSONObject()
            data.put(keyLetter, true)
            socket.emit("startKey", data)
        }
    }

    private fun checkKeyJustReleased(keyJustPressed: Boolean, key: String) {
        if (keyJustPressed) {
            val data = JSONObject()
            data.put(key, true)
            socket.emit("stopKey", data)
        }
    }

    fun configSocketEvents() {
        socket.on(Socket.EVENT_CONNECT) {
            Gdx.app.log("SocketIO", "Connected")
        }
                .on("socketID") { data ->
                    val obj: JSONObject = data[0] as JSONObject
                    val playerId = obj.getString("id")

                    player = Player(MAP_WIDTH / 2f, MAP_HEIGHT / 2f, false, PLAYER_MAX_HEALTH, playerTextures, healthBarTexture, playerId)

                    Gdx.app.log("SocketIO", "My ID: $playerId")
                }
                .on("playerDisconnected") { data ->
                    val obj: JSONObject = data[0] as JSONObject
                    val playerId = obj.getString("id")
                    opponents.remove(playerId)
                }
                .on("gameData") { data ->
                    val obj = data[0] as JSONObject
                    val agents = obj.getJSONArray("agentData")
                    for (i in 0 until agents.length()) {
                        val agent = agents[i] as JSONObject
                        val id = agent.getString("id")
                        val isDead = agent.getBoolean("isDead")
                        val currentHealth = agent.getLong("currentHealth").toFloat()
                        val x = agent.getLong("x").toFloat()
                        val y = agent.getLong("y").toFloat()
                        val weapon = agent.getString("weapon")
                        val xVelocity = agent.getLong("xVelocity").toFloat()
                        val yVelocity = agent.getLong("yVelocity").toFloat()
                        if (id == player.id) {
                            if (!isDead) {
                                player.setPosition(x, y)

                                if (player.weapon.type != weapon) {
                                    when (weapon) {
                                        ProjectileType.PISTOL -> player.weapon = Pistol()
                                        ProjectileType.MACHINE_GUN -> player.weapon = MachineGun()
                                    }
                                }
                                val bulletsLeft = agent.getInt("bulletsLeft")
                                if (bulletsLeft == -1 && player.weapon.bulletsInChamber != -1) shouldPlayReload = true
                                player.weapon.bulletsInChamber = bulletsLeft
                                player.setPosition(x, y)
                                player.currentHealth = currentHealth
                                player.setHealthBar(currentHealth, x, y)
                            } else player.isDead = true
                        } else {
                            if (opponents[id] == null) {
                                opponents[id] = Opponent(x, y, 0f, 0f, playerTextures, id, healthBarTexture)
                                opponents[id]?.velocity?.x = xVelocity
                                opponents[id]?.velocity?.y = yVelocity
                                opponents[id] = Opponent(x, y, isDead, currentHealth, 0f, 0f, playerTexture, id, healthBarTexture)
                            } else {
                                //println(currentHealth)
                                opponents[id]?.setPosition(x, y)
                                opponents[id]?.velocity?.x = xVelocity
                                opponents[id]?.velocity?.y = yVelocity
                                opponents[id]?.setHealthBar(currentHealth, x, y)
                                opponents[id]?.isDead = isDead
                                opponents[id]?.currentHealth = currentHealth
                                opponents[id]?.healthBarSprite!!.setSize(currentHealth, HEALTH_BAR_SPRITE_HEIGHT)
                            }
                        }
                    }

                    val proj = obj.getJSONArray("projectileData")

                    for (i in 0 until proj.length()) {
                        val projectile = proj[i] as JSONObject
                        val type = projectile.getString("type")
                        val id = projectile.getString("id")
                        val x = projectile.getLong("x").toFloat()
                        val y = projectile.getLong("y").toFloat()
                        val xSpeed = projectile.getDouble("xSpeed").toFloat()
                        val ySpeed = projectile.getDouble("ySpeed").toFloat()

                        if (projectiles[id] == null) {
                            projectiles[id] = when (type) {
                                ProjectileType.PISTOL -> pistolProjectilePool.obtain()
                                else -> machineGunProjectilePool.obtain()
                            }.apply {
                                setPosition(x, y)
                                velocity.x = xSpeed
                                velocity.y = ySpeed
                            }
                        } else {
                            projectiles[id]?.apply {
                                setPosition(x, y)
                                velocity.x = xSpeed
                                velocity.y = ySpeed
                            }
                        }
                    }

                    val picks = obj.getJSONArray("pickupData")

                    for (pickup in pickups.values) {
                        if (pickup is PistolPickup) pistolPickupPool.free(pickup)
                        if (pickup is MachineGunPickup) machineGunPickupPool.free(pickup)
                    }

                    pickups.clear()

                    for (i in 0 until picks.length()) {
                        val pickup = picks[i] as JSONObject
                        val id = pickup.getString("id")
                        val x = pickup.getDouble("x").toFloat()
                        val y = pickup.getDouble("y").toFloat()
                        val type = pickup.getString("type")

                        pickups[id] = when (type) {
                            ProjectileType.PISTOL -> pistolPickupPool.obtain().apply { setPosition(x, y) }
                            else -> machineGunPickupPool.obtain().apply { setPosition(x, y) }
                        }
                    }
                }
                .on("newProjectile") { data ->
                    val projectile = data[0] as JSONObject
                    val type = projectile.getString("type")
                    val id = projectile.getString("id")
                    val x = projectile.getLong("x").toFloat()
                    val y = projectile.getLong("y").toFloat()
                    val xSpeed = projectile.getDouble("xSpeed").toFloat()
                    val ySpeed = projectile.getDouble("ySpeed").toFloat()

                    if (projectiles[id] == null) {
                        projectiles[id] = when (type) {
                            ProjectileType.PISTOL -> pistolProjectilePool.obtain()
                            else -> machineGunProjectilePool.obtain()
                        }.apply {
                            setPosition(x, y)
                            velocity.x = xSpeed
                            velocity.y = ySpeed
                            justFired = true
                        }
                    } else {
                        projectiles[id]?.apply {
                            setPosition(x, y)
                            velocity.x = xSpeed
                            velocity.y = ySpeed
                            justFired = true
                        }
                    }
                }
    }

    fun connectionSocket() {
        try {
            socket = IO.socket("http://localhost:8080");
            socket.connect();
        } catch (e: Exception) {
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
        player.setAngle(angle)
    }

    private fun checkControls(delta: Float) {
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

    private fun moveOpponents(delta: Float) {
        for (opponent in opponents.values) {
            opponent.setPosition(
                    opponent.bounds.x + opponent.velocity.x * delta,
                    opponent.bounds.y + opponent.velocity.y * delta)
        }
    }

    private fun movePlayer(x: Float, y: Float) = player.setPosition(
            MathUtils.clamp(x, WALL_SPRITE_WIDTH, MAP_WIDTH - WALL_SPRITE_WIDTH - PLAYER_SPRITE_WIDTH),
            MathUtils.clamp(y, WALL_SPRITE_HEIGHT, MAP_HEIGHT - WALL_SPRITE_HEIGHT - PLAYER_SPRITE_HEIGHT))


    fun calculatePistolProjectilesPosition(delta: Float) {

        var removed = false

        for (entry in projectiles.entries) {
            removed = false;
            entry.value.setPosition(
                    entry.value.bounds.x + entry.value.velocity.x * delta * entry.value.speed,
                    entry.value.bounds.y + entry.value.velocity.y * delta * entry.value.speed)

            if (entry.value.bounds.x < 0 || entry.value.bounds.x > MAP_WIDTH ||
                    entry.value.bounds.y < 0 || entry.value.bounds.y > MAP_HEIGHT) {

                if (entry.value is PistolProjectile)
                    pistolProjectilePool.free(entry.value as PistolProjectile)
                else if (entry.value is MachineGunProjectile)
                    machineGunProjectilePool.free(entry.value as MachineGunProjectile)

                projectiles.remove(entry.key)
            } else {
                for (opponent in opponents.entries) {
                    if (Intersector.overlaps(entry.value.bounds, opponent.value.bounds)) {
                        if (entry.value is PistolProjectile)
                            pistolProjectilePool.free(entry.value as PistolProjectile)
                        else if (entry.value is MachineGunProjectile)
                            machineGunProjectilePool.free(entry.value as MachineGunProjectile)

                        projectiles.remove(entry.key)
                        removed = true
                    }
                }
                if (!removed) {
                    if (Intersector.overlaps(entry.value.bounds, player.bounds)) {
                        if (entry.value is PistolProjectile)
                            pistolProjectilePool.free(entry.value as PistolProjectile)
                        else if (entry.value is MachineGunProjectile)
                            machineGunProjectilePool.free(entry.value as MachineGunProjectile)
                        projectiles.remove(entry.key)
                    }
                }
            }
        }
    }

    private fun drawPlayer(batch: Batch, agent: Agent) {
        if (!player.isDead && player.currentHealth >= 10) {
            setPlayerRotation()
            agent.sprite.draw(batch)
            agent.healthBarSprite.draw(batch)
        } else {
            val data = JSONObject()
            data.put("isDead", true)
            data.put("id", player.id)
            socket.emit("isDead", data)
        }
    }

    private fun drawProjectiles(batch: Batch) = projectiles.values.forEach {
        it.sprite.draw(batch)
        if (it.justFired) {
            it.justFired = false
            pistolShotSoundEffect.play()
        }
    }

    private fun drawOpponents(batch: Batch) {
        opponents.values.forEach {
            //println("id: "+it.id + ", is " + it.isDead)
            if (!it.isDead) {
                //println("health: ${it.currentHealth}")
                it.healthBarSprite.draw(batch);
                it.sprite.draw(batch)
            } else {
                val data = JSONObject()
                data.put("isDead", true)
                data.put("id", it.id)
                socket.emit("isDead", data)
            }
        }
    }


    private fun drawWalls(batch: Batch) {
        for (i in 0 until walls.size) walls[i].draw(batch)
    }

    private fun drawPickups(batch: Batch) {
        for (pickup in pickups.values) pickup.sprite.draw(batch)
    }

    private fun drawMagazineInfo(batch: Batch) {
        if (player.weapon.bulletsInChamber != -1) {
            font.draw(batch, "${player.weapon.type}, Ammo: ${player.weapon.bulletsInChamber}/${player.weapon.maxBulletsInChamber}",
                    WINDOW_WIDTH - 150f,
                    WINDOW_HEIGHT - 55f)
        } else {
            font.draw(batch, "Reloading...",
                    WINDOW_WIDTH - 150f,
                    WINDOW_HEIGHT - 55f)
        }
    }

    fun generateWalls() {
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
        if (player.bounds.x > WINDOW_WIDTH / 2f && player.bounds.x < MAP_WIDTH - WINDOW_WIDTH / 2f)
            camera.position.x = player.bounds.x

        if (player.bounds.y > WINDOW_HEIGHT / 2f && player.bounds.y < MAP_HEIGHT - WINDOW_HEIGHT / 2f)
            camera.position.y = player.bounds.y
    }
}
