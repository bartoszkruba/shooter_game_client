package com.mygdx.game.assets

import com.badlogic.gdx.assets.AssetManager

class Textures(assets: AssetManager) {
    val projectileTexture = assets[TextureAssets.Projectile]
    val wallTexture = assets[TextureAssets.Wall]
    val healthBarTexture = assets[TextureAssets.HealthBar]

    val pistolTexture = assets[TextureAssets.Pistol]
    val machineGunTexture = assets[TextureAssets.MachineGun]
    val shotgunTexture = assets[TextureAssets.Shotgun]
    val bazookaTexture = assets[TextureAssets.Bazooka]

    val groundTexture = assets[TextureAssets.Ground]
    val bloodOnTheFloorTexture = assets[TextureAssets.BloodOnTheFloor]
    val explosiveBarrelTexture = assets[TextureAssets.Barrel]
}