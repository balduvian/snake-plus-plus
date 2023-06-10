package com.gnarly.game

import com.gnarly.engine.Camera
import com.gnarly.engine.Texture
import com.gnarly.engine.Vector
import com.gnarly.engine.Window
import org.lwjgl.opengl.GL46.*
import kotlin.math.abs
import kotlin.reflect.KClass

class Menu : Scene {
	val map: Map = Assets.menuMapTemplate.toMap()
	val play: Button = Button()
	val camVel = Vector(1.0f, 0.5f).setNormalize()
	val camCenter = Vector(0.0f, 0.0f)
	var shouldSwitch = false
	val dataTexture = Texture.empty()
	var time = 0.0f

	init {
		Assets.menuMapTemplate.writeDataTexture(dataTexture, wrap = true)
		Assets.menuMusic.play(true)
		Assets.menuMusic.setVolume(1.0f)
	}

	override fun update(window: Window, camera: Camera, delta: Float) {
		time += delta
		camCenter.setAdd(camVel * delta)

		if (camCenter.x < 0) {
			camCenter.x = 0.0f
			camVel.x = abs(camVel.x)
		} else if (camCenter.x > map.width) {
			camCenter.x = map.width.toFloat()
			camVel.x = -abs(camVel.x)
		}
		if (camCenter.y < 0) {
			camCenter.y = 0.0f
			camVel.y = abs(camVel.y)
		} else if (camCenter.y > map.height) {
			camCenter.y = map.height.toFloat()
			camVel.y = -abs(camVel.y)
		}

		val playBox = TextureBox.fromTexture(Assets.playTexture)
			.setCenterX(camera.width / 2.0f)
			.setCenterY(camera.height * (1.0f / 3.0f))

		play.update(window, camera, playBox.x, playBox.y, playBox.width, playBox.height)

		if (play.state >= Button.PRESS) {
			Assets.menuMusic.stop()
			shouldSwitch = true
		}

		camera.setCenter(camCenter.x, camCenter.y)
	}

	override fun swapScene(): KClass<*>? {
		return if (shouldSwitch) return GamePanel::class else null
	}

	override fun render(window: Window, camera: Camera, delta: Float) {
		glDisable(GL_DEPTH_TEST)
		map.render(camera, true, time, dataTexture)

		val logoBox = TextureBox.fromTexture(Assets.logoTexture)
			.setCenterX(camera.width / 2.0f)
			.setTop(camera.height)
		Assets.logoTexture.bind()
		Assets.textureShader.enable().setMVP(camera.getMP(logoBox))
		Assets.rect.render()

		play.render(camera, Assets.playTexture, time)
	}
}
