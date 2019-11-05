package com.mygdx.game.model

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.math.Vector2

class Opponent(
        x: Float,
        y: Float,
        isDead: Boolean,
        currentHealth: Float,
        xSpeed: Float = 0f,
        ySpeed: Float = 0f,
        texture: Texture,
        id: String,
        healthBarTexture: Texture,
        weapon: Weapon = Pistol(),
        facingDirectionAngle: Float = 0f) : Agent(x, y, isDead, currentHealth, texture, healthBarTexture, weapon, facingDirectionAngle, id) {

    val speed: Vector2

    init {
        speed = Vector2(xSpeed, ySpeed)
    }

}