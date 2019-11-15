package com.mygdx.game.model.agent

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.math.Vector2
import com.mygdx.game.model.weapon.Pistol
import com.mygdx.game.model.weapon.Weapon

val OPPONENT_SPRITE_COLOR = Color(0.9f, 0.3f, 0f, 1f)

class Opponent(
        x: Float,
        y: Float,
        name: String,
        isDead: Boolean,
        currentHealth: Float,
        gotShot: Boolean,
        xSpeed: Float = 0f,
        ySpeed: Float = 0f,
        textureAtlas: TextureAtlas,
        id: String,
        healthBarTexture: Texture,
        weapon: Weapon = Pistol(),
        facingDirectionAngle: Float = 0f) : Agent(x, y, name, isDead, currentHealth, gotShot, textureAtlas, healthBarTexture, weapon, facingDirectionAngle, id, OPPONENT_SPRITE_COLOR) {

    val speed: Vector2

    init {
        speed = Vector2(xSpeed, ySpeed)
    }

}