package com.mygdx.game.model.pickup

import com.badlogic.gdx.graphics.Texture
import com.mygdx.game.model.pickup.Pickup
import com.mygdx.game.settings.PISTOL_SPRITE_HEIGHT
import com.mygdx.game.settings.PISTOL_SPRITE_WIDTH

class PistolPickup(
        x: Float = 0f,
        y: Float = 0f,
        texture: Texture) : Pickup(
        x,
        y,
        PISTOL_SPRITE_WIDTH,
        PISTOL_SPRITE_HEIGHT,
        texture
)