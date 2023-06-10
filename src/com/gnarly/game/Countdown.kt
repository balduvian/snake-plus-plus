package com.gnarly.game

import com.gnarly.engine.Camera
import kotlin.math.floor

class Countdown(var time: Float) {
	fun update(delta: Float) {
		time -= delta
	}

	fun isDone(): Boolean {
		return time <= 0.0f
	}

	fun render(camera: Camera, x: Float, y: Float, width: Float, height: Float) {
		val number = floor(time).toInt().coerceIn(0..2)
		Assets.countTextures[number].bind()

		val scale = (time % 1.0f) * 0.75f + 0.25f

		Assets.textureShader.enable().setMVP(camera.getMPCentered(
			camera.width / 2.0f,
			camera.height / 2.0f,
			width * scale,
			height * scale
		))
		Assets.rect.render()
	}
}
