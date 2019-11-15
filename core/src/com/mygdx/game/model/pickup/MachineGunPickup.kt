package com.mygdx.game.model.pickup

import com.badlogic.gdx.graphics.Texture
import com.mygdx.game.model.pickup.Pickup
import com.mygdx.game.settings.MACHINE_GUN_SPRITE_HEIGHT
import com.mygdx.game.settings.MACHINE_GUN_SPRITE_WIDTH

class MachineGunPickup(
        x: Float = 0f,
        y: Float = 0f,
        texture: Texture) : Pickup(
        x,
        y,
        MACHINE_GUN_SPRITE_WIDTH,
        MACHINE_GUN_SPRITE_HEIGHT,
        texture
)