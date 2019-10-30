package com.mygdx.game.model

import com.badlogic.gdx.graphics.Texture

class Player(
        x: Float,
        y: Float,
        texture: Texture,
        weapon: Weapon = Pistol(),
        facingDirectionAngle: Float = 0f) : Agent(x, y, texture, weapon, facingDirectionAngle) {

    fun canShoot() = weapon.canShoot()
    fun shoot() = weapon.shoot()
}