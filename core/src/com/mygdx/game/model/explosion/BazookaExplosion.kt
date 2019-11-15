package com.mygdx.game.model.explosion

import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.utils.TimeUtils
import com.mygdx.game.settings.BAZOOKA_EXPLOSION_FRAMES
import com.mygdx.game.settings.BAZOOKA_EXPLOSION_FRAME_DURATION
import com.mygdx.game.settings.BAZOOKA_EXPLOSION_SIZE

class BazookaExplosion(var x: Float = 0f, var y: Float = 0f, textureAtlas: TextureAtlas) {

    private var created = TimeUtils.millis()
    private var animationTimer = 0L
    private val animation = Animation<Sprite>(BAZOOKA_EXPLOSION_FRAME_DURATION, textureAtlas.createSprites())
    var sprite: Sprite

    init {
        sprite = animation.getKeyFrame(0f)
        sprite.setSize(BAZOOKA_EXPLOSION_SIZE, BAZOOKA_EXPLOSION_SIZE)
        sprite.setPosition(x - 0.5f * BAZOOKA_EXPLOSION_SIZE, y - 0.5f * BAZOOKA_EXPLOSION_SIZE)
    }

    fun resetTimer() {
        this.created = TimeUtils.millis()
        this.animationTimer = 0L
        this.sprite = animation.getKeyFrame(0f)
    }

    fun animate() {
        animationTimer = TimeUtils.millis() - created
        sprite = animation.getKeyFrame(animationTimer.toFloat() / 1000)
        sprite.setSize(BAZOOKA_EXPLOSION_SIZE, BAZOOKA_EXPLOSION_SIZE)
        sprite.setPosition(x - 0.5f * BAZOOKA_EXPLOSION_SIZE, y - 0.5f * BAZOOKA_EXPLOSION_SIZE)
    }

    fun isFinished() = animationTimer > BAZOOKA_EXPLOSION_FRAME_DURATION * BAZOOKA_EXPLOSION_FRAMES * 1000
}