package com.mygdx.game.assets

import com.badlogic.gdx.assets.AssetManager

class Sounds(assets: AssetManager) {
    val deathSound = assets[SoundAssets.Death]
    val playerDamage = assets[SoundAssets.PlayerDamage]
    val zombieDamage = assets[SoundAssets.ZombieDamage]
    val zombieBite = assets[SoundAssets.ZombieBite]
    val zombieMoan = assets[SoundAssets.ZombieMoan]

    val pistolShotSoundEffect = assets[SoundAssets.PistolShot]
    val shotgunShotSoundEffect = assets[SoundAssets.ShotgunShot]
    val machineGunShotSoundEffect = assets[SoundAssets.MachineGunShot]
    val bazookaShotSoundEffect = assets[SoundAssets.BazookaShot]

    val dryfire = assets[SoundAssets.DryFire]
    val reloadSoundEffect = assets[SoundAssets.Reload]

    val explosionSoundEffect = assets[SoundAssets.Explosion]
}