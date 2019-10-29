package com.mygdx.game.model

import com.badlogic.gdx.utils.TimeUtils

abstract class Weapon(private val reloadTime: Long) {
    private var lastShoot = 0L

    fun canShoot() = TimeUtils.millis() - lastShoot > reloadTime

    fun shoot() = if (canShoot()) {
        lastShoot = TimeUtils.millis()
        true
    } else false

}