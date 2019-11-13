package com.mygdx.game.model

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.utils.Array

class Player(
        x: Float,
        y: Float,
        name: String,
        isDead: Boolean,
        currentHealth: Float,
        gotShot: Boolean,
        textureAtlas: TextureAtlas,
        healthBarTexture: Texture,
        id: String,
        weapon: Weapon = Pistol(),
        facingDirectionAngle: Float = 0f) : Agent(x, y, name, isDead, currentHealth,gotShot, textureAtlas, healthBarTexture, weapon, facingDirectionAngle, id) {

    fun canShoot() = weapon.canShoot()
    fun shoot() = weapon.shoot()
}