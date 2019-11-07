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
        assets.load("sounds/pistol_shot.wav", Sound::class.java)
        assets.load("sounds/reload_sound.mp3", Sound::class.java)
        assets.load("music/music.wav", Music::class.java)
    }

    override fun render(delta: Float) {
        // continue loading our assets
        assets.update()
        camera.update()
        batch.projectionMatrix = camera.combined

        batch.use {
            font.draw(it, "Welcome to Leprechaun Nuclear Invasion 420 (GOTY edition)", 100f, 150f)
            if (assets.isFinished) {
                font.draw(it, "Tap anywhere to begin!", 100f, 100f)
            } else {
                font.draw(it, "Loading assets...", 100f, 100f)
            }
        }

        if (Gdx.input.isTouched && assets.isFinished) {
            val gameScreen = GameScreen(game, batch, assets, camera, font)
            game.addScreen(gameScreen)
            gameScreen.connectionSocket()
            gameScreen.configSocketEvents()
            game.setScreen<GameScreen>()
            game.removeScreen<LoadingScreen>()
            dispose()

        }
    }
}