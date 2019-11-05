package com.mygdx.game.model

import com.badlogic.gdx.graphics.Texture

class Player(
        x: Float,
        y: Float,
        isDead: Boolean,
        currentHealth: Float,
        texture: Texture,
        healthBarTexture: Texture,
        id: String,
        weapon: Weapon = Pistol(),
        facingDirectionAngle: Float = 0f) : Agent(x, y, isDead, currentHealth, texture, healthBarTexture, weapon, facingDirectionAngle, id) {

    fun canShoot() = weapon.canShoot()
    fun shoot() = weapon.shoot()
}