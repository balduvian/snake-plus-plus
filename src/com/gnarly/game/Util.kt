package com.gnarly.game

object Util {
	fun posMod(a: Int, b: Int): Int {
		return ((a % b) + b) % b
	}
}
