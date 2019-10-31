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
import io.socket.client.IO
import io.socket.client.Socket
import ktx.app.KtxScreen
import ktx.assets.pool
import ktx.collections.iterate
import ktx.graphics.use
import org.json.JSONArray
import org.json.JSONObject
import kotlin.collections.HashMap

class GameScreen(
        val game: Game,
        val batch: SpriteBatch,
        val font: BitmapFont,
        val camera: OrthographicCamera) : KtxScreen {

    val playerTexture = Texture(Gdx.files.internal("images/player_placeholder.png"))

    private lateinit var socket: Socket
    private val opponents = HashMap<String,Player>()
    private var timer: Float = 0.0f
    private var wWasPressed = false
    private var aWasPressed = false
    private var dWasPressed = false
    private var sWasPressed = false
    private var mouseWasPressed = false

    lateinit var player: Player
    val mousePosition = Vector2()

    val pistolProjectilePool = pool { PistolProjectile() }

    val walls = Array<Wall>()
    val projectiles = Array<Projectile>()
    //val opponents = HashMap<String, Opponent>()

    init {
        generateWalls();
    }

    private var pressedKeys = 0

    override fun render(delta: Float) {
        updateServer(delta)
        camera.update()
        if (::player.isInitialized){
            updateServerMoves()
            updateServerMouse()
            getMousePosInGameWorld()
            setPlayerRotation()
            calculatePistolProjectilesPosition(delta)
            checkControls(delta)
            setCameraPosition()
            camera.position.set(player.bounds.x, player.bounds.y, 0f)
            camera.update()
        }

        batch.projectionMatrix = camera.combined

        if (::player.isInitialized) {
            batch.use {
                drawProjectiles(it)
                //drawOpponents(it)
                drawPlayer(it, player)
                drawWalls(it)
                for (value in opponents.values) {
                    drawPlayer(it, value)
                }
            }
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
        if (wWasReleased){
            val data = JSONObject()
            data.put("Mouse", true)
            socket.emit("mouseStop", data)
        }
    }

    private fun updateServerMoves() {
        val isWPressed = Gdx.input.isKeyPressed(Input.Keys.W);
        val isAPressed = Gdx.input.isKeyPressed(Input.Keys.A);
        val isSPressed = Gdx.input.isKeyPressed(Input.Keys.S);
        val isDPressed = Gdx.input.isKeyPressed(Input.Keys.D);

        val wWasReleased = wWasPressed && !isWPressed;
        val aWasReleased = aWasPressed && !isAPressed;
        val sWasReleased = sWasPressed && !isSPressed;
        val dWasReleased = dWasPressed && !isDPressed;

        wWasPressed = isWPressed;
        aWasPressed = isAPressed;
        sWasPressed = isSPressed;
        dWasPressed = isDPressed;

        checkKeyJustPressed(Input.Keys.W, "W")
        checkKeyJustReleased(wWasReleased, "W")

        checkKeyJustPressed(Input.Keys.A, "A")
        checkKeyJustReleased(aWasReleased, "A")

        checkKeyJustPressed(Input.Keys.S, "S")
        checkKeyJustReleased(sWasReleased, "S")

        checkKeyJustPressed(Input.Keys.D, "D")
        checkKeyJustReleased(dWasReleased, "D")
    }

    private fun checkKeyJustPressed(keyNumber: Int, keyLetter: String) {
        if (Gdx.input.isKeyJustPressed(keyNumber)){
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

    private fun updateServer(dt: Float){
        timer += dt
        if (timer >= UPDATE_TIME && ::player.isInitialized && hasMoved()){
            val data = JSONObject()
            data.put("x", player.sprite.x)
            data.put("y", player.sprite.y)
            socket.emit("playerMoved", data)
        }
    }

    fun configSocketEvents() {
        socket.on(Socket.EVENT_CONNECT) {
            Gdx.app.log("SocketIO", "Connected")
            player = Player(WINDOW_WIDTH / 2 - PLAYER_SPRITE_WIDTH / 2,
                    WINDOW_HEIGHT / 2 - PLAYER_SPRITE_HEIGHT / 2, playerTexture)
        }
        .on("socketID") { data ->
            val obj: JSONObject = data[0] as JSONObject
            val playerId = obj.getString("id")

            Gdx.app.log("SocketIO", "My ID: $playerId")
        }
        .on("newPlayer") { data ->
            val obj: JSONObject = data[0] as JSONObject
            val playerId = obj.getString("id")
            Gdx.app.log("SocketIO", "New player has just connected with ID: $playerId")
            opponents[playerId] = Player(WINDOW_WIDTH / 2 - PLAYER_SPRITE_WIDTH / 2,
                    WINDOW_HEIGHT / 2 - PLAYER_SPRITE_HEIGHT / 2, playerTexture)
        }
        .on("playerDisconnected") { data ->
            val obj: JSONObject = data[0] as JSONObject
            val playerId = obj.getString("id")
            opponents.remove(playerId)
        }
        .on("getPlayers") { data ->
            val obj: JSONArray = data[0] as JSONArray
            Gdx.app.log("Other players: ", "${data[0]}")
            for (i in 0 until obj.length()) {
                val newPlayer = Player(WINDOW_WIDTH / 2 - PLAYER_SPRITE_WIDTH / 2,
                        WINDOW_HEIGHT / 2 - PLAYER_SPRITE_HEIGHT / 2, playerTexture)
                val vector = Vector2()
                vector.x = (obj.getJSONObject(i).getDouble("x").toFloat())
                vector.y = (obj.getJSONObject(i).getDouble("y").toFloat())
                newPlayer.setPosition(vector.x, vector.y)

                val playerId = obj.getJSONObject(i).getString("id")
                opponents[playerId] = newPlayer
            }
        }
        .on("playerMoved") { data ->
            val obj: JSONObject = data[0] as JSONObject
            val playerId = obj.getString("id")
            val x = obj.getDouble("x")
            val y = obj.getDouble("y")

            if(opponents[playerId] != null)
                opponents[playerId]!!.setPosition(x.toFloat(), y.toFloat())
        }
    }

    private fun hasMoved(): Boolean {
        return (Gdx.input.isKeyPressed(Input.Keys.W)
                || Gdx.input.isKeyPressed(Input.Keys.S)
                || Gdx.input.isKeyPressed(Input.Keys.A)
                || Gdx.input.isKeyPressed(Input.Keys.D))
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