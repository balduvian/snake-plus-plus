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
		const val TYPE_SWITCH = 6
		const val TYPE_ON_OFF_1 = 7
		const val TYPE_ON_OFF_2 = 8
		const val TYPE_SPEED_DOWN = 9
		const val TYPE_LENGTH_DOWN = 10

		const val TYPE_APPLE = 11
		const val TYPE_APPLE_GATE = 12
		const val TYPE_ONE_WAY = 13

		const val INT_EMPTY       = 0x000000ff
		const val INT_WALL        = 0x00ff00ff
		const val INT_LENGTH      = 0x010000ff // 1
		const val INT_SPEED       = 0x020000ff // 2
		const val INT_LENGTH_DOWN = 0x030000ff // 3
		const val INT_SPEED_DOWN  = 0x040000ff // 4

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

				val argb = image.getRGB(x, j)

				val red = argb.ushr(16).and(0xff)
				val gre = argb.ushr(8).and(0xff)
				val blu = argb.and(0xff)

				when {
					red == 0xff && gre == 0xff && blu == 0xff -> map[index] = TYPE_WALL
					red == 0x00 && gre == 0xff && blu in 0..3 -> {
						map[index] = TYPE_START
						snakeStartPos = Point(x, y)
						snakeStartDir = Direction.values()[blu]
					}
					red == 0xff && gre == 0x00 && blu == 0x00 -> map[index] = TYPE_END
					red == 0x00 && gre == 0x00 && blu == 0xff -> map[index] = TYPE_LENGTH
					red == 0xff && gre == 0x7f && blu == 0x00 -> map[index] = TYPE_SPEED
					red == 0xff && gre == 0x00 && blu == 0xff -> map[index] = TYPE_SWITCH
					red == 0x00 && gre == 0x7f && blu == 0x7f -> map[index] = TYPE_ON_OFF_1
					red == 0x7f && gre == 0x00 && blu == 0x00 -> map[index] = TYPE_ON_OFF_2
					red == 0xff && gre == 0xff && blu == 0x00 -> map[index] = TYPE_SPEED_DOWN
					red == 0x00 && gre == 0x7f && blu == 0xff -> map[index] = TYPE_LENGTH_DOWN
					red == 0x00 && gre == 0x7f && blu == 0x00 -> map[index] = TYPE_APPLE
					red in 0..3 && gre == 0xff && blu == 0x7f -> {
						map[index] = TYPE_APPLE_GATE.or(red.shl(8))
					}
					red == 0x7f && gre in 0..3 && blu == 0x7f -> {
						map[index] = TYPE_ONE_WAY.or(gre.shl(8))
					}
					else -> map[index] = TYPE_EMPTY
				}
			}
		}
	}

	fun toMap(): Map {
		return Map(width, height, map.clone(), data.palette, data.wrap, true)
	}
}
