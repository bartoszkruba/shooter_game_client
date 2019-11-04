package com.mygdx.game.model

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.math.Rectangle
import com.mygdx.game.settings.*

abstract class Agent(x: Float, y: Float, var isDead: Boolean, texture: Texture, healthBarTexture: Texture, var weapon: Weapon,
                     var facingDirectionAngle: Float, var id: String) {
    var sprite: Sprite
    var healthBarSprite: Sprite
    val bounds: Rectangle
    var counter = 85f
    var healthBarSpriteWidth = 220f

    init {
        bounds = Rectangle(x, y, PLAYER_SPRITE_WIDTH, PLAYER_SPRITE_HEIGHT)
        // todo should be changed to loading assets from assetManager
        sprite = Sprite(texture)
        healthBarSprite = Sprite(healthBarTexture)

        healthBarSprite.setSize(healthBarSpriteWidth, HEALTH_BAR_SPRITE_HEIGHT)
        sprite.setSize(PLAYER_SPRITE_WIDTH, PLAYER_SPRITE_HEIGHT)
        sprite.setPosition(x, y)
        healthBarSprite.setPosition((sprite.x - ((200 - 32)/2)), y)

        healthBarSprite.setOrigin(healthBarSprite.width / 2f, healthBarSprite.height / 2f)
        sprite.setOrigin(sprite.width / 2f, sprite.height / 2f)
    }

    fun setPosition(x: Float, y: Float) {
        sprite.setPosition(x, y)
        healthBarSprite.setPosition((x - counter), y)
        bounds.setPosition(x, y)
    }

    fun reduceHealthBarWidth(){
        counter -= 20
        if(healthBarSpriteWidth >= 10)  healthBarSpriteWidth -= 50f else isDead = true
        healthBarSprite.x = sprite.x - counter
        healthBarSprite.setSize(healthBarSpriteWidth, HEALTH_BAR_SPRITE_HEIGHT)
    }
}