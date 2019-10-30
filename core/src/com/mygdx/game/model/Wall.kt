package com.mygdx.game.model

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.math.Rectangle

class Wall(x: Float, y: Float) {
    val sprite: Sprite
    val bounds: Rectangle

    init {
        // todo change to assets
        val texture = Texture(Gdx.files.internal("images/wall.png"))

        sprite = Sprite(texture)
        sprite.setSize(50f, 50f)
        sprite.setPosition(x, y)

        bounds = Rectangle(x, y, 50f, 50f)
    }

    fun setPosition(x: Float, y: Float) {
        bounds.setPosition(x, y)
        sprite.setPosition(x, y)
    }
}