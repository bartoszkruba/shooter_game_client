package com.mygdx.game.model.weapon

import com.mygdx.game.model.projectile.ProjectileType
import com.mygdx.game.settings.MACHINE_FUN_RELOAD_TIME
import com.mygdx.game.settings.MACHINE_GUN_BULLETS_IN_CHAMBER

class MachineGun : Weapon(MACHINE_FUN_RELOAD_TIME, MACHINE_GUN_BULLETS_IN_CHAMBER, ProjectileType.MACHINE_GUN)