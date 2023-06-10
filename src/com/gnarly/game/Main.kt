package com.gnarly.game

import com.gnarly.engine.Camera
import com.gnarly.engine.Window
import com.gnarly.engine.audio.ALManagement
import org.lwjgl.glfw.GLFW.*

const val FULL_CAMERA_HEIGHT = 18f

fun main() {
	var lastTime: Long

	val al = ALManagement()

	val window = Window(1600, 900, "Snake++", true, true, true, false)
	val camera = Camera().setDims(32.0f, 18.0f)

	Assets.init()

	var scene: Scene = Menu()

	lastTime = System.nanoTime()

	while (!window.shouldClose()) {
		val curTime = System.nanoTime()
		val delta = ((curTime - lastTime) / 1000000000.0f).coerceAtMost(0.25f)
		lastTime = curTime

		/* update */
		window.update()
		if (window.wasResized()) {
			val windowRatio = window.width.toFloat() / window.height.toFloat()
			camera.setDims(windowRatio * FULL_CAMERA_HEIGHT, FULL_CAMERA_HEIGHT)
		}
		if (window.key(GLFW_KEY_ESCAPE) == GLFW_PRESS) {
			window.close()
			break
		}
		if (window.key(GLFW_KEY_F11) == GLFW_PRESS) {
			window.setFullScreen(!window.getFullScreen())
		}
		scene.update(window, camera, delta)
		camera.update()

		/* render */
		window.clear()
		scene.render(window, camera, delta)
		window.swap()

		/* swap */
		val swap = scene.swapScene()
		if (swap == Menu::class) {
			scene = Menu()
		} else if (swap == GamePanel::class) {
			scene = GamePanel()
		}
	}

	al.destroy()
	Window.terminate()
}