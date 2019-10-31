package com.mygdx.game.desktop

import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration
import com.mygdx.game.Game
import com.mygdx.game.settings.WINDOW_HEIGHT
import com.mygdx.game.settings.WINDOW_WIDTH

fun main() {
    val config = LwjglApplicationConfiguration()
    config.width = WINDOW_WIDTH.toInt()
    config.height = WINDOW_HEIGHT.toInt()
    LwjglApplication(Game(), config)
}
