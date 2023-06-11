package com.gnarly.game

import com.gnarly.engine.Vector
import com.gnarly.game.Util.posMod
import kotlin.math.PI

enum class Direction(val point: Point, val rotation: Float) {
	RIGHT(Point(1, 0), 0.0f),
	UP(Point(0, 1), PI.toFloat() / 2.0f),
	LEFT(Point(-1, 0), PI.toFloat()),
	DOWN(Point(0, -1), 3.0f * PI.toFloat() / 2.0f);

	fun inverse() = values()[(ordinal + 2) % 4]
	fun rotateLeft() = values()[(ordinal + 1) % 4]
	fun rotateRight() = values()[posMod((ordinal - 1), 4)]
}

data class Point(val x: Int, val y: Int) {
	operator fun plus(other: Point): Point {
		return Point(x + other.x, y + other.y)
	}

	operator fun minus(other: Point): Point {
		return Point(x - other.x, y - other.y)
	}

	fun toVector() = Vector(x.toFloat(), y.toFloat())
	fun getDirection() = Direction.values().find { this == it.point }
}
