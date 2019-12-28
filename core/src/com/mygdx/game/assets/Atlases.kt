package com.mygdx.game.assets

import com.badlogic.gdx.assets.AssetManager

class Atlases(assets: AssetManager) {
    val playerAtlas = assets[TextureAtlasAssets.Player]
    val bazookaExplosionAtlas = assets[TextureAtlasAssets.BazookaExplosion]
}