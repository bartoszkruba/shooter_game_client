package com.mygdx.game.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
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
    MAIN, CREDITS, START_GAME, OPTIONS, SPLASH_SCREEN
}

class MenuScreen(
        val game: Game,
        private val batch: SpriteBatch,
        private val assets: AssetManager,
        private val camera: OrthographicCamera,
        private val font: BitmapFont) : KtxScreen {

    private var currentWindow = Menu.SPLASH_SCREEN

    private val startGameChoice = "sg"
    private val quitChoice = "q"
    private val optionsChoice = "op"
    private val creditsChoice = "cr"

    private val mousePosition = Vector2()

    private val mainMenuChoices = Array<MenuChoice>()

    private val rainMusic = assets.get<Music>("music/rain.mp3")
    private val backgroundMusic = assets.get<Music>("music/waiting.ogg")

    private val background: Sprite = Sprite(assets.get<Texture>("images/splashscreen/background.png"))
    private val foreground: Sprite = Sprite(assets.get<Texture>("images/splashscreen/foreground.png"))

    private val text = Sprite(assets.get<Texture>("images/logo.png"))

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
        foreground.setBounds(-WINDOW_WIDTH * 2, 0f, WINDOW_WIDTH * 3, WINDOW_HEIGHT)
        background.setBounds(0f, 0f, WINDOW_WIDTH * 3, WINDOW_HEIGHT)
        text.setBounds(WINDOW_WIDTH, WINDOW_HEIGHT, WINDOW_WIDTH / 3, WINDOW_HEIGHT / 3)

        mainMenuChoices.add(MenuChoice(
                assets.get<Texture>("images/menu/quit_selected.png"),
                assets.get<Texture>("images/menu/quit.png"),
                quitChoice
        ))

        mainMenuChoices.add(MenuChoice(
                assets.get<Texture>("images/menu/credits_selected.png"),
                assets.get<Texture>("images/menu/credits.png"),
                creditsChoice
        ))

        mainMenuChoices.add(MenuChoice(
                assets.get<Texture>("images/menu/options_selected.png"),
                assets.get<Texture>("images/menu/options.png"),
                optionsChoice
        ))

        mainMenuChoices.add(MenuChoice(
                assets.get<Texture>("images/menu/start_selected.png"),
                assets.get<Texture>("images/menu/start.png"),
                startGameChoice
        ))
        val x = (WINDOW_WIDTH - mainMenuChoices[0].sprite.width) / 2
        var y = 100f

        mainMenuChoices.forEach {
            it.setPosition(x, y)
            y += it.sprite.height + 50
        }

    }

    override fun render(delta: Float) {
        foreground.setPosition(foreground.x + 0.1f * 60 * delta, foreground.y)
        background.setPosition(background.x - 0.03f * 60 * delta, background.y)
        if ( text.x>100 && text.y>200 ) {
            text.translate(-WINDOW_WIDTH/1000*text.width*delta, -WINDOW_HEIGHT/1000*text.height*delta)
        }

        moveDroplets(delta)
        if (TimeUtils.millis() - lastSpawn > spawnRate) spawnDroplet()


        camera.update()
        batch.projectionMatrix = camera.combined

        getMousePosInGameWorld()
        checkMouseOverlay()
        checkMouseClick()

        batch.use {
            background.draw(it)
            foreground.draw(it)
            text.draw(it)
        }

        drawRain()

        if (Gdx.input.isTouched && currentWindow == Menu.SPLASH_SCREEN) currentWindow = Menu.MAIN

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
                quitChoice -> {
                    assets.dispose()
                    dispose()
                    Gdx.app.exit()
                }
            }
        }
    }

    private fun checkMouseOverlay() {
        var newValue = ""
        if(currentWindow == Menu.MAIN){
            mainMenuChoices.forEach { choice ->
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
        }
        currentChoice = newValue
    }

    private fun drawMenuChoices(batch: SpriteBatch) = when (currentWindow) {
        Menu.MAIN -> drawMainMenuChoices(batch)
        Menu.CREDITS -> {
        }
        Menu.START_GAME -> {
        }
        Menu.OPTIONS -> {
        }
        Menu.SPLASH_SCREEN -> drawSplashScreen(batch)
    }

    private fun drawSplashScreen(batch: SpriteBatch) {
        text.draw(batch)
    }

    private fun drawMainMenuChoices(batch: SpriteBatch) = mainMenuChoices.forEach { it.sprite.draw(batch) }

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