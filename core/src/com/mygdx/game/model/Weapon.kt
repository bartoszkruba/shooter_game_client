package com.mygdx.game.model

import com.badlogic.gdx.utils.TimeUtils

abstract class Weapon(private val reloadTime: Long, maxBulletsInChamber: Int) {
    private var lastShoot = 0L

    // todo this is no longer used anywhere - remove

    var bulletsInChamber = maxBulletsInChamber

    fun canShoot() = TimeUtils.millis() - lastShoot > reloadTime

    fun shoot() = if (canShoot()) {
        lastShoot = TimeUtils.millis()
        true
    } else false

}