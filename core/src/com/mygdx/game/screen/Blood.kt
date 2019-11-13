package com.mygdx.game.screen

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite

class Blood(bloodOnTheFloorTexture: Texture,
            bloodOnTheFloorOpponentX: Float = 0f,
            bloodOnTheFloorOpponentY: Float =0f,
            transparent: Float = 0f,
            gotShot: Boolean = false) {

    var transparent = transparent
    var x = bloodOnTheFloorOpponentX
    var y = bloodOnTheFloorOpponentY
    var gotShot = gotShot
    var bloodOnTheFloorSprite = Sprite(bloodOnTheFloorTexture)

    init {
        bloodOnTheFloorSprite.setSize(150f, 55f)
    }

    fun changeTransparent(){
        this.transparent -= 0.001f
    }
}
