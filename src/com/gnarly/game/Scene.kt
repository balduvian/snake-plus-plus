package com.gnarly.game

import com.gnarly.engine.Camera
import com.gnarly.engine.Window
import kotlin.reflect.KClass

interface Scene {
	fun update(window: Window, camera: Camera, delta: Float)

	fun swapScene(): KClass<*>?

	fun render(window: Window, camera: Camera, delta: Float)
}
