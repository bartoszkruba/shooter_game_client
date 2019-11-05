package com.mygdx.game.model

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.math.Circle
import com.badlogic.gdx.math.Vector2

abstract class Projectile(
        x: Float = 0f,
        y: Float = 0f,
        xSpeed: Float = 0f,
        ySpeed: Float = 0f,
        texture: Texture,
        radius: Float,
        val type: ProjectileType,
        val speed: Float) {

    val bounds: Circle = Circle()
    val sprite: Sprite
    val velocity: Vector2

    var justFired = false

    init {
        bounds.radius = radius
        bounds.x = x
        bounds.y = y
        sprite = Sprite(texture)
        sprite.setPosition(x - bounds.radius, bounds.radius - 4f)
        sprite.setSize(radius * 2, radius * 2)
        velocity = Vector2(xSpeed, ySpeed)
    }

    fun setPosition(x: Float, y: Float) {
        bounds.x = x
        bounds.y = y
        sprite.setPosition(x - bounds.radius, y - bounds.radius)
    }
}