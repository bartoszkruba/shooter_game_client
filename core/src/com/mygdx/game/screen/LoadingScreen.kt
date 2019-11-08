package com.mygdx.game.screen

import com.mygdx.game.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.assets.loaders.FileHandleResolver
import com.badlogic.gdx.assets.loaders.MusicLoader
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.mygdx.game.assets.TextureAtlasAssets
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.mygdx.game.assets.load
import ktx.app.KtxScreen
import ktx.graphics.use
import com.badlogic.gdx.graphics.Texture
import ktx.assets.load


class LoadingScreen(private val game: Game,
                    private val batch: SpriteBatch,
                    private val font: BitmapFont,
                    private val assets: AssetManager,
                    private val camera: OrthographicCamera) : KtxScreen {


    override fun show() {
        //TextureAtlasAssets.values().forEach { assets.load(it) }
        assets.load("images/player.png", Texture::class.java)
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
        assets.load("images/ground.jpg", Texture::class.java)

        assets.load("images/menu/menu_credits.png", Texture::class.java)
        assets.load("images/menu/menu_credits_selected.jpg", Texture::class.java)
        assets.load("images/menu/menu_options.png", Texture::class.java)
        assets.load("images/menu/menu_options_selected.jpg", Texture::class.java)
        assets.load("images/menu/menu_quit.png", Texture::class.java)
        assets.load("images/menu/menu_quit_selected.jpg", Texture::class.java)
        assets.load("images/menu/menu_start_game.png", Texture::class.java)
        assets.load("images/menu/menu_start_game_selected.jpg", Texture::class.java)


        assets.load("images/menu/far-buildings.png", Texture::class.java)
        assets.load("images/menu/back-buildings.png", Texture::class.java)

        assets.load("sounds/pistol_shot.wav", Sound::class.java)
        assets.load("sounds/reload_sound.mp3", Sound::class.java)

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
            val splashScreen = SplashScreen(game, batch, font, assets, camera)
            game.addScreen(splashScreen)
            game.setScreen<SplashScreen>()
            game.removeScreen<LoadingScreen>()
            dispose()
        }
    }
}