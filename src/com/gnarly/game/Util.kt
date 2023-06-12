package com.gnarly.game

object Util {
	const val FULL_CAMERA_HEIGHT = 18f

	fun posMod(a: Int, b: Int): Int {
		return ((a % b) + b) % b
	}

	fun interp(low: Float, high: Float, along: Float): Float {
		return (high - low) * along + low
	}
}
