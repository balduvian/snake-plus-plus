package com.gnarly.game

import com.gnarly.engine.Window
import com.gnarly.engine.audio.ALManagement
import org.lwjgl.glfw.GLFW.*

fun main(args: Array<String>) {
	Global.parseArgs(args)

	var lastTime: Long

	val al = ALManagement()

	val window = Window(1600, 900, "Snake++", true, true, true, false)

	Assets.init()

	var scene: Scene = if (Global.DEBUG_MODE) GamePanel() else Menu()
	scene.resized(window, window.width, window.height)

	lastTime = System.nanoTime()

	while (!window.shouldClose()) {
		val curTime = System.nanoTime()
		val delta = ((curTime - lastTime) / 1000000000.0f).coerceAtMost(0.25f)
		lastTime = curTime

		/* update */
		window.update()
		if (window.wasResized()) {
			scene.resized(window, window.width, window.height)
		}

		if (window.key(GLFW_KEY_ESCAPE) == GLFW_PRESS) {
			window.close()
			break
		}
		if (window.key(GLFW_KEY_F11) == GLFW_PRESS) {
			window.setFullScreen(!window.getFullScreen())
			scene.resized(window, window.width, window.height)
		}

		scene.update(window, delta)

		/* render */
		window.clear()
		scene.render(window, delta)
		window.swap()

		/* swap */
		val swap = scene.swapScene()
		if (swap == Menu::class) {
			scene = Menu()
			scene.resized(window, window.width, window.height)
		} else if (swap == GamePanel::class) {
			scene = GamePanel()
			scene.resized(window, window.width, window.height)
		}
	}

	al.destroy()
	Window.terminate()
}