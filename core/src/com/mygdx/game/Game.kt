package com.mygdx.game

import backendServer.Server
import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.mygdx.game.screen.GameScreen
import com.mygdx.game.screen.LoadingScreen
import com.mygdx.game.settings.WINDOW_HEIGHT
import com.mygdx.game.settings.WINDOW_WIDTH
import io.socket.client.IO
import io.socket.client.Socket
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.inject.Context

class Game : KtxGame<KtxScreen>() {
    val assets = AssetManager()
    private val context = Context()
    override fun create() {
        context.register {
            bindSingleton(this@Game)
            bindSingleton(SpriteBatch())
            bindSingleton(BitmapFont())
            bindSingleton(AssetManager())
            bindSingleton(OrthographicCamera().apply { setToOrtho(false, WINDOW_WIDTH, WINDOW_HEIGHT) })
            addScreen(LoadingScreen(inject(), inject(), inject(), inject(), inject()))
            //val game = GameScreen(inject(), inject(), inject(), inject(), inject())
           // addScreen(game)
            //game.connectionSocket()
            //game.configSocketEvents()
        }
        setScreen<LoadingScreen>()
        super.create()
    }

    override fun dispose() {
        context.dispose()
        super.dispose()
    }
}