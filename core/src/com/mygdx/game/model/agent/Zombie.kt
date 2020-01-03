package com.mygdx.game.model.agent

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureAtlas

val ZOMBIE_SPRITE_COLOR = Color(0.0f, 0.0f, 0.9f, 1f)

class Zombie(
        x: Float,
        y: Float,
        isDead: Boolean,
        currentHealth: Float,
        gotShot: Boolean,
        textureAtlas: TextureAtlas,
        id: String,
        healthBarTexture: Texture,
        facingDirectionAngle: Float = 0f)
    : Agent(
        x,
        y,
        "",
        0,
        0,
        isDead,
        currentHealth,
        gotShot,
        textureAtlas,
        healthBarTexture,
        facingDirectionAngle,
        id,
        ZOMBIE_SPRITE_COLOR)