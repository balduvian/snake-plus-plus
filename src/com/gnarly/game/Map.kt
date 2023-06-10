package com.gnarly.game

import com.gnarly.engine.Camera
import com.gnarly.engine.Texture
import kotlin.math.ceil
import kotlin.math.floor

class Map(val width: Int, val height: Int, val map: IntArray) {
	val palette = floatArrayOf(
		0.500f, 0.500f, 0.500f,
		0.468f, 0.438f, 0.168f,
		1.000f, 0.878f, 1.000f,
		0.000f, 0.333f, 0.667f,
	)

	fun indexOf(x: Int, y: Int): Int {
		return y * width + x
	}

	fun warpAccess(x: Int, y: Int): Int {
		return map[indexOf(posMod(x, width), posMod(y, height))]
	}

	fun boundedAccess(x: Int, y: Int): Int {
		return if (x < 0 || x >= width || y < 0 || y >= height) MapTemplate.TYPE_WALL else map[indexOf(x, y)]
	}

	fun access(x: Int, y: Int, wrap: Boolean): Int {
		return if (wrap) warpAccess(x, y) else boundedAccess(x, y)
	}

	fun render(camera: Camera, wrap: Boolean = false, time: Float, dataTexture: Texture, paletteOverride: FloatArray? = null) {
		Assets.levelShader.enable().setMVP(camera.getMP(0.0f, 0.0f, camera.width, camera.height))
		Assets.levelShader.setTime(time)
			.setLevelSize(dataTexture.width - 1, dataTexture.height - 1)
			.setCameraDims(camera.width, camera.height)
			.setOffset(camera.x, camera.y)
			.setcolorPalette(paletteOverride ?: palette)
		dataTexture.bind()
		Assets.rect.render()

		val minX = floor(camera.x).toInt()
		val minY = floor(camera.y).toInt()
		val maxX = ceil(camera.x + camera.width).toInt()
		val maxY = ceil(camera.y + camera.height).toInt()

		for (y in minY..maxY) {
			for (x in minX..maxX) {
				when (access(x, y, wrap)) {
					MapTemplate.TYPE_END -> {
						Assets.colorShader.enable().setMVP(camera.getMVP(x.toFloat(), y.toFloat(), 1.0f, 1.0f))
						Assets.colorShader.setColor(1.0f, 0.0f, 0.0f, 1.0f)
						Assets.rect.render()
					}
				}
			}
		}
	}

	companion object {
		fun posMod(a: Int, b: Int): Int {
			return ((a % b) + b) % b
		}
	}
}
