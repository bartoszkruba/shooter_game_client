package com.mygdx.game.model

class Player(
        x: Float,
        y: Float,
        weapon: Weapon = Pistol(),
        facingDirectionAngle: Float = 0f) : Agent(x, y, weapon, facingDirectionAngle) {

    fun canShoot() = weapon.canShoot()
    fun shoot() = weapon.shoot()
}