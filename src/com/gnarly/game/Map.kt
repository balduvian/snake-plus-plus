package com.gnarly.game

import com.gnarly.engine.Camera
import com.gnarly.engine.Texture
import com.gnarly.game.Util.posMod
import org.lwjgl.opengl.GL46
import kotlin.math.ceil
import kotlin.math.floor

class Map(val width: Int, val height: Int, val map: IntArray, val palette: FloatArray, val wrap: Boolean) {
	val textureBuffer: IntArray = IntArray(map.size)

	fun indexOf(x: Int, y: Int): Int {
		return y * width + x
	}

	fun access(x: Int, y: Int): Int {
		return if (wrap) {
			map[indexOf(posMod(x, width), posMod(y, height))]
		} else {
			if (x < 0 || x >= width || y < 0 || y >= height) MapTemplate.TYPE_WALL else map[indexOf(x, y)]
		}
	}

	fun writeDataTexture(texture: Texture): Texture {
		val textureWrap = if (wrap) GL46.GL_REPEAT else GL46.GL_CLAMP_TO_EDGE
		texture.parameters(GL46.GL_NEAREST, GL46.GL_NEAREST, textureWrap, textureWrap)

		for (i in textureBuffer.indices) {
			textureBuffer[i] = when (map[i]) {
				MapTemplate.TYPE_EMPTY -> MapTemplate.INT_EMPTY
				MapTemplate.TYPE_WALL -> MapTemplate.INT_WALL
				MapTemplate.TYPE_LENGTH -> MapTemplate.INT_LENGTH
				MapTemplate.TYPE_SPEED -> MapTemplate.INT_SPEED
				else -> MapTemplate.INT_EMPTY
			}
		}

		texture.setImage(width, height, textureBuffer)

		return texture
	}

	fun render(camera: Camera, time: Float, dataTexture: Texture, paletteOverride: FloatArray? = null) {
		writeDataTexture(dataTexture)

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
				when (access(x, y)) {
					MapTemplate.TYPE_END -> {
						Assets.colorShader.enable().setMVP(camera.getMVP(x.toFloat(), y.toFloat(), 1.0f, 1.0f))
						Assets.colorShader.setColor(1.0f, 0.0f, 0.0f, 1.0f)
						Assets.rect.render()
					}
				}
			}
		}
	}
}
