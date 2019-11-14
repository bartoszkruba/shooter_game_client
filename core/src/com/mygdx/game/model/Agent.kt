package com.mygdx.game.model

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.*
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.mygdx.game.settings.*
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode


abstract class Agent(x: Float, y: Float, var name: String, var isDead: Boolean, var currentHealth: Float,
                     var gotShot: Boolean, textureAtlas: TextureAtlas, healthBarTexture: Texture, var weapon: Weapon,
                     var facingDirectionAngle: Float, var id: String) {

    var sprite: Sprite
    var stateTime = 0f
    private val animateRight = Animation<Sprite>(PLAYER_FRAME_DURATION, textureAtlas.createSprites("right"), PlayMode.LOOP_REVERSED)
    private val animateLeft = Animation<Sprite>(PLAYER_FRAME_DURATION, textureAtlas.createSprites("left"), PlayMode.LOOP_REVERSED)
    private val animateUp = Animation<Sprite>(PLAYER_FRAME_DURATION, textureAtlas.createSprites("up"), PlayMode.LOOP_REVERSED)
    private val animateDown = Animation<Sprite>(PLAYER_FRAME_DURATION, textureAtlas.createSprites("down"), PlayMode.LOOP_REVERSED)
    private val animateUpLeft = Animation<Sprite>(PLAYER_FRAME_DURATION, textureAtlas.createSprites("upleft"), PlayMode.LOOP_REVERSED)
    private val animateDownLeft = Animation<Sprite>(PLAYER_FRAME_DURATION, textureAtlas.createSprites("downleft"), PlayMode.LOOP_REVERSED)
    private val animateUpRight = Animation<Sprite>(PLAYER_FRAME_DURATION, textureAtlas.createSprites("upright"), PlayMode.LOOP_REVERSED)
    private val animateDownRight = Animation<Sprite>(PLAYER_FRAME_DURATION, textureAtlas.createSprites("downright"), PlayMode.LOOP_REVERSED)

    var currentAnimation: Animation<Sprite>
    val healthBarSprite = Sprite(healthBarTexture)
    val bounds: Rectangle
    var counter = 85f
    var healthBarSpriteWidth = 182f
    val velocity = Vector2()

    init {
        bounds = Rectangle(x, y, PLAYER_SPRITE_WIDTH, PLAYER_SPRITE_HEIGHT)
        currentAnimation = animateDown
        sprite = currentAnimation.getKeyFrame(0f)
        sprite.setSize(PLAYER_SPRITE_WIDTH, PLAYER_SPRITE_HEIGHT)
        sprite.setOrigin(sprite.width / 2f, sprite.height / 2f)
        sprite.setPosition(x, y)
        healthBarSprite.setSize(healthBarSpriteWidth, HEALTH_BAR_SPRITE_HEIGHT)
        healthBarSprite.setPosition((sprite.x - ((200 - 32) / 2)), y)
        healthBarSprite.setOrigin(healthBarSprite.width / 2f, healthBarSprite.height / 2f)
    }

    fun animate() {
        sprite = currentAnimation.getKeyFrame(stateTime)
        sprite.setSize(PLAYER_SPRITE_WIDTH, PLAYER_SPRITE_HEIGHT)
        sprite.setOrigin(sprite.width / 2f, sprite.height / 2f)
        sprite.setPosition(bounds.x, bounds.y)
    }

    fun setHealthBar(currentHealth: Float, x: Float, y: Float) {
        healthBarSprite.setSize(currentHealth, HEALTH_BAR_SPRITE_HEIGHT)
        healthBarSprite.setPosition((x - ((currentHealth - 45) / 2)), y)
    }

    fun setPosition(newX: Float, newY: Float) {
        if ( newX != sprite.x || newY !=  sprite.y  ){
            stateTime += Gdx.graphics.deltaTime
        }
        animate()
        sprite.setPosition(newX, newY)
        bounds.setPosition(newX, newY)
        healthBarSprite.setPosition((newX - ((currentHealth - 45) / 2)), newY)
    }

    fun reduceHealthBarWidth() {
        counter -= 2
        if (healthBarSpriteWidth >= 4) healthBarSpriteWidth -= 5f else healthBarSpriteWidth = 0f
        healthBarSprite.x = sprite.x - counter
        healthBarSprite.setSize(healthBarSpriteWidth, HEALTH_BAR_SPRITE_HEIGHT)
    }

    fun setAngle(newAngle: Float) {
        if (newAngle != facingDirectionAngle) {
            facingDirectionAngle = newAngle
            when {
                     facingDirectionAngle >= 337.5 || facingDirectionAngle < 22.5 -> {
                         currentAnimation = animateRight
                     }
                     facingDirectionAngle >= 22.5 && facingDirectionAngle < 67.5 -> {
                         currentAnimation = animateUpRight
                     }
                     facingDirectionAngle >= 67.5 && facingDirectionAngle < 112.5 -> {
                         currentAnimation = animateUp
                     }
                    facingDirectionAngle >= 112.5 && facingDirectionAngle < 157.5 -> {
                        currentAnimation = animateUpLeft
                    }
                     facingDirectionAngle >= 157.5 && facingDirectionAngle < 202.5 -> {
                         currentAnimation = animateLeft
                     }
                    facingDirectionAngle >= 202.5 && facingDirectionAngle < 247.5 -> {
                        currentAnimation = animateDownLeft
                    }
                     facingDirectionAngle >= 247.5 && facingDirectionAngle < 292.5 -> {
                         currentAnimation = animateDown
                     }
                     facingDirectionAngle >= 292.5 && facingDirectionAngle < 337.5 -> {
                         currentAnimation = animateDownRight
                        }
                     }
        }
    }
}