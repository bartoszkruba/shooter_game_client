package com.mygdx.game.screen

import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.utils.Array
import com.mygdx.game.assets.Textures
import com.mygdx.game.model.agent.Agent
import com.mygdx.game.model.agent.Opponent
import com.mygdx.game.model.agent.Zombie
import com.mygdx.game.model.explosion.BarrelExplosion
import com.mygdx.game.model.explosion.BazookaExplosion
import com.mygdx.game.model.obstacles.ExplosiveBarrel
import com.mygdx.game.model.obstacles.Wall
import com.mygdx.game.model.pickup.Pickup
import com.mygdx.game.model.projectile.Projectile
import com.mygdx.game.settings.*
import java.util.concurrent.ConcurrentHashMap

class GameObjects(textures: Textures) {
    val projectiles = ConcurrentHashMap<String, Projectile>()
    val bazookaExplosions = Array<BazookaExplosion>()
    val barrelExplosions = Array<BarrelExplosion>()
    val explosiveBarrels = ConcurrentHashMap<String, ExplosiveBarrel>()
    val opponents = ConcurrentHashMap<String, Opponent>()
    val zombies = ConcurrentHashMap<String, Zombie>()
    val wallMatrix = generateWallMatrix()
    val walls = Array<Wall>()
    val ground = generateGround(textures)
    val playerOnScoreboardTable: ConcurrentHashMap<String, Agent> = ConcurrentHashMap()
    val pickups = ConcurrentHashMap<String, Pickup>()
    val bloodOnTheFloor = ArrayList<Blood>()

    fun generateWallMatrix(): HashMap<String, Array<Wall>> {
        val matrix = HashMap<String, Array<Wall>>()

        for (i in 0 until (MAP_WIDTH / ZONE_SIZE) + 1) {
            for (j in 0 until (MAP_HEIGHT / ZONE_SIZE) + 1) {
                matrix["_${i}_${j}"] = Array()
            }
        }
        return matrix
    }

    fun generateGround(textures: Textures): Array<Sprite> {
        val ground = Array<Sprite>()
        for (i in 0 until (MAP_HEIGHT % GROUND_TEXTURE_HEIGHT + 1).toInt()) {
            for (j in 0 until (MAP_WIDTH % GROUND_TEXTURE_WIDTH + 1).toInt()) {
                val groundSprite = Sprite(textures.groundTexture)
                groundSprite.setPosition(i * GROUND_TEXTURE_WIDTH, j * GROUND_TEXTURE_HEIGHT)
                groundSprite.setSize(GROUND_TEXTURE_WIDTH, GROUND_TEXTURE_HEIGHT)
                ground.add(groundSprite)
            }
        }
        return ground
    }
}