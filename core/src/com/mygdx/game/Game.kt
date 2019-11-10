package com.mygdx.game

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.mygdx.game.screen.GameScreen
import com.mygdx.game.screen.LoadingScreen
import com.mygdx.game.screen.MenuScreen
import com.mygdx.game.settings.WINDOW_HEIGHT
import com.mygdx.game.settings.WINDOW_WIDTH
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.inject.Context

class Game : KtxGame<KtxScreen>() {
    private val context = Context()
    private val assets = AssetManager()
    override fun create() {
        context.register {
            bindSingleton(this@Game)
            bindSingleton(SpriteBatch())
            bindSingleton(BitmapFont())
            bindSingleton(assets)
            bindSingleton(OrthographicCamera().apply { setToOrtho(false, WINDOW_WIDTH, WINDOW_HEIGHT) })
            addScreen(LoadingScreen(inject(), inject(), inject(), inject(), inject()))
        }
        setScreen<LoadingScreen>()
        super.create()
    }

    override fun dispose() {
        context.dispose()
        assets.dispose()
        super.dispose()
    }

    fun changeToMenu() {
        addScreen(MenuScreen(context.inject(), context.inject(), context.inject(), context.inject(),context.inject()))
        setScreen<MenuScreen>()
    }

    fun changeToGame() {
        val gameScreen = GameScreen(this, context.inject(), context.inject(), context.inject(), context.inject())

        gameScreen.connectionSocket()
        gameScreen.configSocketEvents()
        addScreen(gameScreen)
        removeScreen<MenuScreen>()
        setScreen<GameScreen>()
    }
}