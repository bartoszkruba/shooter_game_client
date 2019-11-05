package com.mygdx.game.model

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.math.Rectangle
import com.mygdx.game.settings.*

abstract class Agent(x: Float, y: Float, var isDead: Boolean, var currentHealth: Float, texture: Texture, healthBarTexture: Texture, var weapon: Weapon,
                     var facingDirectionAngle: Float, var id: String) {
    var sprite: Sprite
    var healthBarSprite: Sprite
    val bounds: Rectangle

    init {
        bounds = Rectangle(x, y, PLAYER_SPRITE_WIDTH, PLAYER_SPRITE_HEIGHT)
        // todo should be changed to loading assets from assetManager
        sprite = Sprite(texture)
        healthBarSprite = Sprite(healthBarTexture)

        healthBarSprite.setSize(currentHealth, HEALTH_BAR_SPRITE_HEIGHT)
        sprite.setSize(PLAYER_SPRITE_WIDTH, PLAYER_SPRITE_HEIGHT)

        sprite.setPosition(x, y)
        healthBarSprite.setPosition(x - 85f, y)

        healthBarSprite.setOrigin(0f, healthBarSprite.height / 2f)
        sprite.setOrigin(sprite.width / 2f, sprite.height / 2f)
    }

    fun setHealthBar(currentHealth: Float, x: Float, y: Float){
        healthBarSprite.setSize(currentHealth, HEALTH_BAR_SPRITE_HEIGHT)
        healthBarSprite.setPosition((x - ((currentHealth - 45)/2)), y)
    }

    fun setPosition(x: Float, y: Float) {
        sprite.setPosition(x, y)
        healthBarSprite.setPosition((x - ((currentHealth - 45)/2)), y)
        bounds.setPosition(x, y)
    }
}