package com.mygdx.game.screen

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite

class Blood(bloodOnTheFloorTexture: Texture, bloodOnTheFloorOpponentX: Float, bloodOnTheFloorOpponentY: Float, transparent: Float, gotShot: Boolean) {
    var transparent = transparent
    var x = bloodOnTheFloorOpponentX
    var y = bloodOnTheFloorOpponentY
    var gotShot = gotShot
    var bloodOnTheFloorSprite = Sprite(bloodOnTheFloorTexture)

    init {
        bloodOnTheFloorSprite.setPosition(x, y)
        bloodOnTheFloorSprite.setSize(150f, 55f)
    }

    fun changeTransparent(){
        this.transparent -= 0.001f
    }
}
