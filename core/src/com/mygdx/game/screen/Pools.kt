package com.mygdx.game.screen

import com.mygdx.game.assets.Atlases
import com.mygdx.game.assets.Textures
import com.mygdx.game.client.Client
import com.mygdx.game.model.explosion.BarrelExplosion
import com.mygdx.game.model.explosion.BazookaExplosion
import com.mygdx.game.model.obstacles.ExplosiveBarrel
import com.mygdx.game.model.pickup.BazookaPickup
import com.mygdx.game.model.pickup.MachineGunPickup
import com.mygdx.game.model.pickup.PistolPickup
import com.mygdx.game.model.pickup.ShotgunPickup
import com.mygdx.game.model.projectile.BazookaProjectile
import com.mygdx.game.model.projectile.MachineGunProjectile
import com.mygdx.game.model.projectile.PistolProjectile
import com.mygdx.game.model.projectile.ShotgunProjectile
import ktx.assets.pool

class Pools(textures: Textures, atlases: Atlases) {
    val pistolProjectilePool = pool { PistolProjectile(texture = textures.projectileTexture) }
    val machineGunProjectilePool = pool { MachineGunProjectile(texture = textures.projectileTexture) }
    val shotgunProjectilePool = pool { ShotgunProjectile(texture = textures.projectileTexture) }
    val bazookaProjectilePool = pool { BazookaProjectile(texture = textures.projectileTexture) }

    val bazookaExplosionPool = pool { BazookaExplosion(textureAtlas = atlases.bazookaExplosionAtlas) }
    val barrelExplosionPool = pool { BarrelExplosion(textureAtlas = atlases.bazookaExplosionAtlas) }

    val pistolPickupPool = pool { PistolPickup(texture = textures.pistolTexture) }
    val machineGunPickupPool = pool { MachineGunPickup(texture = textures.machineGunTexture) }
    val shotgunPickupPool = pool { ShotgunPickup(texture = textures.shotgunTexture) }
    val bazookaPickupPool = pool { BazookaPickup(texture = textures.bazookaTexture) }

    val explosiveBarrelPool = pool { ExplosiveBarrel(texture = textures.explosiveBarrelTexture) }
}