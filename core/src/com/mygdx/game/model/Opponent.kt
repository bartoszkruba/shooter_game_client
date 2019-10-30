package com.mygdx.game.model

import com.badlogic.gdx.math.Vector2

class Opponent(
        x: Float,
        y: Float,
        xSpeed: Float = 0f,
        ySpeed: Float = 0f,
        weapon: Weapon = Pistol(),
        facingDirectionAngle: Float = 0f) : Agent(x, y, weapon, facingDirectionAngle) {

    val speed: Vector2

    init {
        speed = Vector2(xSpeed, ySpeed)
    }

}