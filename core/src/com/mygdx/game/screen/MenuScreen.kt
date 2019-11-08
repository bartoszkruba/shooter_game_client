package com.mygdx.game.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.mygdx.game.settings.WINDOW_WIDTH
import com.mygdx.game.ui.MenuChoice
import com.badlogic.gdx.utils.Array
import com.mygdx.game.settings.WINDOW_HEIGHT
import ktx.app.KtxScreen
import ktx.graphics.use

class MenuScreen(assets: AssetManager, private val batch: SpriteBatch, private val camera: OrthographicCamera) : KtxScreen {

    private val startGameChoice = "sg"
    private val quitChoice = "q"
    private val optionsChoice = "op"
    private val creditsChoice = "cr"

    private val mousePosition = Vector2()

    private val menuChoices = Array<MenuChoice>()

    private val rainMusic = assets.get<Music>("music/rain.mp3")
    private val backgroundMusic = assets.get<Music>("music/waiting.ogg")

    private val backgroundOne: Sprite = Sprite(assets.get<Texture>("images/menu/far-buildings.png"))

    private val backgroundTwo: Sprite = Sprite(assets.get<Texture>("images/menu/back-buildings.png"))

    init {

        backgroundOne.setSize(WINDOW_WIDTH, WINDOW_HEIGHT)
        backgroundOne.setPosition(0f, 0f)
        backgroundTwo.setSize(WINDOW_WIDTH, WINDOW_HEIGHT)
        backgroundTwo.setPosition(0f, 0f)

        menuChoices.add(MenuChoice(
                assets.get<Texture>("images/menu/menu_quit_selected.jpg"),
                assets.get<Texture>("images/menu/menu_quit.png"),
                quitChoice
        ))

        menuChoices.add(MenuChoice(
                assets.get<Texture>("images/menu/menu_credits_selected.jpg"),
                assets.get<Texture>("images/menu/menu_credits.png"),
                creditsChoice
        ))

        menuChoices.add(MenuChoice(
                assets.get<Texture>("images/menu/menu_options_selected.jpg"),
                assets.get<Texture>("images/menu/menu_options.png"),
                optionsChoice
        ))

        menuChoices.add(MenuChoice(
                assets.get<Texture>("images/menu/menu_start_game_selected.jpg"),
                assets.get<Texture>("images/menu/menu_start_game.png"),
                startGameChoice
        ))
        val x = (WINDOW_WIDTH - menuChoices[0].sprite.width) / 2
        var y = 100f

        menuChoices.forEach {
            it.setPosition(x, y)
            y += it.sprite.height + 50
        }
    }

    override fun render(delta: Float) {
        camera.update()
        batch.projectionMatrix = camera.combined

        getMousePosInGameWorld()
        checkMouseOverlay()

        batch.use {
            backgroundOne.draw(it)
            backgroundTwo.draw(it)
            drawMenuChoices(it)
        }
    }

    private fun checkMouseOverlay() = menuChoices.forEach { choice ->
        choice.active = choice.bounds.contains(mousePosition)
    }

    private fun drawMenuChoices(batch: SpriteBatch) = menuChoices.forEach { it.sprite.draw(batch) }

    private fun getMousePosInGameWorld() {
        val position = camera.unproject(Vector3(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f))
        mousePosition.x = position.x
        mousePosition.y = position.y
    }

    override fun show() {
        super.show()
        rainMusic.isLooping = true
        rainMusic.play()
        backgroundMusic.isLooping = true
        backgroundMusic.play()
    }
}