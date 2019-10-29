package com.mygdx.game

import backendServer.Server
import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.mygdx.game.screen.GameScreen
import com.mygdx.game.settings.WINDOW_HEIGHT
import com.mygdx.game.settings.WINDOW_WIDTH
import io.socket.client.IO
import io.socket.client.Socket
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.inject.Context

class Game : KtxGame<KtxScreen>() {
    private val backendServer = Server()
    private val context = Context()

    override fun create() {
        backendServer.connectionSocket()
        backendServer.configSocketEvents()
        context.register {
            bindSingleton(this@Game)
            bindSingleton(SpriteBatch())
            bindSingleton(BitmapFont())
            bindSingleton(OrthographicCamera().apply { setToOrtho(false, WINDOW_WIDTH, WINDOW_HEIGHT) })

            addScreen(GameScreen(inject(), inject(), inject(), inject()))
        }

        changeToGameScreen()

        super.create()
    }

    fun changeToGameScreen() = setScreen<GameScreen>()
}
