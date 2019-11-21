package com.mygdx.game.model.pickup

import com.badlogic.gdx.graphics.Texture
import com.mygdx.game.settings.BAZOOKA_SPRITE_HEIGHT
import com.mygdx.game.settings.BAZOOKA_SPRITE_WIDTH

class BazookaPickup(
        x: Float = 0f,
        y: Float = 0f,
        texture: Texture) : Pickup(
        x,
        y,
        BAZOOKA_SPRITE_WIDTH,
        BAZOOKA_SPRITE_HEIGHT,
        texture
)