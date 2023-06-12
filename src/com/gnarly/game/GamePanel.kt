package com.gnarly.game

import com.gnarly.engine.Camera
import com.gnarly.engine.Texture
import com.gnarly.engine.Vector
import com.gnarly.engine.Window
import com.gnarly.engine.audio.Sound
import org.lwjgl.glfw.GLFW.*
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.tan
import kotlin.random.Random
import kotlin.reflect.KClass

class GamePanel : Scene {
	companion object {
		const val STATE_COUNTDOWN = 0
		const val STATE_GOING = 1
		const val STATE_RETRY = 2
		const val STATE_WIN = 3
	}

	val deadPalette = floatArrayOf(
		0.728f, 0.078f, 0.228f, 0.458f, 0.008f, -0.232f, -2.362f, -1.522f, -0.062f, 1.968f, 1.968f, -0.752f,
	)

	val winPalette = floatArrayOf(
		-1.572f, 1.088f, 0.500f,0.000f, 0.500f, 0.500f, 0.000f, 0.333f, 0.500f, 0.000f, 0.667f, 0.500f,
	)

	lateinit var countdown: Countdown
	var state: Int = 0
	var levelIndex = 0
	lateinit var map: Map
	val button: Button = Button()

	var music: Sound? = null
	lateinit var snake: Snake
	val levelDataTexture = Texture.empty()
	private var tilesPerSecond = 1
	private var shouldSwitch = false

	var winTimer = 0.0f
	var cameraShake = 0.0f

	var time = 0.0f
	var moveTime = 0.0f

	val uiCamera = Camera()
	val mapCamera = Camera()

	data class Transition(var time: Float, val toLevel: Int, var triggered: Boolean)
	var transition: Transition? = null

	init {
		startMap(0)
	}

	override fun resized(window: Window, width: Int, height: Int) {
		val ratio = width.toFloat() / height.toFloat()

		val cameraWidth = ratio * Util.FULL_CAMERA_HEIGHT

		uiCamera.setDims(0.0f, cameraWidth, 0.0f, Util.FULL_CAMERA_HEIGHT)
		mapCamera.setDims(-cameraWidth / 2.0f, cameraWidth / 2.0f, -Util.FULL_CAMERA_HEIGHT / 2.0f, Util.FULL_CAMERA_HEIGHT / 2.0f)
	}

	private fun startMap(levelIndex: Int) {
		music?.stop()
		Assets.deathMusic.stop()
		Assets.winMusic.stop()

		if (levelIndex == Assets.levelMapTemplates.size) {
			shouldSwitch = true
			return
		}

		this.levelIndex = levelIndex

		Assets.levelMapTemplates[levelIndex].let { mapTemplate ->
			map = mapTemplate.toMap()
			val music = mapTemplate.music
			this.music = music
			snake = Snake(mapTemplate.snakeStartDir, 10, mapTemplate.snakeStartPos)
			tilesPerSecond = mapTemplate.data.snakeSpeed
		}

		Assets.countdownMusic.play(false)

		countdown = Countdown(3.0f)
		state = STATE_COUNTDOWN
		time = 0.0f
		moveTime = 0.0f
		cameraShake = 0.0f
		winTimer = 0.0f
	}

	private fun startTransition(levelIndex: Int) {
		transition = Transition(0.0f, levelIndex, false)
	}

	fun timePerMove() = 1.0f / tilesPerSecond
	fun moveAlong() = moveTime * tilesPerSecond

	override fun update(window: Window, delta: Float) {
		time += delta

		if (state != STATE_COUNTDOWN) {
			moveTime += delta

			val addSegment = moveTime >= timePerMove()
			snake.update(window, state == STATE_GOING, addSegment, map)
			if (addSegment) {
				moveTime -= timePerMove()

				if (state == STATE_GOING) {
					val on = snake.getOn()

					when (map.access(on.x, on.y)) {
						MapTemplate.TYPE_END -> {
							snake.reduceToNothing()
							music?.stop()
							Assets.winMusic.play(false)
							state = STATE_WIN
						}
						MapTemplate.TYPE_LENGTH -> {
							map.map[map.indexOf(on.x, on.y)] = MapTemplate.TYPE_EMPTY
							snake.lengthen(10)
						}
						MapTemplate.TYPE_SPEED -> {
							map.map[map.indexOf(on.x, on.y)] = MapTemplate.TYPE_EMPTY
							tilesPerSecond += 2
						}
					}
				}
			}
		}

		if (state == STATE_COUNTDOWN) {
			countdown.update(delta)
			if (countdown.isDone()) {
				state = STATE_GOING
				music?.play(true)
			}

		} else if (state == STATE_GOING) {
			val movingInto = snake.getMovingInto()

			if (snake.bodyContains(movingInto)) {
				state = STATE_RETRY
				snake.reduceToNothing()
				cameraShake = tilesPerSecond / 10.0f
				music?.stop()
				Assets.deathMusic.play(false)

			} else when (map.access(movingInto.x, movingInto.y)) {
				MapTemplate.TYPE_WALL -> {
					state = STATE_RETRY
					snake.reduceToNothing()
					cameraShake = tilesPerSecond / 10.0f
					music?.stop()
					Assets.deathMusic.play(false)
				}
			}

			if (window.key(GLFW_KEY_R) >= GLFW_PRESS && transition == null) {
				startTransition(levelIndex)
			}

		} else if (state == STATE_RETRY || state == STATE_WIN) {
			winTimer += delta

			val texture = if (state == STATE_RETRY) Assets.retryTexture else Assets.nextTexture

			val buttonBox = TextureBox.fromTexture(texture)
				.setCenterX(uiCamera.width / 2.0f)
				.setCenterY(uiCamera.height * 0.75f)
			button.update(window, uiCamera, buttonBox.x, buttonBox.y, buttonBox.width, buttonBox.height)

			if (transition == null && (button.state == Button.PRESS || window.key(GLFW_KEY_SPACE) >= GLFW_PRESS)) {
				if (state == STATE_RETRY) {
					startTransition(levelIndex)
				} else {
					startTransition(levelIndex + 1)
				}
			}
		}

		transition?.let { transition ->
			transition.time += delta
			if (!transition.triggered && transition.time >= 0.5f) {
				transition.triggered = true
				startMap(transition.toLevel)
			}

			if (transition.time >= 1.0f) {
				this.transition = null
			}
		}

		cameraShake -= delta
		if (cameraShake < 0.0f) cameraShake = 0.0f

		val cameraOffset = Vector((Random.nextFloat() * 2.0f - 1.0f) * cameraShake, (Random.nextFloat() * 2.0f - 1.0f) * cameraShake)

		val cameraRotation = (PI.toFloat() / 4.0f) - 1.0f / (winTimer + (4.0f / PI.toFloat()))
		val cameraScale = 0.5f + 1.0f / (winTimer + 2.0f)
		mapCamera.scale = cameraScale
		mapCamera.rotation = cameraRotation

		mapCamera.setPosition(snake.getCameraPos(moveAlong()) + cameraOffset)

		mapCamera.update()
		uiCamera.update()
	}

