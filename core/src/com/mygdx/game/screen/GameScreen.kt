package com.mygdx.game.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
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
        private val camera: OrthographicCamera) : KtxScreen {

    private val playerTexture:Texture = assets.get("images/leprechaun.png", Texture::class.java)
    private val projectileTexture = assets.get("images/standard_projectile.jpg", Texture::class.java)
    private val wallTexture = assets.get("images/wall.png", Texture::class.java)

    private lateinit var socket: Socket
    private val opponents = HashMap<String, Opponent>()
    private var timer: Float = 0.0f
    private var wWasPressed = false
    private var aWasPressed = false
    private var dWasPressed = false
    private var sWasPressed = false
    private var mouseWasPressed = false
    private var forIf = true

    lateinit var player: Player
    val mousePosition = Vector2()
    val pistolProjectilePool = pool { PistolProjectile(texture = projectileTexture) }
    val walls = Array<Wall>()
    val socketProjectiles = ConcurrentHashMap<String, Projectile>()
    var clientProjectiles = HashMap<String, Projectile>()
    val ghostProjectiles = SnapshotArray<Projectile>()

    var projectilesUpdated = false

    init {
        generateWalls()
    }

    private var pressedKeys = 0

    override fun render(delta: Float) {
        if (projectilesUpdated) {
            for (projectile in ghostProjectiles) {
                pistolProjectilePool.free(projectile as PistolProjectile)
                ghostProjectiles.clear()
            }
            projectilesUpdated = false
            clientProjectiles = HashMap(socketProjectiles)
        }

        if (::player.isInitialized) {
            updateServerRotation()
            updateServerMoves()
            updateServerMouse()
            getMousePosInGameWorld()
            setPlayerRotation()
            calculatePistolProjectilesPosition(delta)
            checkControls(delta)
            setCameraPosition()
        }

        camera.update()
        batch.projectionMatrix = camera.combined

        ghostProjectiles.begin()
        if (::player.isInitialized) {
            batch.use {
                drawProjectiles(it)
                drawOpponents(it)
                drawPlayer(it, player)
                drawWalls(it)
                for (value in opponents.values) {
                    drawPlayer(it, value)
                }
            }
        }
        ghostProjectiles.end()
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

                    player = Player(MAP_WIDTH / 2f, MAP_HEIGHT / 2f, playerTexture, playerId)

                    Gdx.app.log("SocketIO", "My ID: $playerId")
                }
//                .on("newPlayer") { data ->
//                    val obj: JSONObject = data[0] as JSONObject
//                    val playerId = obj.getString("id")
//                    Gdx.app.log("SocketIO", "New player has just connected with ID: $playerId")
//                    opponents[playerId] = Player(WINDOW_WIDTH / 2 - PLAYER_SPRITE_WIDTH / 2,
//                            WINDOW_HEIGHT / 2 - PLAYER_SPRITE_HEIGHT / 2, playerTexture, playerId)
//                }
                .on("playerDisconnected") { data ->
                    val obj: JSONObject = data[0] as JSONObject
                    val playerId = obj.getString("id")
                    opponents.remove(playerId)
                }
//                .on("getPlayers") { data ->
//                    val obj: JSONArray = data[0] as JSONArray
//                    Gdx.app.log("Other players: ", "${data[0]}")
//                    for (i in 0 until obj.length()) {
//                        val playerId = obj.getJSONObject(i).getString("id")
//                        val newPlayer = Player(WINDOW_WIDTH / 2 - PLAYER_SPRITE_WIDTH / 2,
//                                WINDOW_HEIGHT / 2 - PLAYER_SPRITE_HEIGHT / 2, playerTexture, playerId)
//                        val vector = Vector2()
//                        vector.x = (obj.getJSONObject(i).getDouble("x").toFloat())
//                        vector.y = (obj.getJSONObject(i).getDouble("y").toFloat())
//                        newPlayer.setPosition(vector.x, vector.y)
//
//                        opponents[playerId] = newPlayer
//                    }
//                }
//                .on("playerMoved") { data ->
//                    val obj: JSONObject = data[0] as JSONObject
//                    val playerId = obj.getString("id")
//                    val x = obj.getDouble("x")
//                    val y = obj.getDouble("y")
//
//                    if (opponents[playerId] != null)
//                        opponents[playerId]!!.setPosition(x.toFloat(), y.toFloat())
//                }
                .on("gameData") { data ->
                    val obj = data[0] as JSONObject
                    val agents = obj.getJSONArray("agentData")
                    for (i in 0 until agents.length()) {
                        val agent = agents[i] as JSONObject
                        val id = agent.getString("id")
                        val x = agent.getLong("x").toFloat()
                        val y = agent.getLong("y").toFloat()
                        if (id == player.id) {
                            player.setPosition(x, y)
                        } else {
                            if (opponents[id] == null) {
                                opponents[id] = Opponent(x, y, 0f, 0f, playerTexture, id)
                            } else {
                                opponents[id]?.setPosition(x, y)
                            }
                        }
                    }

                    val proj = obj.getJSONArray("projectileData")

                    socketProjectiles.values.forEach { pistolProjectilePool.free(it as PistolProjectile) }
                    socketProjectiles.clear()

                    for (i in 0 until proj.length()) {
                        val projectile = proj[i] as JSONObject
                        val id = projectile.getString("id")
                        val x = projectile.getLong("x").toFloat()
                        val y = projectile.getLong("y").toFloat()
                        val xSpeed = projectile.getDouble("xSpeed").toFloat()
                        val ySpeed = projectile.getDouble("ySpeed").toFloat()

                        if (socketProjectiles[id] == null) {
                            socketProjectiles[id] = pistolProjectilePool.obtain().apply {
                                setPosition(x, y)
                                velocity.x = xSpeed
                                velocity.y = ySpeed
                            }
                        } else {
                            socketProjectiles[id]?.apply {
                                setPosition(x, y)
                                velocity.x = xSpeed
                                velocity.y = ySpeed
                            }
                        }
                    }

                    projectilesUpdated = true
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
        var angle = MathUtils.atan2(mousePosition.y - originY, mousePosition.x - originX) * MathUtils.radDeg
        if (angle < 0) angle += 360f
        player.facingDirectionAngle = angle
    }

    private fun checkControls(delta: Float) {
        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT) && player.canShoot()) {
            player.shoot()
            val xCentre = player.bounds.x + player.bounds.width / 2f
            val yCentre = player.bounds.y + player.bounds.height / 2f
            val edgePoint = projectToRectEdgeRad(player.facingDirectionAngle.toDouble(), player.bounds)

            edgePoint.x += xCentre - player.bounds.width / 2
            edgePoint.y += yCentre - player.bounds.height / 2
            spawnPistolProjectile(
                    edgePoint.x, edgePoint.y,
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

        for (projectile in clientProjectiles.values) {
            projectile.setPosition(
                    projectile.bounds.x + projectile.velocity.x * delta * projectile.speed,
                    projectile.bounds.y + projectile.velocity.y * delta * projectile.speed)
        }

        for (projectile in ghostProjectiles) {
            projectile.setPosition(
                    projectile.bounds.x + projectile.velocity.x * delta * projectile.speed,
                    projectile.bounds.y + projectile.velocity.y * delta * projectile.speed)
        }

//        clientProjectiles.iterate { projectile, iterator ->
//            projectile.setPosition(
//                    projectile.bounds.x + projectile.velocity.x * delta * projectile.speed,
//                    projectile.bounds.y + projectile.velocity.y * delta * projectile.speed)
//
//            if (projectile.bounds.x < 0 || projectile.bounds.x > MAP_WIDTH ||
//                    projectile.bounds.y < 0 || projectile.bounds.y > MAP_HEIGHT) {
//                // todo should check projectile type
//                pistolProjectilePool.free(projectile as PistolProjectile)
//                iterator.remove()
//                return
//            }
//
//            for (opponent in opponents.entries) {
//                if (Intersector.overlaps(projectile.bounds, opponent.value.bounds)) {
//                    // todo should check projectile type
//                    pistolProjectilePool.free(projectile as PistolProjectile)
//                    iterator.remove()
//                    return
//                }
//            }
//        }
    }

    private fun spawnPistolProjectile(x: Float, y: Float, xSpeed: Float, ySpeed: Float) {
        val projectile = pistolProjectilePool.obtain()
        projectile.setPosition(x, y)
        projectile.velocity.set(xSpeed, ySpeed)
        ghostProjectiles.add(projectile)
    }

    private fun drawPlayer(batch: Batch, agent: Agent) = agent.sprite.draw(batch)

    private fun drawProjectiles(batch: Batch) {
        clientProjectiles.values.forEach { it.sprite.draw(batch) }
        for (i in 0 until ghostProjectiles.size) ghostProjectiles[i].sprite.draw(batch)
    }

    private fun drawOpponents(batch: Batch) = opponents.values.forEach { it.sprite.draw(batch) }

    private fun drawWalls(batch: Batch) {
        for (i in 0 until walls.size) walls[i].draw(batch)
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
        if (player.bounds.x > WINDOW_WIDTH / 2f && player.bounds.x < MAP_WIDTH - WINDOW_WIDTH / 2f)
            camera.position.x = player.bounds.x

        if (player.bounds.y > WINDOW_HEIGHT / 2f && player.bounds.y < MAP_HEIGHT - WINDOW_HEIGHT / 2f)
            camera.position.y = player.bounds.y
    }

    override fun dispose() {

        super.dispose()
    }

    fun projectToRectEdgeRad(angle: Double, rect: Rectangle): Vector2 {

        var theta = angle * MathUtils.degreesToRadians

        while (theta < -MathUtils.PI) theta += MathUtils.PI2
        while (theta > MathUtils.PI) theta -= MathUtils.PI2

        val rectAtan = MathUtils.atan2(rect.height, rect.width)
        val tanTheta = tan(theta)
        val region: Int

        region = if ((theta > -rectAtan) && (theta <= rectAtan)) 1
        else if ((theta > rectAtan) && (theta <= (Math.PI - rectAtan))) 2
        else if ((theta > (Math.PI - rectAtan)) || (theta <= -(Math.PI - rectAtan))) 3
        else 4

        val edgePoint = Vector2().apply {
            x = rect.width / 2f
            y = rect.height / 2f
        }
        var xFactor = 1
        var yFactor = 1

        when (region) {
            3, 4 -> {
                xFactor = -1
                yFactor = -1
            }
        }

        when (region) {
            1, 3 -> {
                edgePoint.x += xFactor * (rect.width / 2f)
                edgePoint.y += yFactor * (rect.width / 2f) * tanTheta.toFloat()
            }
            else -> {
                edgePoint.x += xFactor * (rect.height / (2f * tanTheta.toFloat()))
                edgePoint.y += yFactor * (rect.height / 2f)
            }
        }

        return edgePoint
    }
}