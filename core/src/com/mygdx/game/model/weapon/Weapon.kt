package com.mygdx.game.model.weapon

import com.badlogic.gdx.utils.TimeUtils

abstract class Weapon(private val reloadTime: Long, val maxBulletsInChamber: Int, val type: String) {
    private var lastShoot = 0L

    var bulletsInChamber = maxBulletsInChamber

    fun canShoot() = TimeUtils.millis() - lastShoot > reloadTime

    fun shoot() = if (canShoot()) {
        lastShoot = TimeUtils.millis()
        true
    } else false

}