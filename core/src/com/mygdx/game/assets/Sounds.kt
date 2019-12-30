package com.mygdx.game.assets

import com.badlogic.gdx.assets.AssetManager

class Sounds(assets: AssetManager) {
    val deathSound = assets[SoundAssets.Death]
    val damageSound = assets[SoundAssets.Damage]

    val pistolShotSoundEffect = assets[SoundAssets.PistolShot]
    val shotgunShotSoundEffect = assets[SoundAssets.ShotgunShot]
    val machineGunShotSoundEffect = assets[SoundAssets.MachineGunShot]
    val bazookaShotSoundEffect = assets[SoundAssets.BazookaShot]

    val dryfire = assets[SoundAssets.DryFire]
    val reloadSoundEffect = assets[SoundAssets.Reload]

    val explosionSoundEffect = assets[SoundAssets.Explosion]
}