package com.mygdx.game.model.agent

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.mygdx.game.model.weapon.Pistol
import com.mygdx.game.model.weapon.Weapon

val PLAYER_SPRITE_COLOR = Color(0f, 0.9f, 0.0f, 1f)

class Player(
        x: Float,
        y: Float,
        name: String,
        kills: Int,
        deaths: Int,
        isDead: Boolean,
        currentHealth: Float,
        gotShot: Boolean,
        textureAtlas: TextureAtlas,
        healthBarTexture: Texture,
        id: String,
        var weapon: Weapon = Pistol(),
        facingDirectionAngle: Float = 0f)
    : Agent(
        x,
        y,
        name,
        kills,
        deaths,
        isDead,
        currentHealth,
        gotShot,
        textureAtlas,
        healthBarTexture,
        facingDirectionAngle,
        id,
        PLAYER_SPRITE_COLOR)