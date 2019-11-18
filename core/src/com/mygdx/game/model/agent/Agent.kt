package com.mygdx.game.model.agent

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.*
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.mygdx.game.settings.*
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode
import com.mygdx.game.model.weapon.Weapon


abstract class Agent(x: Float, y: Float, var name: String, var kills: Int, var deaths: Int, var isDead: Boolean,
                     var currentHealth: Float, var gotShot: Boolean, textureAtlas: TextureAtlas,
                     healthBarTexture: Texture, var weapon: Weapon, var facingDirectionAngle: Float,
                     var id: String, spritecolor: Color) {


    private val animateRight = Animation<Sprite>(PLAYER_FRAME_DURATION, textureAtlas.createSprites("right"), PlayMode.LOOP_REVERSED)
    private val animateLeft = Animation<Sprite>(PLAYER_FRAME_DURATION, textureAtlas.createSprites("left"), PlayMode.LOOP_REVERSED)
    private val animateUp = Animation<Sprite>(PLAYER_FRAME_DURATION, textureAtlas.createSprites("up"), PlayMode.LOOP_REVERSED)
    private val animateDown = Animation<Sprite>(PLAYER_FRAME_DURATION, textureAtlas.createSprites("down"), PlayMode.LOOP_REVERSED)
    private val animateUpLeft = Animation<Sprite>(PLAYER_FRAME_DURATION, textureAtlas.createSprites("upleft"), PlayMode.LOOP_REVERSED)
    private val animateDownLeft = Animation<Sprite>(PLAYER_FRAME_DURATION, textureAtlas.createSprites("downleft"), PlayMode.LOOP_REVERSED)
    private val animateUpRight = Animation<Sprite>(PLAYER_FRAME_DURATION, textureAtlas.createSprites("upright"), PlayMode.LOOP_REVERSED)
    private val animateDownRight = Animation<Sprite>(PLAYER_FRAME_DURATION, textureAtlas.createSprites("downright"), PlayMode.LOOP_REVERSED)

    var sprite: Sprite
    val spritecolor = spritecolor
    private var stateTime = 0f
    private var currentAnimation: Animation<Sprite>
    val healthBarSprite = Sprite(healthBarTexture)
    val bounds: Rectangle = Rectangle(x, y, PLAYER_SPRITE_WIDTH, PLAYER_SPRITE_HEIGHT)
    private var counter = 85f
    private var healthBarSpriteWidth = 182f
    var isMoving = false
    val velocity = Vector2()

    init {
        currentAnimation = animateDown
        sprite = currentAnimation.getKeyFrame(0f)
        sprite.setSize(PLAYER_SPRITE_WIDTH, PLAYER_SPRITE_HEIGHT)
        sprite.setOrigin(sprite.width / 2f, sprite.height / 2f)
        sprite.setPosition(x, y)
        sprite.color = spritecolor
        healthBarSprite.setSize(healthBarSpriteWidth, HEALTH_BAR_SPRITE_HEIGHT)
        healthBarSprite.setPosition((sprite.x - ((200 - 32) / 2)), y)
        healthBarSprite.setOrigin(healthBarSprite.width / 2f, healthBarSprite.height / 2f)
    }

    private fun animate() {
        sprite = currentAnimation.getKeyFrame(stateTime)
        sprite.color = spritecolor
        sprite.setSize(PLAYER_SPRITE_WIDTH, PLAYER_SPRITE_HEIGHT)
        sprite.setOrigin(sprite.width / 2f, sprite.height / 2f)
        sprite.setPosition(bounds.x, bounds.y)
    }

    fun setHealthBar(currentHealth: Float, x: Float, y: Float) {
        healthBarSprite.setSize(currentHealth, HEALTH_BAR_SPRITE_HEIGHT)
        healthBarSprite.setPosition((x - ((currentHealth - 45) / 2)), y)
    }

    fun setPosition(newX: Float, newY: Float) {
        if ( isMoving ) stateTime += Gdx.graphics.deltaTime else stateTime = currentAnimation.animationDuration
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