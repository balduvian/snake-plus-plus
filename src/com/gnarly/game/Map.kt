package com.gnarly.game

import com.gnarly.engine.Camera
import com.gnarly.engine.Texture
import com.gnarly.game.Util.posMod
import org.lwjgl.opengl.GL46
import kotlin.math.ceil
import kotlin.math.floor

class Map(val width: Int, val height: Int, val map: IntArray, val palette: FloatArray, val wrap: Boolean, var onState: Boolean) {
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

	fun isSolid(tile: Int): Boolean {
		return when (tile) {
			MapTemplate.TYPE_WALL -> true
			MapTemplate.TYPE_ON_OFF_1 -> onState
			MapTemplate.TYPE_ON_OFF_2 -> !onState
			else -> false
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
				MapTemplate.TYPE_SPEED_DOWN -> MapTemplate.INT_SPEED_DOWN
				MapTemplate.TYPE_LENGTH_DOWN -> MapTemplate.INT_LENGTH_DOWN
				else -> MapTemplate.INT_EMPTY
			}
		}

		texture.setImage(width, height, textureBuffer)

		return texture
	}

	fun render(camera: Camera, time: Float, dataTexture: Texture, paletteOverride: FloatArray? = null) {
		writeDataTexture(dataTexture)

		Assets.levelShader.enable().setMVP(
			camera.projectionView(),
			camera.model(camera.x - camera.width, camera.y - camera.height, camera.width * 2.0f, camera.height * 2.0f)
		)
		Assets.levelShader.setTime(time)
			.setLevelSize(dataTexture.width - 1, dataTexture.height - 1)
			.setcolorPalette(paletteOverride ?: palette)
		dataTexture.bind()
		Assets.rect.render()

		val minX = floor(camera.x - camera.width / 2.0f).toInt()
		val minY = floor(camera.y - camera.height / 2.0f).toInt()
		val maxX = ceil(camera.x + camera.width / 2.0f).toInt()
		val maxY = ceil(camera.y + camera.height / 2.0f).toInt()

		for (y in minY..maxY) {
			for (x in minX..maxX) {
				when (access(x, y)) {
					MapTemplate.TYPE_END -> {
						Assets.colorShader.enable().setMVP(
							camera.projectionView(),
							camera.model(x.toFloat(), y.toFloat(), 1.0f, 1.0f)
						)
						Assets.colorShader.setColor(1.0f, 0.0f, 0.0f, 1.0f)
						Assets.rect.render()
					}
					MapTemplate.TYPE_SWITCH -> {
						Assets.colorShader.enable().setMVP(
							camera.projectionView(),
							camera.model(x.toFloat(), y.toFloat(), 1.0f, 1.0f)
						)
						Assets.colorShader.setColor(0.5f, 0.0f, 1.0f, 1.0f)
						Assets.rect.render()
					}
					MapTemplate.TYPE_ON_OFF_1 -> {
						Assets.colorShader.enable().setMVP(
							camera.projectionView(),
							camera.model(x.toFloat(), y.toFloat(), 1.0f, 1.0f)
						)
						Assets.colorShader.setColor(0.0f, 0.5f, 0.5f, if (onState) 1.0f else 0.25f)
						Assets.rect.render()
					}
					MapTemplate.TYPE_ON_OFF_2 -> {
						Assets.colorShader.enable().setMVP(
							camera.projectionView(),
							camera.model(x.toFloat(), y.toFloat(), 1.0f, 1.0f)
						)
						Assets.colorShader.setColor(0.5f, 0.0f, 0.0f, if (onState) 0.25f else 1.0f)
						Assets.rect.render()
					}
				}
			}
		}
	}
}
