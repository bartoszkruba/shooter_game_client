package com.mygdx.game.model.pickup

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.math.Rectangle


abstract class Pickup(var x: Float = 0f, var y: Float = 0f, width: Float, height: Float, texture: Texture) {

    val sprite = Sprite(texture)
    val bounds = Rectangle()

    init {
        sprite.setSize(width, height)
        sprite.setPosition(x, y)
        bounds.width = width
        bounds.height = height
        bounds.setPosition(x, y)
    }

    fun setPosition(x: Float, y: Float) {
        sprite.setPosition(x, y)
        bounds.setPosition(x, y)
    }
}