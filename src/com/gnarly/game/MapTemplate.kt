package com.gnarly.game

import com.gnarly.engine.Texture
import com.gnarly.engine.audio.Sound
import org.lwjgl.opengl.GL46.*
import java.io.File
import javax.imageio.ImageIO

class MapTemplate(levelFile: File, soundFile: File) {
	companion object {
		const val TYPE_EMPTY = 0
		const val TYPE_WALL = 1
		const val TYPE_START = 2
		const val TYPE_END = 3
		const val TYPE_LENGTH = 4
		const val TYPE_SPEED = 5

		const val INT_EMPTY = 0x000000ff
		const val INT_WALL = 0x00ff00ff
		const val INT_LENGTH = 0x400000ff
		const val INT_SPEED = 0x7f0000ff
	}

	val width: Int
	val height: Int
	var snakeStartPos: Point = Point(0, 0)
	var snakeStartDir: Direction = Direction.RIGHT
	val map: IntArray
	val music: Sound

	init {
		music = Sound(soundFile)

		val image = ImageIO.read(levelFile)

		width = image.width
		height = image.height

		map = IntArray(width * height)

		for (j in 0 until height) {
			for (x in 0 until width) {
				val y = height - j - 1
				val index = y * width + x

				when (val pixel = image.getRGB(x, j).and(0x00ffffff)) {
					0xffffff -> map[index] = TYPE_WALL
					in 0x00ff00..0x00ff03 -> {
						map[index] = TYPE_START
						snakeStartPos = Point(x, y)
						snakeStartDir = Direction.values()[pixel and 0x03]
					}
					0xff0000 -> map[index] = TYPE_END
					0x0000ff -> map[index] = TYPE_LENGTH
					0xff7f00 -> map[index] = TYPE_SPEED
					else -> map[index] = TYPE_EMPTY
				}
			}
		}
	}

	fun writeDataTexture(texture: Texture, wrap: Boolean = false): Texture {
		val textureWrap = if (wrap) GL_REPEAT else GL_CLAMP_TO_EDGE
		texture.parameters(GL_NEAREST, GL_NEAREST, textureWrap, textureWrap)
		texture.setImage(width, height, IntArray(width * height) { i ->
			when (map[i]) {
				TYPE_EMPTY -> INT_EMPTY
				TYPE_WALL -> INT_WALL
				TYPE_LENGTH -> INT_LENGTH
				TYPE_SPEED -> INT_SPEED
				else -> INT_EMPTY
			}
		})

		return texture
	}

	fun toMap(): Map {
		return Map(width, height, map.clone())
	}
}
