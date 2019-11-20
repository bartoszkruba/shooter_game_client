package com.mygdx.game.model.obstacles

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.math.Rectangle
import com.mygdx.game.settings.EXPLOSIVE_BARREL_SPRITE_HEIGHT
import com.mygdx.game.settings.EXPLOSIVE_BARREL_SPRITE_WIDTH
import com.mygdx.game.settings.WALL_SPRITE_HEIGHT
import com.mygdx.game.settings.WALL_SPRITE_WIDTH

class ExplosiveBarrel(x: Float = 0f, y: Float = 0f, texture: Texture) : Sprite(texture) {
    val bounds = Rectangle(x, y, EXPLOSIVE_BARREL_SPRITE_WIDTH, EXPLOSIVE_BARREL_SPRITE_HEIGHT)

    init {
        setSize(EXPLOSIVE_BARREL_SPRITE_WIDTH, EXPLOSIVE_BARREL_SPRITE_HEIGHT)
        setBounds(x, y, EXPLOSIVE_BARREL_SPRITE_WIDTH, EXPLOSIVE_BARREL_SPRITE_HEIGHT)
        setPosition(x, y)
    }

    override fun setPosition(x: Float, y: Float) {
        super.setPosition(x, y)
        super.setBounds(x, y, EXPLOSIVE_BARREL_SPRITE_WIDTH, EXPLOSIVE_BARREL_SPRITE_HEIGHT)
        bounds.setPosition(x, y)
    }
}