package com.gnarly.game

import com.gnarly.engine.Camera
import com.gnarly.engine.Texture
import com.gnarly.engine.Vector
import com.gnarly.engine.Window
import com.gnarly.engine.audio.Sound
import org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE
import org.lwjgl.glfw.GLFW.GLFW_PRESS
import kotlin.math.pow
import kotlin.math.sin
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
		0.848f, 0.288f, 0.078f,
		-0.142f, 0.188f, 0.068f,
		0.478f, 2.458f, 3.138f,
		0.348f, -0.222f, -0.722f,
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

	var cameraShake = 0.0f

	var time = 0.0f
	var moveTime = 0.0f

	init {
		startMap(0)
	}

	private fun startMap(levelIndex: Int) {
		music?.stop()

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
		Assets.deathMusic.stop()

		countdown = Countdown(3.0f)
		state = STATE_COUNTDOWN
		time = 0.0f
		moveTime = 0.0f
		cameraShake = 0.0f
	}

	fun timePerMove() = 1.0f / tilesPerSecond
	fun moveAlong() = moveTime * tilesPerSecond

	override fun update(window: Window, camera: Camera, delta: Float) {
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

		} else if (state == STATE_RETRY || state == STATE_WIN) {
			val texture = if (state == STATE_RETRY) Assets.retryTexture else Assets.nextTexture

			val buttonBox = TextureBox.fromTexture(texture)
				.setCenterX(camera.width / 2.0f)
				.setCenterY(camera.height * 0.75f)
			button.update(window, camera, buttonBox.x, buttonBox.y, buttonBox.width, buttonBox.height)

			if (button.state == Button.PRESS || window.key(GLFW_KEY_SPACE) >= GLFW_PRESS) {
				if (state == STATE_RETRY) {
					startMap(levelIndex)
				} else {
					startMap(levelIndex + 1)
				}
			}
		}

		val cameraOffset = Vector((Random.nextFloat() * 2.0f - 1.0f) * cameraShake, (Random.nextFloat() * 2.0f - 1.0f) * cameraShake)
		camera.setCenter(snake.getCameraPos(moveAlong()) + cameraOffset)

		cameraShake -= delta
		if (cameraShake < 0.0f) cameraShake = 0.0f
	}

	fun renderDark(camera: Camera, opacity: Float) {
		Assets.colorShader.enable().setMVP(camera.getMP(0.0f, 0.0f, camera.width, camera.height))
		Assets.colorShader.setColor(0.0f, 0.0f, 0.0f, opacity)
		Assets.rect.render()
	}

	override fun render(window: Window, camera: Camera, delta: Float) {
		map.render(camera, time, levelDataTexture, if (state == STATE_RETRY) deadPalette else null)
		snake.render(camera, moveAlong())

		val reminderScale = sin(0.5f * Math.PI.toFloat() * time) / 8.0f + 0.875f
		val reminderBox = TextureBox.fromTexture(Assets.spaceContinueTexture)
			.scale(reminderScale)
			.setCenterX(camera.width / 2.0f)
			.setCenterY(camera.height * 0.25f)

		if (state == STATE_COUNTDOWN) {
			renderDark(camera, (0.7f * countdown.time.pow(1.0f / 3.0f)).coerceAtMost(1.0f))
			countdown.render(camera, camera.width / 2.0f, camera.height / 2.0f, 5.0f, 5.0f)

		} else if (state == STATE_RETRY) {
			button.render(camera, Assets.retryTexture, time)

			Assets.spaceRetryTexture.bind()
			Assets.textureShader.enable().setMVP(camera.getMP(reminderBox))
			Assets.rect.render()

		} else if (state == STATE_WIN) {
			button.render(camera, Assets.nextTexture, time)

			Assets.spaceContinueTexture.bind()
			Assets.textureShader.enable().setMVP(camera.getMP(reminderBox))
			Assets.rect.render()
		}
	}

	override fun swapScene(): KClass<*>? {
		return if (shouldSwitch) Menu::class else null
	}
}
