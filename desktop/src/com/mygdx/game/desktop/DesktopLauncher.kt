package com.mygdx.game.desktop

import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration
import com.mygdx.game.Game

fun main() {
    val config = LwjglApplicationConfiguration()
    LwjglApplication(Game(), config)
}
