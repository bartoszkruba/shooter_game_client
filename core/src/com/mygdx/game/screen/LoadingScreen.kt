package com.mygdx.game.screen

import com.mygdx.game.Game
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.OrthographicCamera
import ktx.app.KtxScreen
import ktx.graphics.use
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.*
import frontendServer.Server
import ktx.assets.load


class LoadingScreen(private val game: Game,
                    private val batch: SpriteBatch,
                    private val font: BitmapFont,
                    private val assets: AssetManager,
                    private val camera: OrthographicCamera) : KtxScreen {


    override fun show() {
        assets.load<TextureAtlas>("images/player/player.atlas")
        assets.load<TextureAtlas>("images/bazooka/bazooka_explosion.atlas")

        assets.load("images/player.png", Texture::class.java)
        assets.load("images/blood-animation.png", Texture::class.java)
        assets.load("images/blood-onTheFloor.png", Texture::class.java)
        assets.load("images/player/right.png", Texture::class.java)
        assets.load("images/miniMap.png", Texture::class.java)
        assets.load("images/opponentsInMiniMap.png", Texture::class.java)
        assets.load("images/meInMiniMap.png", Texture::class.java)
        assets.load("images/player/left.png", Texture::class.java)
        assets.load("images/player/up.png", Texture::class.java)
        assets.load("images/player/down.png", Texture::class.java)
        assets.load("images/brickwall2.jpg", Texture::class.java)
        assets.load("images/gameOver.png", Texture::class.java)
        assets.load("images/projectile.png", Texture::class.java)
        assets.load("images/healthBar3.png", Texture::class.java)

        assets.load("images/pistol.png", Texture::class.java)
        assets.load("images/machine_gun.png", Texture::class.java)
        assets.load("images/shotgun.png", Texture::class.java)
        assets.load("images/bazooka.png", Texture::class.java)

        assets.load("images/ground.jpg", Texture::class.java)
        assets.load("images/crosshair.png", Texture::class.java)
        assets.load("images/menu/credits.png", Texture::class.java)
        assets.load("images/menu/credits_selected.png", Texture::class.java)
        assets.load("images/menu/options.png", Texture::class.java)
        assets.load("images/menu/options_selected.png", Texture::class.java)
        assets.load("images/menu/quit.png", Texture::class.java)
        assets.load("images/menu/quit_selected.png", Texture::class.java)
        assets.load("images/menu/start.png", Texture::class.java)
        assets.load("images/menu/start_selected.png", Texture::class.java)
        assets.load("images/logo.png", Texture::class.java)
        assets.load("images/enterYourName.png", Texture::class.java)
        assets.load("images/txfUsernameBackground.png", Texture::class.java)
        assets.load("scoreboard/scoreboardBackground.png", Texture::class.java)
        assets.load("scoreboard/scoreboardTable.png", Texture::class.java)

        assets.load("sounds/menu_hover.wav", Sound::class.java)
        assets.load("sounds/menu_select.wav", Sound::class.java)

        assets.load("sounds/pistol_shot.wav", Sound::class.java)
        assets.load("sounds/shotgun_shot.wav", Sound::class.java)
        assets.load("sounds/machine_gun_shot.wav", Sound::class.java)
        assets.load("sounds/bazooka_shot.mp3", Sound::class.java)
        assets.load("sounds/bazooka_explosion.mp3", Sound::class.java)

        assets.load("sounds/damage.mp3", Sound::class.java)
        assets.load("sounds/deathSound.wav", Sound::class.java)
        assets.load("sounds/reload_sound.mp3", Sound::class.java)
        assets.load("sounds/dryfire.mp3", Sound::class.java)

        assets.load("music/ingame_music.ogg", Music::class.java)
        assets.load("music/music.wav", Music::class.java)
        assets.load("music/rain.mp3", Music::class.java)
        assets.load("music/waiting.ogg", Music::class.java)

        assets.load<Texture>("images/splashscreen/foreground.png")
        assets.load<Texture>("images/splashscreen/background.png")
        assets.load<Texture>("images/splashscreen/splashtext.png")
    }

    override fun render(delta: Float) {
        // continue loading our assets
        assets.update()
        camera.update()
        batch.projectionMatrix = camera.combined

        batch.use {
            font.draw(it, "Welcome to Leprechaun Nuclear Invasion 420 (GOTY edition)", 100f, 150f)
            font.draw(it, "Loading assets...", 100f, 100f)
        }

        if (assets.isFinished) {
            game.changeToMenu()
            game.removeScreen<LoadingScreen>()
            dispose()
        }
    }
}