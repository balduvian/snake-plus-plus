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
		Assets.menuMusic.play(true)
		Assets.menuMusic.setVolume(1.0f)
	}

	override fun update(window: Window, camera: Camera, delta: Float) {
		time += delta
		camCenter.setAdd(camVel * delta)

		val playBox = TextureBox.fromTexture(Assets.playTexture)
			.setCenterX(camera.width / 2.0f)
			.setCenterY(camera.height * (1.0f / 3.0f))

		play.update(window, camera, playBox.x, playBox.y, playBox.width, playBox.height)

		if (play.state >= Button.PRESS) {
			Assets.menuMusic.stop()
			shouldSwitch = true
		}

		camera.scale = 1.0f
		camera.rotation += delta * 0.1f
		camera.setCenter(camCenter.x, camCenter.y)
	}

	override fun swapScene(): KClass<*>? {
		return if (shouldSwitch) return GamePanel::class else null
	}

	override fun render(window: Window, camera: Camera, delta: Float) {
		glDisable(GL_DEPTH_TEST)
		map.render(camera, time, dataTexture)

		val logoBox = TextureBox.fromTexture(Assets.logoTexture)
			.setCenterX(camera.width / 2.0f)
			.setTop(camera.height)
		Assets.logoTexture.bind()
		Assets.textureShader.enable().setMVP(camera.projection(), camera.boxModel(logoBox))
		Assets.rect.render()

		play.render(camera, Assets.playTexture, time)
	}
}
