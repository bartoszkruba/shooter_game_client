package com.mygdx.game.model

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.utils.Array

class Player(
        x: Float,
        y: Float,
        name: String,
        isDead: Boolean,
        currentHealth: Float,
        gotShot: Boolean,
        texture: Array<Texture>,
        healthBarTexture: Texture,
        id: String,
        weapon: Weapon = Pistol(),
        facingDirectionAngle: Float = 0f) : Agent(x, y, name, isDead, currentHealth,gotShot, texture, healthBarTexture, weapon, facingDirectionAngle, id) {

    fun canShoot() = weapon.canShoot()
    fun shoot() = weapon.shoot()
}