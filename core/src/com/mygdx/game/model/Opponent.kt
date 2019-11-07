package com.mygdx.game.model

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Array

class Opponent(
        x: Float,
        y: Float,
        xSpeed: Float = 0f,
        ySpeed: Float = 0f,
        texture: Array<Texture>,
        id: String,
        healthBarTexture: Texture,
        weapon: Weapon = Pistol(),
        facingDirectionAngle: Float = 0f) : Agent(x, y, texture, healthBarTexture, weapon, facingDirectionAngle, id) {

    val speed: Vector2

    init {
        speed = Vector2(xSpeed, ySpeed)
    }

}