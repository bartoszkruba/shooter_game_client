package com.mygdx.game.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.mygdx.game.settings.WINDOW_WIDTH
import com.mygdx.game.ui.MenuChoice
import com.badlogic.gdx.utils.Array
import ktx.app.KtxScreen
import ktx.graphics.use

class MenuScreen(assets: AssetManager, private val batch: SpriteBatch, private val camera: OrthographicCamera) : KtxScreen {

    val startGameChoice = "sg"
    val quitChoice = "q"
    val optionsChoice = "op"
    val creditsChoice = "cr"

    val mousePosition = Vector2()

    val menuChoices = Array<MenuChoice>()

    init {

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
}