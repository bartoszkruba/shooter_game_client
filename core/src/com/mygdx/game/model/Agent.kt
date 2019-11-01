package com.mygdx.game.model

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.math.Rectangle
import com.mygdx.game.settings.PLAYER_SPRITE_HEIGHT
import com.mygdx.game.settings.PLAYER_SPRITE_WIDTH

abstract class Agent(x: Float, y: Float, texture: Texture, var weapon: Weapon, var facingDirectionAngle: Float) {
    var sprite: Sprite = Sprite(texture)
    val bounds: Rectangle = Rectangle(x, y, PLAYER_SPRITE_WIDTH, PLAYER_SPRITE_HEIGHT)

    init {
        // todo should be changed to loading assets from assetManager
        sprite.setSize(PLAYER_SPRITE_WIDTH, PLAYER_SPRITE_HEIGHT)
        sprite.setPosition(x, y)
        sprite.setOrigin(sprite.width / 2f, sprite.height / 2f)
    }

    fun setPosition(x: Float, y: Float) {
        sprite.setPosition(x, y)
        bounds.setPosition(x, y)
    }
}