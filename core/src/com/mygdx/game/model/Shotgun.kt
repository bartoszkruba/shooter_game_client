package com.mygdx.game.model

import com.mygdx.game.settings.SHOTGUN_BULLETS_IN_CHAMBER
import com.mygdx.game.settings.SHOTGUN_RELOAD_TIME

class Shotgun : Weapon(SHOTGUN_RELOAD_TIME, SHOTGUN_BULLETS_IN_CHAMBER, ProjectileType.SHOTGUN)