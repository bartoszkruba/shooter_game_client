package com.mygdx.game.assets

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import ktx.assets.Asset
import ktx.assets.getAsset
import ktx.assets.load


enum class TextureAtlasAssets(val path: String) {
    Player("images/player/player.atlas"),
    BazookaExplosion("images/bazooka/bazooka_explosion.atlas")
}

enum class TextureAssets(val path: String) {
    Pistol("images/pistol.png"),
    MachineGun("images/machine_gun.png"),
    Shotgun("images/shotgun.png"),
    Bazooka("images/bazooka.png"),

    WeaponUIPanel("images/weapon_panel.png"),

    Logo("images/logo.png"),
    MenuBackground("images/splashscreen/background.png"),
    MenuForeground("images/splashscreen/foreground.png"),

    MenuQuit("images/menu/quit.png"),
    MenuQuitSelected("images/menu/quit_selected.png"),
    MenuCredits("images/menu/credits.png"),
    MenuCreditsSelected("images/menu/credits_selected.png"),
    MenuOptions("images/menu/options.png"),
    MenuOptionsSelected("images/menu/options_selected.png"),
    MenuStart("images/menu/start.png"),
    MenuStartSelected("images/menu/start_selected.png"),
    MenuEnterName("images/enterYourName.png"),
    InputFieldBackground("images/txfUsernameBackground.png"),

    BloodAnimation("images/blood-animation.png"),
    BloodOnTheFloor("images/blood-onTheFloor.png"),

    MiniMap("images/miniMap.png"),
    PlayerOnMap("images/meInMiniMap.png"),
    OpponentOnMap("images/opponentsInMiniMap.png"),

    Ground("images/ground.jpg"),
    Wall("images/brickwall2.jpg"),
    Barrel("images/explosive_barrel.png"),

    MouseCrossHair("images/crosshair.png"),
    HealthBar("images/healthBar3.png"),
    GameOverScreen("images/gameOver.png"),
    ScoreboardBackground("images/scoreboard/scoreboardBackground.png"),
    ScoreboardTable("images/scoreboard/scoreboardTable.png"),

    Projectile("images/projectile.png"),

}

// todo change to mp3
enum class SoundAssets(val path: String) {
    MenuHover("sounds/menu_hover.wav"),
    MenuSelect("sounds/menu_select.wav"),

    PistolShot("sounds/pistol_shot.wav"),
    MachineGunShot("sounds/machine_gun_shot.wav"),
    ShotgunShot("sounds/shotgun_shot.wav"),
    BazookaShot("sounds/bazooka_shot.mp3"),
    Reload("sounds/reload_sound.mp3"),
    DryFire("sounds/dryfire.mp3"),
    Explosion("sounds/bazooka_explosion.mp3"),

    PlayerDamage("sounds/damage.mp3"),
    ZombieDamage("sounds/zombie_damage_sound.mp3"),
    Death("sounds/deathSound.wav")
}

enum class MusicAssets(val path: String) {
    Rain("music/rain.mp3"),
    MenuMusic("music/waiting.ogg"),
    GameMusic("music/ingame_music.ogg")
}

inline fun AssetManager.load(asset: TextureAtlasAssets) = load<TextureAtlas>(asset.path)
inline operator fun AssetManager.get(asset: TextureAtlasAssets) = getAsset<TextureAtlas>(asset.path)

inline fun AssetManager.load(asset: TextureAssets) = load<Texture>(asset.path)
inline operator fun AssetManager.get(asset: TextureAssets) = getAsset<Texture>(asset.path)

inline fun AssetManager.load(asset: SoundAssets) = load<Sound>(asset.path)
inline operator fun AssetManager.get(asset: SoundAssets) = getAsset<Sound>(asset.path)

inline fun AssetManager.load(asset: MusicAssets) = load<Music>(asset.path)
inline operator fun AssetManager.get(asset: MusicAssets) = getAsset<Music>(asset.path)