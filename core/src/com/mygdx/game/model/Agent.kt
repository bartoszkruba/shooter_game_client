package com.mygdx.game.model

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.mygdx.game.settings.*

abstract class Agent(x: Float, y: Float, var isDead: Boolean, var currentHealth: Float, texture: Array<Texture>, healthBarTexture: Texture, var weapon: Weapon,
                     var facingDirectionAngle: Float, var id: String) {
    var sprite: Sprite
    private val spriteUp = Sprite(texture[0])
    private val spriteDown = Sprite(texture[1])
    private val spriteLeft = Sprite(texture[2])
    private val spriteRight = Sprite(texture[3])
    var healthBarSprite: Sprite
    val bounds: Rectangle
    var counter = 85f
    var healthBarSpriteWidth = 182f
    val velocity = Vector2()

    init {
        bounds = Rectangle(x, y, PLAYER_SPRITE_WIDTH, PLAYER_SPRITE_HEIGHT)
        // todo should be changed to loading assets from assetManager
        sprite = spriteRight
        healthBarSprite = Sprite(healthBarTexture)

        healthBarSprite.setSize(healthBarSpriteWidth, HEALTH_BAR_SPRITE_HEIGHT)
        sprite.setSize(PLAYER_SPRITE_WIDTH, PLAYER_SPRITE_HEIGHT)
        sprite.setPosition(x, y)
        healthBarSprite.setPosition((sprite.x - ((200 - 32) / 2)), y)

        healthBarSprite.setOrigin(healthBarSprite.width / 2f, healthBarSprite.height / 2f)
        sprite.setOrigin(sprite.width / 2f, sprite.height / 2f)
    }

    fun setHealthBar(currentHealth: Float, x: Float, y: Float){
        healthBarSprite.setSize(currentHealth, HEALTH_BAR_SPRITE_HEIGHT)
        healthBarSprite.setPosition((x - ((currentHealth - 45)/2)), y)
    }

    fun setPosition(x: Float, y: Float) {
        sprite.setPosition(x, y)
        healthBarSprite.setPosition((x - ((currentHealth - 45)/2)), y)
        bounds.setPosition(x, y)
    }

    fun reduceHealthBarWidth() {
        counter -= 2
        if (healthBarSpriteWidth >= 4) healthBarSpriteWidth -= 5f else healthBarSpriteWidth = 0f
        healthBarSprite.x = sprite.x - counter
        healthBarSprite.setSize(healthBarSpriteWidth, HEALTH_BAR_SPRITE_HEIGHT)
    }

    fun setAngle(newAngle:Float){
        facingDirectionAngle = newAngle
            when {
                facingDirectionAngle >= 337.5 || facingDirectionAngle < 22.5 -> {sprite = spriteRight}
                //angle >= 22.5 && angle < 67.5 -> println("LOOKING UP-RIGHT")
                facingDirectionAngle >= 67.5 && facingDirectionAngle < 112.5 -> sprite = spriteUp
                //angle >= 112.5 && angle < 157.5 -> println("LOOKING UP-LEFT")
                facingDirectionAngle >= 157.5 && facingDirectionAngle < 202.5 -> sprite = spriteLeft
                //angle >= 202.5 && angle < 247.5 -> println("LOOKING DOWN-LEFT")
                facingDirectionAngle >= 247.5 && facingDirectionAngle < 292.5 -> sprite = spriteDown
                //angle >= 292.5 && angle < 337.5 -> println("LOOKING DOWN-RIGHT")
            }
        sprite.setSize(PLAYER_SPRITE_WIDTH, PLAYER_SPRITE_HEIGHT)
        sprite.setPosition(bounds.x, bounds.y)
    }


}