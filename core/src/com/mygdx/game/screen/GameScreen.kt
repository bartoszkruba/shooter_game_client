package com.mygdx.game.screen

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.mygdx.game.Game
import com.mygdx.game.model.Player
import com.mygdx.game.settings.WINDOW_HEIGHT
import com.mygdx.game.settings.WINDOW_WIDTH
import ktx.app.KtxScreen
import ktx.graphics.use

class GameScreen(
        val game: Game,
        val batch: SpriteBatch,
        val font: BitmapFont,
        val camera: OrthographicCamera) : KtxScreen {

    val player = Player(WINDOW_WIDTH / 2f - 16f, WINDOW_HEIGHT / 2f - 32f)

    override fun render(delta: Float) {
        batch.projectionMatrix = camera.combined

        batch.use {
            drawPlayer(it, player)
        }
    }

    private fun drawPlayer(batch: Batch, player: Player) = player.sprite.draw(batch)
}