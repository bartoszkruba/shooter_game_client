package com.mygdx.game.model

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.mygdx.game.settings.PISTOL_BULLET_SPEED

class PistolProjectile(
        x: Float = 0f,
        y: Float = 0f,
        xSpeed: Float = 0f,
        ySpeed: Float = 0f) : Projectile(
        x,
        y,
        xSpeed,
        ySpeed,
        Texture(Gdx.files.internal("images/projectile.jpg")),
        4f,
        ProjectileType.PISTOL,
        PISTOL_BULLET_SPEED)