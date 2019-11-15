package com.mygdx.game.model.pickup

import com.badlogic.gdx.graphics.Texture
import com.mygdx.game.model.pickup.Pickup
import com.mygdx.game.settings.SHOTGUN_SPRITE_HEIGHT
import com.mygdx.game.settings.SHOTGUN_SPRITE_WIDTH

class ShotgunPickup(
        x: Float = 0f,
        y: Float = 0f,
        texture: Texture) : Pickup(
        x,
        y,
        SHOTGUN_SPRITE_WIDTH,
        SHOTGUN_SPRITE_HEIGHT,
        texture
)