package com.gnarly.game

import com.gnarly.engine.Camera
import com.gnarly.engine.Texture
import com.gnarly.engine.Vector
import com.gnarly.engine.Window
import org.lwjgl.glfw.GLFW.*

class Button {
	var x = 0.0f
	var y = 0.0f
	var width = 0.0f
	var height = 0.0f

	companion object {
		val UNPRESS = 0
		val PRESS = 1
		val HOLD = 2
	}

	var state: Int = UNPRESS
		private set

	fun update(window: Window, camera: Camera, x: Float, y: Float, width: Float, height: Float) {
		this.x = x
		this.y = y
		this.width = width
		this.height = height

		if (window.mouse(GLFW_MOUSE_BUTTON_1) >= GLFW_PRESS) {
			val mouseCoord = window.getMousePos() * Vector(camera.width / window.width, camera.height / window.height)
			mouseCoord.y = camera.height - mouseCoord.y

			state = if (contains(mouseCoord)) {
				if (state == UNPRESS) PRESS else HOLD
			} else {
				UNPRESS
			}
		}
	}

	fun render(camera: Camera, texture: Texture, time: Float) {
		texture.bind()
		Assets.textureShader.enable().setMVP(camera.projection(), camera.model(x, y, width, height))
		Assets.rect.render()
	}

	fun contains(coord: Vector): Boolean {
		return coord.x >= this.x && coord.y >= this.y && coord.x < this.x + width && coord.y < this.y + height
	}
}
