package com.mygdx.game.client

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.mygdx.game.assets.Atlases
import com.mygdx.game.assets.Textures
import com.mygdx.game.model.agent.Opponent
import com.mygdx.game.model.agent.Player
import com.mygdx.game.model.explosion.ExplosionType
import com.mygdx.game.model.obstacles.Wall
import com.mygdx.game.model.pickup.*
import com.mygdx.game.model.projectile.*
import com.mygdx.game.model.weapon.Bazooka
import com.mygdx.game.model.weapon.MachineGun
import com.mygdx.game.model.weapon.Pistol
import com.mygdx.game.model.weapon.Shotgun
import com.mygdx.game.screen.GameObjects
import com.mygdx.game.screen.Pools
import com.mygdx.game.settings.*
import com.mygdx.game.util.getZonesForRectangle
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONArray
import org.json.JSONObject

class Client {
    companion object {
        lateinit var socket: Socket
        var shouldPlayReload = false

        private lateinit var player: Player
        private lateinit var textures: Textures
        private lateinit var atlases: Atlases
        private lateinit var pools: Pools
        private lateinit var gameObj: GameObjects

        fun connectionSocket(ipAddress: String) {
            try {
                socket = IO.socket("http://$ipAddress:8080")
                socket.connect()
            } catch (e: Exception) {
                println("something went wrong!")
            }
        }

        fun getPlayer(): Player? {
            if (Companion::player.isInitialized) {
                return player
            }
            return null
        }

        fun configSocketEvents(textures: Textures, atlases: Atlases, pools: Pools, gameObjects: GameObjects) {

            Companion.atlases = atlases
            Companion.textures = textures
            Companion.pools = pools
            Companion.gameObj = gameObjects

            socket.on(Socket.EVENT_CONNECT) { Gdx.app.log("SocketIO", "Connected") }
                    .on("socketID") { processSocketID(it) }
                    .on("playerDisconnected") { removeOpponent(it) }
                    .on("newProjectile") { processNewProjectile(it) }
                    .on("agentData") { processAgentData(it) }
                    .on("zombieData") { processZombieData(it) }
                    .on("projectileData") { processProjectileData(it) }
                    .on("pickupData") { processPickupData(it) }
                    .on("barrelData") { processBarrelData(it) }
                    .on("wallData") { processWallData(it) }
                    .on("newExplosion") { processNewExplosion(it) }
                    .on("scoreboardData") { processScoreboardData(it) }
                    .on("killConfirm") { processKillConfirm(it) }
                    .on("zombieDead") { processZombieDead(it) }
        }

        var shouldPlayDeathSound = false

        private fun processZombieDead(data: Array<Any>) {
            val id = (data[0] as JSONObject).getString("id")
            gameObj.zombies[id]?.isDead = true
            shouldPlayDeathSound = true
        }

        private fun processSocketID(data: Array<Any>) {
            val obj: JSONObject = data[0] as JSONObject
            val playerId = obj.getString("id")

            createPlayer(playerId, textures.healthBarTexture, atlases.playerAtlas)

            Gdx.app.log("SocketIO", "My ID: $playerId")
        }

        private fun processKillConfirm(data: kotlin.Array<Any>) {
            shouldPlayDeathSound = true
        }

        private fun processScoreboardData(data: kotlin.Array<Any>) {
            val obj = data[0] as JSONObject
            val agents = obj.getJSONArray("scoreboardData")
            for (i in 0 until agents.length()) {
                val agent = agents[i] as JSONObject
                val id = agent.getString("id")
                val kills = agent.getInt("kills")
                val deaths = agent.getInt("deaths")
                val name = agent.getString("name")

                for (player in gameObj.playerOnScoreboardTable.values) {
                    if (player.id == id) {
                        player.kills = kills
                        player.deaths = deaths
                    } else {
                        gameObj.playerOnScoreboardTable[id] = Opponent(0f, 0f, name, kills, deaths, false,
                                0f, false, atlases.playerAtlas, id, textures.healthBarTexture)
                    }
                }
            }
        }

        private fun processWallData(data: kotlin.Array<Any>) {
            gameObj.walls.clear()
            generateEdgeWalls()
            val walls = data[0] as JSONArray
            for (i in 0 until walls.length()) {
                val obj = walls[i] as JSONObject
                val x = obj.getDouble("x").toFloat()
                val y = obj.getDouble("y").toFloat()
                val wall = Wall(x, y, textures.wallTexture)
                for (zone in getZonesForRectangle(wall.bounds)) {
                    gameObj.wallMatrix[zone]?.add(wall)
                }
                gameObj.walls.add(wall)
            }
        }

        private fun processPickupData(data: kotlin.Array<Any>) {
            val obj = data[0] as JSONObject
            val picks = obj.getJSONArray("pickupData")

            for (pickup in gameObj.pickups.values) {
                when (pickup) {
                    is PistolPickup -> pools.pistolPickupPool.free(pickup)
                    is MachineGunPickup -> pools.machineGunPickupPool.free(pickup)
                    is ShotgunPickup -> pools.shotgunPickupPool.free(pickup)
                    is BazookaPickup -> pools.bazookaPickupPool.free(pickup)
                }
            }

            gameObj.pickups.clear()

            for (i in 0 until picks.length()) {
                val pickup = picks[i] as JSONObject
                val id = pickup.getString("id")
                val x = pickup.getDouble("x").toFloat()
                val y = pickup.getDouble("y").toFloat()
                val type = pickup.getString("type")

                gameObj.pickups[id] = when (type) {
                    ProjectileType.PISTOL -> pools.pistolPickupPool.obtain()
                    ProjectileType.SHOTGUN -> pools.shotgunPickupPool.obtain()
                    ProjectileType.BAZOOKA -> pools.bazookaPickupPool.obtain()
                    else -> pools.machineGunPickupPool.obtain()
                }.apply { setPosition(x, y) }
            }
        }

        private fun processProjectileData(data: kotlin.Array<Any>) {
            val obj = data[0] as JSONObject
            val proj = obj.getJSONArray("projectileData")

            for (i in 0 until proj.length()) {
                val projectile = proj[i] as JSONObject
                val type = projectile.getString("type")
                val id = projectile.getString("id")
                val x = projectile.getLong("x").toFloat()
                val y = projectile.getLong("y").toFloat()
                val xSpeed = projectile.getDouble("xSpeed").toFloat()
                val ySpeed = projectile.getDouble("ySpeed").toFloat()
                val agentId = projectile.getString("agentId")

                if (gameObj.projectiles[id] == null) {
                    gameObj.projectiles[id] = when (type) {
                        ProjectileType.PISTOL -> pools.pistolProjectilePool.obtain()
                        ProjectileType.SHOTGUN -> pools.shotgunProjectilePool.obtain()
                        ProjectileType.BAZOOKA -> pools.bazookaProjectilePool.obtain()
                        else -> pools.machineGunProjectilePool.obtain()
                    }.apply {
                        setPosition(x, y)
                        velocity.x = xSpeed
                        velocity.y = ySpeed
                        this.agentId = agentId
                    }
                } else {
                    gameObj.projectiles[id]?.apply {
                        setPosition(x, y)
                        velocity.x = xSpeed
                        velocity.y = ySpeed
                        this.agentId = agentId
                    }
                }
            }
        }

        private fun processBarrelData(data: kotlin.Array<Any>) {
            for (barrel in gameObj.explosiveBarrels.values) pools.explosiveBarrelPool.free(barrel)
            gameObj.explosiveBarrels.clear()
            val barrels = data[0] as JSONArray

            for (i in 0 until barrels.length()) {
                val barrel = barrels[i] as JSONObject
                val x = barrel.getDouble("x").toFloat()
                val y = barrel.getDouble("y").toFloat()
                val id = barrel.getString("id")
                gameObj.explosiveBarrels[id] = pools.explosiveBarrelPool.obtain().apply { setPosition(x, y) }
            }
        }

        private fun processAgentData(data: kotlin.Array<Any>) {
            val obj = data[0] as JSONObject
            val agents = obj.getJSONArray("agentData")

            for (i in 0 until agents.length()) {
                val agent = agents[i] as JSONObject
                val id = agent.getString("id")
                val name = agent.getString("name")
                val isDead = agent.getBoolean("isDead")
                val currentHealth = agent.getLong("currentHealth").toFloat()
                val x = agent.getLong("x").toFloat()
                val y = agent.getLong("y").toFloat()
                val weapon = agent.getString("weapon")
                val xVelocity = agent.getLong("xVelocity").toFloat()
                val yVelocity = agent.getLong("yVelocity").toFloat()
                val angle = agent.getDouble("angle").toFloat()
                val invisible = agent.getBoolean("inv")
                if (id == player.id) {
                    if (!isDead) {
                        if (player.weapon.type != weapon) {
                            when (weapon) {
                                ProjectileType.PISTOL -> player.weapon = Pistol()
                                ProjectileType.MACHINE_GUN -> player.weapon = MachineGun()
                                ProjectileType.SHOTGUN -> player.weapon = Shotgun()
                                ProjectileType.BAZOOKA -> player.weapon = Bazooka()
                            }
                        }
                        val bulletsLeft = agent.getInt("bulletsLeft")
                        if (bulletsLeft == -1 && player.weapon.bulletsInChamber != -1) shouldPlayReload = true
                        //playerOnScoreboardTable[id]!!.name = name
                        player.apply {
                            this.invisible = invisible
                            this.name = name
                            this.isDead = isDead
                            setPosition(x, y)
                            this.weapon.bulletsInChamber = bulletsLeft
                            gotShot = player.currentHealth != currentHealth
                            this.currentHealth = currentHealth
                            setHealthBar(currentHealth, x, y)
                        }
                    } else player.isDead = true
                } else {
                    if (gameObj.opponents[id] == null) {
                        createOpponent(id, x, y, name, currentHealth).apply {
                            velocity.x = xVelocity
                            setAngle(angle)
                            velocity.y = yVelocity
                            isMoving = xVelocity == 0f && yVelocity == 0f
                            this.invisible = invisible
                        }
                    } else {
                        gameObj.opponents[id]?.apply {
                            this.name = name
                            gotShot = gameObj.opponents[id]?.currentHealth != currentHealth
                            setPosition(x, y)
                            setAngle(angle)
                            velocity.x = xVelocity
                            velocity.y = yVelocity
                            setHealthBar(currentHealth, x, y)
                            this.isDead = isDead
                            this.currentHealth = currentHealth
                            healthBarSprite.setSize(currentHealth, HEALTH_BAR_SPRITE_HEIGHT)
                            isMoving = xVelocity == 0f && yVelocity == 0f
                            this.invisible = invisible
                        }
                    }
                }
            }
        }

        private fun processZombieData(data: Array<Any>) {
            val obj = data[0] as JSONObject
            val zombies = obj.getJSONArray("zombieData")

            for (i in 0 until zombies.length()) {
                val zombie = zombies[i] as JSONObject
                val x = zombie.getDouble("x").toFloat()
                val y = zombie.getDouble("y").toFloat()
                val xVelocity = zombie.getDouble("xVelocity").toFloat()
                val yVelocity = zombie.getDouble("yVelocity").toFloat()
                val isDead = zombie.getBoolean("isDead")
                val currentHealth = zombie.getDouble("currentHealth").toFloat()
                val id = zombie.getString("id")
                val angle = zombie.getDouble("angle").toFloat()

                if (gameObj.zombies[id] == null) gameObj.zombies[id] = pools.zombiePool.obtain()
                gameObj.zombies[id]?.apply {
                    setPosition(x, y)
                    this.isDead = isDead
                    this.facingDirectionAngle = angle
                    this.velocity.x = xVelocity
                    this.velocity.y = yVelocity
                    gotShot = player.currentHealth != currentHealth
                    this.currentHealth = currentHealth
                    setHealthBar(currentHealth, x, y)
                }
            }
        }

        private fun processNewExplosion(data: kotlin.Array<Any>) {
            val explosion = data[0] as JSONObject
            val x = explosion.getDouble("x").toFloat()
            val y = explosion.getDouble("y").toFloat()
            val type = explosion.getString("type")
            if (type == ExplosionType.BAZOOKA) {
                gameObj.bazookaExplosions.add(pools.bazookaExplosionPool.obtain().apply {
                    this.justSpawned = true
                    this.x = x
                    this.y = y
                    resetTimer()
                })
            } else if (type == ExplosionType.BARREL) {
                gameObj.barrelExplosions.add(pools.barrelExplosionPool.obtain().apply {
                    this.justSpawned = true
                    this.x = x
                    this.y = y
                    resetTimer()
                })
            }
        }

        private fun processNewProjectile(data: kotlin.Array<Any>) {
            val projectile = data[0] as JSONObject
            val type = projectile.getString("type")
            val id = projectile.getString("id")
            val x = projectile.getLong("x").toFloat()
            val y = projectile.getLong("y").toFloat()
            val xSpeed = projectile.getDouble("xSpeed").toFloat()
            val ySpeed = projectile.getDouble("ySpeed").toFloat()
            val agentId = projectile.getString("agentId")

            if (agentId == player.id) player.weapon.shoot()

            if (gameObj.projectiles[id] == null) {
                gameObj.projectiles[id] = when (type) {
                    ProjectileType.PISTOL -> pools.pistolProjectilePool.obtain()
                    ProjectileType.SHOTGUN -> pools.shotgunProjectilePool.obtain()
                    ProjectileType.BAZOOKA -> pools.bazookaProjectilePool.obtain()
                    else -> pools.machineGunProjectilePool.obtain()
                }.apply {
                    setPosition(x, y)
                    velocity.x = xSpeed
                    velocity.y = ySpeed
                    justFired = true
                    this.agentId = agentId
                }
            } else {
                gameObj.projectiles[id]?.apply {
                    setPosition(x, y)
                    velocity.x = xSpeed
                    velocity.y = ySpeed
                    justFired = true
                    this.agentId = agentId
                }
            }
        }

        fun setPlayerName(name: String) {
            val data = JSONObject()
            data.put("name", name)
            socket.emit("playerName", data)
        }

        private fun createOpponent(id: String, x: Float, y: Float, name: String, currentHealth: Float): Opponent {
            val opponent = Opponent(x, y, name, 0, 0, false, currentHealth, false,
                    atlases.playerAtlas, id, textures.healthBarTexture)
            gameObj.opponents[id] = opponent
            return opponent
        }

        private fun removeOpponent(data: kotlin.Array<Any>) {
            val obj: JSONObject = data[0] as JSONObject
            val playerId = obj.getString("id")
            gameObj.opponents.remove(playerId)
            gameObj.playerOnScoreboardTable.remove(playerId)
        }

        private fun createPlayer(playerId: String, healthBarTexture: Texture, playerTextures: TextureAtlas) {
            val player = Player(500f, 500f, "", 0, 0, false,
                    PLAYER_MAX_HEALTH, false, playerTextures, healthBarTexture, playerId)
            Companion.player = player
            gameObj.playerOnScoreboardTable[playerId] = player
        }

        fun broadcastKeyPressed(key: String) {
            val data = JSONObject()
            data.put("key", key)
            socket.emit("startKey", data)
        }

        fun broadcastKeyReleased(key: String) {
            val data = JSONObject()
            data.put("key", key)
            socket.emit("stopKey", data)
        }

        fun broadcastRestart() {
            socket.emit("restart")
        }

        fun brodcastPlayerFacingDirection() {
            val data = JSONObject()
            data.put("degrees", player.facingDirectionAngle)
            socket.emit("playerRotation", data)
        }

        private fun generateEdgeWalls() {
            for (i in 0 until MAP_HEIGHT step WALL_SPRITE_HEIGHT.toInt()) {
                gameObj.walls.add(Wall(0f, i.toFloat(), textures.wallTexture))
                gameObj.walls.add(Wall(MAP_WIDTH - WALL_SPRITE_WIDTH, i.toFloat(), textures.wallTexture))
            }
            for (i in WALL_SPRITE_WIDTH.toInt() until MAP_WIDTH - WALL_SPRITE_WIDTH.toInt() step WALL_SPRITE_WIDTH.toInt()) {
                gameObj.walls.add(Wall(i.toFloat(), 0f, textures.wallTexture))
                gameObj.walls.add(Wall(i.toFloat(), MAP_HEIGHT - WALL_SPRITE_HEIGHT, textures.wallTexture))
            }
        }
    }
}