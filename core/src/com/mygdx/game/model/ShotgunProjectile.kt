package com.mygdx.game.model

import com.badlogic.gdx.graphics.Texture
import com.mygdx.game.settings.SHOTGUN_BULLET_SPEED
import com.mygdx.game.settings.STANDARD_PROJECTILE_WIDTH

class ShotgunProjectile(
        x: Float,
        y: Float,
        xSpeed: Float = 0f,
        ySpeed: Float = 0f,
        texture: Texture) : Projectile(
        x,
        y,
        xSpeed,
        ySpeed,
        texture,
        STANDARD_PROJECTILE_WIDTH / 2,
        SHOTGUN_BULLET_SPEED
)