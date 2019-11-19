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
import frontendServer.Server
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

    private val ipFont = BitmapFont()
    private val errorMassageFont = BitmapFont()
    private val connectorFont = BitmapFont()
    private var errorMassage = false
    private var massageText = ""
    private val lastSpawn = 0L

    var imgpos = 0.0
    var imgposdir = 0.1

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
    lateinit var txfIP: TextField
    private var stage = Stage(FitViewport(Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat()))
    var skin: Skin = Skin(Gdx.files.internal("terra-mother/skin/terra-mother-ui.json"))

    lateinit var username: String
    lateinit var ipAddress: String

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
            ipInputField(it)
            checkNameInput(it)
            drawErrorMassage(it)
            drawConnectionLabel(it)
        }
        stage.draw();
        drawRain();
    }

    private fun drawErrorMassage(batch: SpriteBatch) {
        if (errorMassage) {
            errorMassageFont.draw(batch, massageText, WINDOW_WIDTH / 3.9f, WINDOW_HEIGHT / 3.5f);
            errorMassageFont.color = Color.RED
        }
    }

    private fun ipInputField(batch: SpriteBatch) {
        inputFieldBackground(batch, 4.4f, 3.5f)
        ipFont.draw(batch, "For example: 10.152.190.106", WINDOW_WIDTH / 2.7f, WINDOW_HEIGHT / 2.7f);
        ipFont.color = Color.GRAY;
        txfIP = TextField("", skin)
        txfIP.maxLength = 14
        setupTextField(txfIP, 3.42f)

        txfIP.setTextFieldListener { textField, key -> ipAddress = textField.text }

        font.draw(batch, "Press ENTER to start the game!", WINDOW_WIDTH / 4f, WINDOW_HEIGHT / 3f);
    }

    private fun inputFieldBackground(batch: SpriteBatch, x: Float, y: Float) {
        val c = batch.color;
        val txfUsernameBackground = assets.get("images/txfUsernameBackground.png", Texture::class.java)
        batch.setColor(c.r, c.g, c.b, .5f)
        batch.draw(txfUsernameBackground, WINDOW_WIDTH / x, WINDOW_HEIGHT / y,
                WINDOW_WIDTH / 6.1f, WINDOW_HEIGHT / 6.5f);
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

            var showConnectionSign = false
    private fun checkNameInput(batch: SpriteBatch) {
        if (Gdx.input.isKeyPressed(Input.Keys.ENTER)) {
            if (::username.isInitialized && ::ipAddress.isInitialized) {
                if (username.length > 2 && ipAddress.length == 14) {
                    Server.connectionSocket(ipAddress)
                    rainMusic.stop()
                    backgroundMusic.stop()
                    Server.setName(username)
                    game.changeToGame()

                }else{
                    showConnectionSign = false
                    errorMassage = true
                    massageText = "LENGTH OF YOUR NAME OR IP IS NOT CORRECT"
                }
            }else {
                showConnectionSign = false
                errorMassage = true
                massageText = "YOU HAVE TO ENTER YOUR NAME AND IP"
            }
        }
    }

    private fun drawConnectionLabel(batch: SpriteBatch) {
        if (showConnectionSign) {
            imgpos += (imgposdir / 3);
            if (imgpos < 0.0) imgposdir = -imgposdir;
            if (imgpos > 1.0) imgposdir = -imgposdir;

            val c = batch.color;
            connectorFont.draw(batch, "TRY TO CONNECT..", WINDOW_WIDTH / 3.9f, WINDOW_HEIGHT / 3.5f);
            batch.setColor(c.r, c.g, c.b, imgpos.toFloat())
        }
    }

    private fun nameInputFiled(batch: SpriteBatch) {
        inputFieldBackground(batch, 4.4f, 2.68f)
        font.draw(batch, "OBS! max 8 characters", WINDOW_WIDTH / 2.7f, WINDOW_HEIGHT / 2.18f);
        txfUsername = TextField("", skin)
        txfUsername.maxLength = 8
        setupTextField(txfUsername, 2.65f)
        txfUsername.setTextFieldListener { textField, key -> username = textField.text }
    }

    private fun setupTextField(txf: TextField, y: Float) {
        txf.setPosition(Gdx.graphics.width / 3.9f, Gdx.graphics.height / y)
        txf.setSize(Gdx.graphics.width / 3.9f, Gdx.graphics.height / 6.5f)
        stage.addActor(txf)
        Gdx.input.inputProcessor = this.stage;
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
