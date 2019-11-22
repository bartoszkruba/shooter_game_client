package com.mygdx.game.model.explosion

import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.utils.TimeUtils
import com.mygdx.game.settings.*

class BarrelExplosion(var x: Float = 0f, var y: Float = 0f, textureAtlas: TextureAtlas) {
    private var created = TimeUtils.millis()
    private var animationTimer = 0L
    private val animation = Animation<Sprite>(BARREL_EXPLOSION_FRAME_DURATION, textureAtlas.createSprites())
    var sprite: Sprite
    var justSpawned = true

    init {
        sprite = animation.getKeyFrame(0f)
        sprite.setSize(BARREL_EXPLOSION_SIZE, BARREL_EXPLOSION_SIZE)
        sprite.setPosition(x - 0.5f * BARREL_EXPLOSION_SIZE, y - 0.5f * BARREL_EXPLOSION_SIZE)
    }

    fun resetTimer() {
        this.created = TimeUtils.millis()
        this.animationTimer = 0L
        this.sprite = animation.getKeyFrame(0f)
    }

    fun animate() {
        animationTimer = TimeUtils.millis() - created
        sprite = animation.getKeyFrame(animationTimer.toFloat() / 1000)
        sprite.setSize(BARREL_EXPLOSION_SIZE, BARREL_EXPLOSION_SIZE)
        sprite.setPosition(x - 0.5f * BARREL_EXPLOSION_SIZE, y - 0.5f * BARREL_EXPLOSION_SIZE)
    }

    fun isFinished() = animationTimer > BARREL_EXPLOSION_FRAME_DURATION * BARREL_EXPLOSION_FRAMES * 1000
}