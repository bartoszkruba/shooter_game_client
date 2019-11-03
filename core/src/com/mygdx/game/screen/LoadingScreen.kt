package com.mygdx.game.screen

import com.mygdx.game.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetManager
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



class LoadingScreen(private val game: Game,
                    private val batch: SpriteBatch,
                    private val font: BitmapFont,
                    private val assets: AssetManager,
                    private val camera: OrthographicCamera) : KtxScreen {


    override fun show() {
        TextureAtlasAssets.values().forEach { assets.load(it) }
        assets.load("images/leprechaun.png", Texture::class.java)
        assets.load("images/standard_projectile.jpg", Texture::class.java)
        assets.load("images/wall.png", Texture::class.java)
        assets.load("images/standard_projectile.jpg", Texture::class.java)
    }

    override fun render(delta: Float) {
        // continue loading our assets
        assets.update()
        camera.update()
        batch.projectionMatrix = camera.combined

        batch.use {
            font.draw(it, "Welcome to... will someone name this game already!?! ", 100f, 150f)
            if (assets.isFinished) {
                font.draw(it, "Tap anywhere to begin!", 100f, 100f)
            } else {
                font.draw(it, "Loading assets...", 100f, 100f)
            }
        }

        if (Gdx.input.isTouched && assets.isFinished) {
            val gameScreen = GameScreen(game, batch, assets, camera)
            game.addScreen(gameScreen)
            gameScreen.connectionSocket()
            gameScreen.configSocketEvents()
            game.setScreen<GameScreen>()
            game.removeScreen<LoadingScreen>()
            dispose()

        }
    }
}