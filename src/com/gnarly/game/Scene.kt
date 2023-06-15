package com.gnarly.game

import com.gnarly.engine.Window
import kotlin.reflect.KClass

interface Scene {
	fun resized(window: Window, width: Int, height: Int)

	fun update(window: Window, delta: Float)

	fun swapScene(): KClass<*>?

	fun render(window: Window, delta: Float)
}
