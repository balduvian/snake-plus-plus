package com.gnarly.game

import com.gnarly.engine.audio.Sound
import java.io.File
import javax.imageio.ImageIO

class MapTemplate(levelFile: File, soundFile: File, dataFile: File) {
	data class Data(val snakeSpeed: Int, val palette: FloatArray, val wrap: Boolean)

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

		fun readDataFile(dataFile: File): Data {
			var snakeSpeed: Int? = null
			var palette: FloatArray? = null
			var wrap: Boolean? = null

			val lines = dataFile.readLines()

			lines.forEach { line ->
				val parts = line.split("=").map { it.trim() }

				if (parts.size != 2) return@forEach

				if (parts[0] == "snakeSpeed") {
					snakeSpeed = parts[1].toInt()
				} else if (parts[0] == "palette") {
					palette = parts[1]
						.split(Regex("[\\[\\] ]+"))
						.filter { it.isNotEmpty() }
						.map { it.toFloat() }
						.toFloatArray()
				} else if (parts[0] == "wrap") {
					wrap = parts[1].toBoolean()
				}
			}

			return Data(
				snakeSpeed ?: throw Exception("no 'snakeSpeed' defined in datafile $dataFile"),
				palette ?: throw Exception("no 'palette' defined in datafile $dataFile"),
				wrap ?: throw Exception("no 'wrap' defined in datafile $dataFile"),
			)
		}
	}

	val width: Int
	val height: Int
	var snakeStartPos: Point = Point(0, 0)
	var snakeStartDir: Direction = Direction.RIGHT
	val map: IntArray
	val music: Sound
	val data: Data

	init {
		data = readDataFile(dataFile)
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

	fun toMap(): Map {
		return Map(width, height, map.clone(), data.palette, data.wrap)
	}
}