	fun renderDark(camera: Camera, opacity: Float) {
		Assets.colorShader.enable().setMVP(camera.projection(), camera.model(0.0f, 0.0f, camera.width, camera.height))
		Assets.colorShader.setColor(0.0f, 0.0f, 0.0f, opacity)
		Assets.rect.render()
	}

	override fun render(window: Window, delta: Float) {
		map.render(mapCamera, time, levelDataTexture, if (state == STATE_RETRY) deadPalette else if (state == STATE_WIN) winPalette else null)
		snake.render(mapCamera, moveAlong())

		val reminderScale = sin(0.5f * Math.PI.toFloat() * time) / 8.0f + 0.875f
		val reminderBox = TextureBox.fromTexture(Assets.spaceContinueTexture)
			.scale(reminderScale)
			.setCenterX(uiCamera.width / 2.0f)
			.setCenterY(uiCamera.height * 0.25f)

		if (state == STATE_COUNTDOWN) {
			renderDark(uiCamera, (0.7f * countdown.time.pow(1.0f / 3.0f)).coerceAtMost(1.0f))
			countdown.render(uiCamera, uiCamera.width / 2.0f, uiCamera.height / 2.0f, 5.0f, 5.0f)

		} else if (state == STATE_RETRY) {
			button.render(uiCamera, Assets.retryTexture, time)

			Assets.spaceRetryTexture.bind()
			Assets.textureShader.enable().setMVP(uiCamera.projection(), uiCamera.boxModel(reminderBox))
			Assets.rect.render()

		} else if (state == STATE_WIN) {
			button.render(uiCamera, Assets.nextTexture, time)

			Assets.spaceContinueTexture.bind()
			Assets.textureShader.enable().setMVP(uiCamera.projection(), uiCamera.boxModel(reminderBox))
			Assets.rect.render()
		}

		transition?.let { transition ->
			val angle = PI.toFloat() / 8.0f
			val extent = uiCamera.height * tan(angle)

			val halfWidth = uiCamera.width / 2.0f + extent

			val centerX = Util.interp(-halfWidth, uiCamera.width + halfWidth, transition.time)

			/* main box */
			Assets.colorShader.enable().setMVP(uiCamera.projection(), uiCamera.modelCentered(centerX, uiCamera.height / 2.0f, uiCamera.width, uiCamera.height))
			Assets.colorShader.setColor(0.0f, 0.0f, 0.0f, 1.0f)
			Assets.rect.render()

			/* behind leading edge */
			Assets.colorShader.enable().setMVP(
				uiCamera.projection(),
				uiCamera.model.translation(centerX + uiCamera.width / 2.0f, 0.0f, 0.0f).scale(uiCamera.height)
			)
			Assets.colorShader.setColor(0.0f, 0.0f, 0.0f, 1.0f)
			Assets.transitionTriangle.render()

			/* behind trailing edge */
			Assets.colorShader.enable().setMVP(
				uiCamera.projection(),
				uiCamera.model.translation(centerX - uiCamera.width / 2.0f, uiCamera.height, 0.0f).rotateZ(PI.toFloat()).scale(uiCamera.height))
			Assets.colorShader.setColor(0.0f, 0.0f, 0.0f, 1.0f)
			Assets.transitionTriangle.render()

			/* leading edge */
			val leadingVertex = Vector(centerX + uiCamera.width / 2.0f, 0.0f) + Vector(-1.0f, -1.0f).rotate(-angle)

			Assets.transitionTexture.bind()
			Assets.textureShader.enable().setMVP(uiCamera.projection(), uiCamera.model(leadingVertex.x, leadingVertex.y, 1.0f, uiCamera.height * 2.0f, -angle))
			Assets.rect.render()

			/* trailing edge */
			val trailingVertex = Vector(centerX - uiCamera.width / 2.0f, uiCamera.height) + Vector(-1.0f, -1.0f).rotate(PI.toFloat() - angle)

			Assets.transitionTexture.bind()
			Assets.textureShader.enable().setMVP(uiCamera.projection(), uiCamera.model(trailingVertex.x, trailingVertex.y, 1.0f, uiCamera.height * 2.0f, PI.toFloat() - angle))
			Assets.rect.render()
		}
	}

	override fun swapScene(): KClass<*>? {
		return if (shouldSwitch) Menu::class else null
	}
}
