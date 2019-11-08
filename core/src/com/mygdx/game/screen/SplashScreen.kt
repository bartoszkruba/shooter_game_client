package com.mygdx.game.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.mygdx.game.Game
import com.mygdx.game.settings.WINDOW_HEIGHT
import com.mygdx.game.settings.WINDOW_WIDTH
import ktx.app.KtxScreen
import ktx.graphics.use

class SplashScreen(
        val game: Game,
        private val batch: SpriteBatch,
        private val assets: AssetManager,
        private val camera: OrthographicCamera,
        private val font: BitmapFont) : KtxScreen {

    private val rainMusic = assets.get<Music>("music/rain.mp3")
    private val backgroundMusic = assets.get<Music>("music/waiting.ogg")
    private val background: Sprite = Sprite(assets.get<Texture>("images/splashscreen/background.png"))
    private val foreground: Sprite = Sprite(assets.get<Texture>("images/splashscreen/foreground.png"))
    private val text = Sprite(assets.get<Texture>("images/splashscreen/splashtext.png"))
    private var textCounter = 0

    init {
        foreground.setPosition(0f,0f)
        foreground.setBounds(-WINDOW_WIDTH*2,0f, WINDOW_WIDTH*3, WINDOW_HEIGHT)
        background.setBounds(0f,0f, WINDOW_WIDTH*3, WINDOW_HEIGHT)
        background.setPosition(0f,0f)
        text.setBounds(WINDOW_WIDTH,  WINDOW_HEIGHT, WINDOW_WIDTH/2,WINDOW_HEIGHT/2)
        text.setPosition(WINDOW_WIDTH*3, WINDOW_HEIGHT/2)
    }

    override fun render(delta: Float) {
        foreground.setPosition(foreground.x + 0.1f, foreground.y)
        background.setPosition(background.x - 0.03f, background.y)
        if (text.x > WINDOW_WIDTH / 5) {
            text.setPosition(text.x - (text.x * 0.01f), text.y)
        }
        camera.update()
        batch.projectionMatrix = camera.combined

        batch.use {
            background.draw(it)
            foreground.draw(it)
            text.draw(it)
            font.draw(it, "PRESS ANY BUTTON TO CONTINUE", WINDOW_WIDTH / 4, WINDOW_HEIGHT / 2)
        }

        if(Gdx.input.isTouched){
            game.changeToMenu()
        }
    }
    override fun show() {
        super.show()
        rainMusic.isLooping = true
        rainMusic.play()
        backgroundMusic.isLooping = true
        backgroundMusic.play()
    }


}
