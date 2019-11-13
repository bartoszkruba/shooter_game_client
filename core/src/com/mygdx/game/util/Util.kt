package com.mygdx.game.util

import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.math.Circle
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.utils.Array
import com.mygdx.game.model.Wall
import com.mygdx.game.settings.*

var x = 0f
var y = 0f

fun inFrustum(camera: Camera, r: Sprite): Boolean {

    x = camera.position.x - camera.viewportWidth / 2f
    y = camera.position.y - camera.viewportHeight / 2f

    if (x > r.x + r.width || r.x > x + camera.viewportWidth) {
        return false
    }

    if (y > r.y + r.height || r.y > y + camera.viewportHeight) {
        return false
    }

    return true
}

fun generateWallMatrix(): HashMap<String, Array<Wall>> {
    val matrix = HashMap<String, Array<Wall>>()

    for (i in 0 until (MAP_WIDTH / ZONE_SIZE) + 1) {
        for (j in 0 until (MAP_HEIGHT / ZONE_SIZE) + 1) {
            matrix["_${i}_${j}"] = Array()
        }
    }

    return matrix
}

fun getZonesForRectangle(rectangle: Rectangle): Array<String> {
    val zones = Array<String>()

    for (i in (rectangle.x / ZONE_SIZE).toInt() until ((rectangle.x + WALL_SPRITE_WIDTH) / ZONE_SIZE).toInt()) {
        for (j in (rectangle.y / ZONE_SIZE).toInt() until ((rectangle.y + WALL_SPRITE_HEIGHT) / ZONE_SIZE).toInt()) {
            zones.add("_${i}_${j}")
            println("_${i}_${j}")
        }
    }

    return zones
}


fun getZonesForCircle(circle: Circle): Array<String> {
    var minX = 0f
    var minY = 0f
    var maxX = 0f
    var maxY = 0f

    minX = circle.x - circle.radius
    minY = circle.y - circle.radius
    maxX = circle.x + circle.radius
    maxY = circle.y + circle.radius

    val zones = Array<String>()

    for (i in (minX / ZONE_SIZE).toInt() until (maxX / ZONE_SIZE).toInt() + 1) {
        for (j in (minY / ZONE_SIZE).toInt() until (maxY / ZONE_SIZE).toInt() + 1) {
            zones.add("_${i}_${j}")
        }
    }

    return zones
}