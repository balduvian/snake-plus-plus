package com.gnarly.game

import com.gnarly.engine.Camera
import com.gnarly.engine.Texture
import com.gnarly.game.Util.posMod
import org.lwjgl.opengl.GL46
import kotlin.math.ceil
import kotlin.math.floor

class Map(val width: Int, val height: Int, val map: IntArray, val palette: FloatArray, val wrap: Boolean, var onState: Boolean) {
	var collectedApples = 0

	data class Tile(val type: Int, val data: Int) {
		companion object {
			fun fromInt(int: Int): Tile {
				return Tile(int.and(0xff), int.ushr(8).and(0xff))
			}
		}
	}

	val textureBuffer: IntArray = IntArray(map.size)

	fun indexOf(x: Int, y: Int): Int {
		return y * width + x
	}

	fun access(x: Int, y: Int): Tile {
		return if (wrap) {
			Tile.fromInt(map[indexOf(posMod(x, width), posMod(y, height))])
		} else {
			if (x < 0 || x >= width || y < 0 || y >= height) Tile(MapTemplate.TYPE_WALL, 0) else Tile.fromInt(map[indexOf(x, y)])
		}
	}

	fun isSolid(tile: Tile, direction: Direction): Boolean {
		return when (tile.type) {
			MapTemplate.TYPE_WALL -> true
			MapTemplate.TYPE_ON_OFF_1 -> onState
			MapTemplate.TYPE_ON_OFF_2 -> !onState
			MapTemplate.TYPE_APPLE_GATE -> collectedApples < tile.data
			MapTemplate.TYPE_ONE_WAY -> direction == Direction.values()[tile.data].inverse()
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
				val tile = access(x, y)

				when (tile.type) {
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
					MapTemplate.TYPE_ONE_WAY -> {
						Assets.colorShader.enable().setMVP(
							camera.projectionView(),
							camera.model.translation(x.toFloat() + 0.5f, y.toFloat() + 0.5f, 0.0f)
								.rotateZ(Direction.values()[tile.data].rotation)
								.translate(-0.125f, 0.0f, 0.0f)
						)
						Assets.colorShader.setColor(1.0f, 0.0f, 0.5f, 1.0f)
						Assets.oneWayTriangle.render()
					}
					MapTemplate.TYPE_APPLE -> {
						Assets.colorShader.enable().setMVP(
							camera.projectionView(),
							camera.model(x + 0.5f, y + 0.5f, 1.0f, 1.0f)
						)
						Assets.colorShader.setColor(0.0f, 0.75f, 0.0f, 1.0f)
						Assets.circle.render()
					}
					MapTemplate.TYPE_APPLE_GATE -> {
						Assets.colorShader.enable().setMVP(
							camera.projectionView(),
							camera.model(x.toFloat(), y.toFloat(), 1.0f, 1.0f)
						)
						Assets.colorShader.setColor(0.0f, 0.5f, 0.0f, if (collectedApples < tile.data) 1.0f else 0.25f)
						Assets.rect.render()
					}
				}
			}
		}
	}
}
