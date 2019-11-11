package com.mygdx.game.util

import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.g2d.Sprite

var x = 0f
var y = 0f

fun inFrustum(camera: Camera, r: Sprite): Boolean {

    x = camera.position.x - camera.viewportWidth / 2f
    y = camera.position.y - camera.viewportHeight / 2f

    if(x > r.x + r.width || r.x > x + camera.viewportWidth){
        return false
    }

    if(y > r.y + r.height || r.y > y + camera.viewportHeight){
        return false
    }

    return true
}