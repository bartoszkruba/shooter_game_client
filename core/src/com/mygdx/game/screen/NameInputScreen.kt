package com.mygdx.game.screen

import com.badlogic.gdx.Gdx
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
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.TimeUtils
import com.badlogic.gdx.utils.viewport.StretchViewport
import com.mygdx.game.Game
import com.mygdx.game.settings.WINDOW_HEIGHT
import com.mygdx.game.settings.WINDOW_WIDTH
import ktx.app.KtxScreen
import ktx.assets.pool
import ktx.collections.iterate
import ktx.graphics.use
import java.awt.TextField

class NameInputScreen (
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

    private val text = Sprite(assets.get<Texture>("images/logo.png"))
    private val droplets = Array<Droplet>()

    private val shape = ShapeRenderer()

    val bigFont = BitmapFont()
    val smallFont = BitmapFont()

    lateinit var txfUsername: TextField
    private var stage = Stage()

    init {
        foreground.setBounds(-WINDOW_WIDTH * 2, 0f, WINDOW_WIDTH * 3, WINDOW_HEIGHT)
        background.setBounds(0f, 0f, WINDOW_WIDTH * 3, WINDOW_HEIGHT)
        text.setBounds(WINDOW_WIDTH, WINDOW_HEIGHT, WINDOW_WIDTH / 3, WINDOW_HEIGHT / 3)
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
        foreground.setPosition(foreground.x + 0.1f * 60 * delta, foreground.y)
        background.setPosition(background.x - 0.03f * 60 * delta, background.y)

        if (text.x > 60 && text.y > 100) {
            text.x -= WINDOW_WIDTH / 1000 * text.width * delta
        }
        moveDroplets(delta)
        if (TimeUtils.millis() - lastSpawn > spawnRate) spawnDroplet()

        camera.update()
        batch.projectionMatrix = camera.combined

        batch.use {
            background.draw(it)
            foreground.draw(it)
            text.draw(it)
            drawNameInput(it)
        }
        stage.act()
        stage.draw();

        drawRain()

    }

    private fun drawNameInput(batch: SpriteBatch) {
        val miniMapexture = assets.get("images/enterYourName.png", Texture::class.java)
        val c = batch.color;
        //batch.setColor(c.r, c.g, c.b, .5f)
        batch.draw(miniMapexture, WINDOW_WIDTH / 4, WINDOW_HEIGHT / 2,WINDOW_WIDTH / 3, 50f);

        txfUsername = TextField("")
        txfUsername.setLocation(300, 250)
        txfUsername.setSize(300, 40)

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
