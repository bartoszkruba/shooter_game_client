package com.mygdx.game.model

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.math.Rectangle

class Player(x: Float, y: Float) {

    var sprite: Sprite
    val bounds: Rectangle

    init {
        bounds = Rectangle(x, y, 32f, 64f)
        // todo should be changed to loading assets from assetManager
        sprite = Sprite(Texture(Gdx.files.internal("player_placeholder.png")))
        sprite.setSize(32f, 64f)
        sprite.setPosition(x, y)
    }

    fun setPosition(x: Float, y: Float) {
        sprite.setPosition(x, y)
        bounds.setPosition(x, y)
    }
}