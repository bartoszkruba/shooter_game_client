package com.mygdx.game.model.weapon

import com.mygdx.game.model.projectile.ProjectileType
import com.mygdx.game.settings.PISTOL_BULLETS_IN_CHAMBER
import com.mygdx.game.settings.PISTOL_RELOAD_TIME

class Pistol : Weapon(PISTOL_RELOAD_TIME, PISTOL_BULLETS_IN_CHAMBER, ProjectileType.PISTOL)