package com.mygdx.game.ui

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.math.Rectangle

class MenuChoice(active: Texture, inactive: Texture, val type: String, x: Float = 0f, y: Float = 0f) {

    var active = false
        set(value) {
            field = value
            if (value) this.sprite = activeSprite
            else this.sprite = inactiveSprite
            sprite.setPosition(bounds.x, bounds.y)
        }

    private val activeSprite: Sprite = Sprite(active)
    private val inactiveSprite: Sprite = Sprite(inactive)
    var sprite: Sprite

    var bounds: Rectangle

    init {
        activeSprite.setPosition(x, y)
        inactiveSprite.setPosition(x, y)
        sprite = inactiveSprite
        bounds = Rectangle(x, y, active.width.toFloat(), active.height.toFloat())
    }

    fun setPosition(x: Float, y: Float) {
        this.sprite.setPosition(x, y)
        this.bounds.setPosition(x, y)
    }

}