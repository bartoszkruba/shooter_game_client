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

class SplashScreen(private val game: Game,
                    private val batch: SpriteBatch,
                    private val font: BitmapFont,
                    private val assets: AssetManager,
                    private val camera: OrthographicCamera) : KtxScreen {

    private val music = assets.get("music/music.wav", Music::class.java)
    private val background =  Sprite(assets.get("images/splash.jpg", Texture::class.java))
    private val text = Sprite(assets.get("images/splashtext.png", Texture::class.java))

    init {
        background.setPosition(0f,0f)
        background.setBounds(0f,0f, WINDOW_WIDTH, WINDOW_HEIGHT)
        text.setBounds(WINDOW_WIDTH,  WINDOW_HEIGHT, WINDOW_WIDTH/2,WINDOW_HEIGHT/2)
        text.setPosition(WINDOW_WIDTH + text.width, WINDOW_HEIGHT/3)
        music.isLooping = true
        music.volume = 0.3f
        music.play()
    }

    override fun render(delta: Float){
        camera.update()
        if(text.x > WINDOW_WIDTH/3 ){ text.setPosition(text.x-(text.x*0.01f), text.y) }
        batch.projectionMatrix = camera.combined
        batch.use{
               background.draw(batch)
                text.draw(batch)
        }

        if(Gdx.input.isTouched){
            val gameScreen = GameScreen(game, batch, assets, camera, font)
            game.addScreen(gameScreen)
            gameScreen.connectionSocket()
            gameScreen.configSocketEvents()
            game.setScreen(GameScreen::class.java)
        }
    }


}
