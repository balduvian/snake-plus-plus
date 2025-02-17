package com.gnarly.game

import com.gnarly.engine.*
import com.gnarly.game.shader.BackgroundShader
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL46.*
import kotlin.math.PI
import kotlin.reflect.KClass

class Menu : Scene {
	val map: Map = Assets.menuMapTemplate.toMap()
	val play: Button = Button()
	val camVel = Vector(1.0f, 0.5f).setNormalize()
	val camCenter = Vector(0.0f, 0.0f)
	var shouldSwitch = false
	var backgroundShader = Assets.menuMapTemplate.backgroundShader
	val dataTexture = Texture.empty()
	var time = 0.0f

	var levelBuffer = FrameBuffer(2)

	val uiCamera = Camera()
	val mapCamera = Camera()

	init {
		Assets.menuMapTemplate.music?.play(true)
	}

	override fun resized(window: Window, width: Int, height: Int) {
		val ratio = width.toFloat() / height.toFloat()

		val cameraWidth = ratio * Util.FULL_CAMERA_HEIGHT

		uiCamera.setDims(0.0f, cameraWidth, 0.0f, Util.FULL_CAMERA_HEIGHT)
		mapCamera.setDims(-cameraWidth / 2.0f, cameraWidth / 2.0f, -Util.FULL_CAMERA_HEIGHT / 2.0f, Util.FULL_CAMERA_HEIGHT / 2.0f)

		levelBuffer.resize(width, height)
	}

	override fun update(window: Window, delta: Float) {
		time += delta
		camCenter.setAdd(camVel * delta)

		val playBox = TextureBox.fromTexture(Assets.playTexture)
			.setCenterX(uiCamera.width / 2.0f)
			.setCenterY(uiCamera.height * (1.0f / 3.0f))

		play.update(window, uiCamera, playBox.x, playBox.y, playBox.width, playBox.height)

		if (play.state >= Button.PRESS || window.key(GLFW_KEY_SPACE) == GLFW_PRESS) {
			Assets.menuMapTemplate.music?.stop()
			shouldSwitch = true
		}

		mapCamera.rotation += delta / (2.0f * PI.toFloat())
		mapCamera.setPosition(camCenter)
		mapCamera.update()
		uiCamera.update()
	}

	override fun swapScene(): KClass<*>? {
		return if (shouldSwitch) return GamePanel::class else null
	}

	override fun render(window: Window, delta: Float) {
		map.render(mapCamera, time, dataTexture, backgroundShader, null, levelBuffer)

		val logoBox = TextureBox.fromTexture(Assets.logoTexture)
			.setCenterX(uiCamera.width / 2.0f)
			.setTop(uiCamera.height)
		Assets.logoTexture.bind()
		Assets.textureShader.enable().setMVP(uiCamera.projection(), uiCamera.boxModel(logoBox))
		Assets.rect.render()

		play.render(uiCamera, Assets.playTexture, time)
	}
}
