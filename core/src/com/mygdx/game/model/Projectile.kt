package com.mygdx.game.model

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.math.Circle
import com.badlogic.gdx.math.Vector2

class Projectile(x: Float, y: Float, xSpeed: Float = 0f, ySpeed: Float = 0f) {
    val bounds: Circle
    val sprite: Sprite
    val speed: Vector2
    val type = ProjectileType.PISTOL

    init {
        val texture = Texture(Gdx.files.internal("images/projectile.jpg"))
        bounds = Circle()
        bounds.x = x
        bounds.y = y
        bounds.radius = 4f
        sprite = Sprite(texture)
        sprite.setPosition(x - 4f, y - 4f)
        speed = Vector2(xSpeed, ySpeed)
    }
}