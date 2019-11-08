package com.mygdx.game.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.mygdx.game.settings.WINDOW_WIDTH
import com.mygdx.game.ui.MenuChoice
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.TimeUtils
import com.mygdx.game.Game
import com.mygdx.game.settings.WINDOW_HEIGHT
import ktx.app.KtxScreen
import ktx.assets.pool
import ktx.collections.iterate
import ktx.graphics.use


private class Droplet(var x: Float = 0f, var y: Float = 0f, var length: Float = 0f, var width: Float = 0f)

private enum class Menu {
    MAIN, CREDITS, START_GAME, OPTIONS
}

class MenuScreen(private val game: Game, assets: AssetManager, private val batch: SpriteBatch,
                 private val camera: OrthographicCamera) : KtxScreen {

    private var current_window = Menu.MAIN

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

    private val hoverSound = assets.get<Sound>("sounds/menu_hover.wav")
    private val selectSound = assets.get<Sound>("sounds/menu_select.wav")

    private val shape = ShapeRenderer()

    private val droplets = Array<Droplet>()

    private val lastSpawn = 0L

    private val spawnRate = 100f

    private val dropletSpeed = 1000f

    private val maxDropletLength = 20f
    private val minDropletLength = 10f
    private val maxDropletWidth = 2f
    private val minDropletWidth = 1f

    private val dropletPool = pool { Droplet() }

    private var currentChoice = ""

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
        moveDroplets(delta)
        if (TimeUtils.millis() - lastSpawn > spawnRate) spawnDroplet()


        camera.update()
        batch.projectionMatrix = camera.combined

        getMousePosInGameWorld()
        checkMouseOverlay()
        checkMouseClick()

        batch.use {
            backgroundOne.draw(it)
            backgroundTwo.draw(it)
        }

        drawRain()

        batch.use {
            drawMenuChoices(it)
        }
    }

    private fun checkMouseClick() {
        if (currentChoice != "" && Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            selectSound.play()
            when (currentChoice) {
                startGameChoice -> {
                    rainMusic.stop()
                    backgroundMusic.stop()
                    game.changeToGame()
                }
            }
        }
    }

    private fun checkMouseOverlay() {
        var newValue = ""
        menuChoices.forEach { choice ->
            choice.active = if (choice.bounds.contains(mousePosition)) {
                if (currentChoice != choice.type) {
                    hoverSound.play()
                }
                newValue = choice.type
                true
            } else {
                false
            }
        }
        currentChoice = newValue
    }

    private fun drawMenuChoices(batch: SpriteBatch) = when(current_window){
        Menu.MAIN -> drawMainMenuChoices(batch)
        Menu.CREDITS -> {}
        Menu.START_GAME -> {}
        Menu.OPTIONS -> {}
    }

    private fun drawMainMenuChoices(batch: SpriteBatch) = menuChoices.forEach { it.sprite.draw(batch) }

    private fun getMousePosInGameWorld() {
        val position = camera.unproject(Vector3(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f))
        mousePosition.x = position.x
        mousePosition.y = position.y
    }

    private fun drawRain() {

        shape.projectionMatrix = camera.combined
        droplets.forEach {
            shape.begin(ShapeRenderer.ShapeType.Filled)
            shape.color = Color.CYAN
            shape.rectLine(it.x, it.y - it.length, it.x, it.y, it.width)
            shape.end()
        }

    }

    private fun spawnDroplet() {
        repeat(3) {
            droplets.add(dropletPool.obtain().apply {
                x = MathUtils.random(0f, WINDOW_WIDTH)
                y = WINDOW_HEIGHT
                length = MathUtils.random(minDropletLength, maxDropletLength)
                width = MathUtils.random(minDropletWidth, maxDropletWidth)
            })
        }
    }

    private fun moveDroplets(delta: Float) {
        droplets.iterate { droplet, iterator ->
            droplet.y -= (dropletSpeed + ((droplet.width - minDropletWidth) * 1500)) * delta
            if (droplet.y + droplet.length < 0f) {
                dropletPool.free(droplet)
                iterator.remove()
            }
        }
    }

    override fun show() {
        super.show()
        rainMusic.isLooping = true
        rainMusic.play()
        backgroundMusic.isLooping = true
        backgroundMusic.volume = 0.3f
        backgroundMusic.play()
    }
}