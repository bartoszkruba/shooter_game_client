package com.mygdx.game.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.TextField
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.TimeUtils
import com.badlogic.gdx.utils.viewport.FitViewport
import com.mygdx.game.Game
import com.mygdx.game.settings.WINDOW_HEIGHT
import com.mygdx.game.settings.WINDOW_WIDTH
import ktx.app.KtxScreen
import ktx.assets.pool
import ktx.collections.iterate
import ktx.graphics.use


class NameInputScreen (
        val x: Float,
        val game: Game,
        private val batch: SpriteBatch,
        private val assets: AssetManager,
        private val camera: OrthographicCamera,
        private val font: BitmapFont) : KtxScreen {

    private val lastSpawn = 0L

    private val spawnRate = 100f

    private val dropletSpeed = 1000f

    private val maxDropletLength = 20f
    private val minDropletLength = 10f
    private val maxDropletWidth = 2f
    private val minDropletWidth = 1f

    private val dropletPool = pool { Droplet() }


    private val rainMusic = assets.get<Music>("music/rain.mp3")
    private val backgroundMusic = assets.get<Music>("music/waiting.ogg")

    private val background: Sprite = Sprite(assets.get<Texture>("images/splashscreen/background.png"))
    private val foreground: Sprite = Sprite(assets.get<Texture>("images/splashscreen/foreground.png"))

    private var text = Sprite(assets.get<Texture>("images/logo.png"))
    private val droplets = Array<Droplet>()

    private val shape = ShapeRenderer()

    val bigFont = BitmapFont()
    val smallFont = BitmapFont()

    lateinit var txfUsername: TextField
    private var stage = Stage(FitViewport(Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat()))
    private var stage2 = Stage(FitViewport(Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat()))
    var skin: Skin = Skin(Gdx.files.internal("terra-mother/skin/terra-mother-ui.json"))

    lateinit var username: String

    init {
        foreground.setBounds(-WINDOW_WIDTH * 2, 0f, WINDOW_WIDTH * 3, WINDOW_HEIGHT)
        background.setBounds(0f, 0f, WINDOW_WIDTH * 3, WINDOW_HEIGHT)
        text.setBounds(x, WINDOW_HEIGHT, WINDOW_WIDTH / 3, WINDOW_HEIGHT / 3)
        text.y = WINDOW_HEIGHT - 280f

        bigFont.data.setScale(4f)
        bigFont.region.texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        smallFont.data.scale(2f)
        smallFont.region.texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        repeat(100) {
            moveDroplets(0.1f)
            spawnDroplet()
        }
    }

    override fun render(delta: Float) {
        setBackground(delta)
        setLogo(delta)
        moveDroplets(delta)
        if (TimeUtils.millis() - lastSpawn > spawnRate) spawnDroplet()

        camera.update()
        batch.projectionMatrix = camera.combined

        batch.use {
            background.draw(it)
            foreground.draw(it)
            text.draw(it)
            drawNameSign(it)
            nameInputFiled(it)
        }
        stage.draw();
        checkNameInput()
        drawRain();
    }

    private fun setBackground(delta: Float) {
        foreground.setPosition(foreground.x + 0.1f * 60 * delta, foreground.y)
        background.setPosition(background.x - 0.03f * 60 * delta, background.y)
    }

    private fun setLogo(delta: Float) {
        if (text.x > 60 && text.y > 100) {
            text.x -= WINDOW_WIDTH / 1000 * text.width * delta
        }
    }

    private fun checkNameInput() {
        if (Gdx.input.isKeyPressed(Input.Keys.ENTER)) {
            rainMusic.stop()
            backgroundMusic.stop()
            game.changeToGame()
            frontendServer.Server.setName(username)
        }
    }

    private fun nameInputFiled(batch: SpriteBatch) {
        val c = batch.color;
        val txfUsernameBackground = assets.get("images/txfUsernameBackground.png", Texture::class.java)
        batch.setColor(c.r, c.g, c.b, .5f)
        batch.draw(txfUsernameBackground, WINDOW_WIDTH / 4.4f, WINDOW_HEIGHT / 2.68f, 210f, 110f);

        font.draw(batch, "OBS! max 8 characters", WINDOW_WIDTH / 2.7f, WINDOW_HEIGHT / 2.18f);
        txfUsername = TextField("", skin)
        txfUsername.maxLength = 8
        txfUsername.setPosition(WINDOW_WIDTH / 3.9f, WINDOW_HEIGHT / 2.6f)
        txfUsername.setSize(130f, 100f)
        stage.addActor(txfUsername)
        Gdx.input.inputProcessor = this.stage;

        txfUsername.setTextFieldListener { textField, key -> username = textField.text }

        font.draw(batch, "Press ENTER to start the game!", WINDOW_WIDTH / 4f, WINDOW_HEIGHT / 2.4f);

    }

    private fun drawNameSign(batch: SpriteBatch) {
        val c = batch.color;
        val miniMapexture = assets.get("images/enterYourName.png", Texture::class.java)
        batch.setColor(c.r, c.g, c.b, 1f)
        batch.draw(miniMapexture, WINDOW_WIDTH / 4, WINDOW_HEIGHT / 2, WINDOW_WIDTH / 3, 50f);
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

    private fun drawRain() {
        shape.projectionMatrix = camera.combined
        droplets.forEach {
            shape.begin(ShapeRenderer.ShapeType.Filled)
            shape.color = Color.CYAN
            shape.rectLine(it.x, it.y - it.length, it.x, it.y, it.width)
            shape.end()
        }

    }
}
