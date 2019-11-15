package com.mygdx.game.model.weapon

import com.mygdx.game.model.projectile.ProjectileType
import com.mygdx.game.settings.BAZOOKA_BULLETS_IN_CHAMBER
import com.mygdx.game.settings.BAZOOKA_RELOAD_TIME

class Bazooka : Weapon(BAZOOKA_RELOAD_TIME, BAZOOKA_BULLETS_IN_CHAMBER, ProjectileType.BAZOOKA)