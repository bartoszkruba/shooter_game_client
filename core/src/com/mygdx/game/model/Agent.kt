package com.mygdx.game.model

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.math.Rectangle
import com.mygdx.game.settings.*

abstract class Agent(x: Float, y: Float, texture: Texture, helthBartexture: Texture, var weapon: Weapon, var facingDirectionAngle: Float) {
    var sprite: Sprite
    var healthBarSprite: Sprite
    val bounds: Rectangle

    init {
        bounds = Rectangle(x, y, PLAYER_SPRITE_WIDTH, PLAYER_SPRITE_HEIGHT)
        // todo should be changed to loading assets from assetManager
        sprite = Sprite(texture)
        healthBarSprite = Sprite(helthBartexture)

        healthBarSprite.setSize(HEALTH_BAR_SPRITE_WIDTH, HEALTH_BAR_SPRITE_HEIGHT)
        sprite.setSize(PLAYER_SPRITE_WIDTH, PLAYER_SPRITE_HEIGHT)

        healthBarSprite.setPosition(1110f / 2 - PLAYER_SPRITE_WIDTH / 2,
                y)
        sprite.setPosition(x, y)

        healthBarSprite.setOrigin(healthBarSprite.width / 2f, healthBarSprite.height / 2f)
        sprite.setOrigin(sprite.width / 2f, sprite.height / 2f)
    }

    fun setPosition(x: Float, y: Float) {
        sprite.setPosition(x, y)
        healthBarSprite.setPosition(x - 85, y)
        bounds.setPosition(x, y)
    }

}