package com.mygdx.game.model

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.math.Rectangle
import com.mygdx.game.settings.WALL_SPRITE_HEIGHT
import com.mygdx.game.settings.WALL_SPRITE_WIDTH

class Wall(x: Float, y: Float, texture:Texture): Sprite(texture) {

    init {
        setSize(WALL_SPRITE_WIDTH, WALL_SPRITE_HEIGHT)
        setPosition(x, y)
        setBounds(x, y, WALL_SPRITE_WIDTH, WALL_SPRITE_HEIGHT)
    }
}