package com.mygdx.game.model

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.mygdx.game.settings.PISTOL_BULLET_SPEED
import com.mygdx.game.settings.STANDARD_PROJECTILE_WIDTH

class PistolProjectile(
        x: Float = 0f,
        y: Float = 0f,
        xSpeed: Float = 0f,
        ySpeed: Float = 0f) : Projectile(
        x,
        y,
        xSpeed,
        ySpeed,
        Texture(Gdx.files.internal("images/standard_projectile.jpg")),
        STANDARD_PROJECTILE_WIDTH / 2,
        ProjectileType.PISTOL,
        PISTOL_BULLET_SPEED)