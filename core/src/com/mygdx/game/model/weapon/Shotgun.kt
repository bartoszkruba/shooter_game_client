package com.mygdx.game.model.weapon

import com.mygdx.game.model.projectile.ProjectileType
import com.mygdx.game.settings.SHOTGUN_BULLETS_IN_CHAMBER
import com.mygdx.game.settings.SHOTGUN_RELOAD_TIME

class Shotgun : Weapon(SHOTGUN_RELOAD_TIME, SHOTGUN_BULLETS_IN_CHAMBER, ProjectileType.SHOTGUN)