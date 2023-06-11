package com.gnarly.game

import com.gnarly.engine.Camera
import com.gnarly.engine.Vector
import com.gnarly.engine.Window
import org.joml.Matrix4f
import org.lwjgl.glfw.GLFW.*

class Snake(
	var futureDirection: Direction,
    var lengthToAdd: Int,
    initialPoint: Point
) {
	class InputBuffer() {
		var primary: Direction? = null
		var secondary: Direction? = null

		fun pop(): Direction? {
			if (primary != null) {
				val ret = primary
				primary = null
				return ret
			} else if (secondary != null) {
				val ret = secondary
				secondary = null
				return ret
			}

			return null
		}
	}

	var fallbackPoint = initialPoint

	data class Segment(var direction: Direction, var point: Point)

	private val inputBuffer = InputBuffer()
	private val snake: ArrayList<Segment> = arrayListOf(Segment(futureDirection, initialPoint))

	fun update(window: Window, allowInput: Boolean, addSegment: Boolean, map: Map) {
		if (addSegment && snake.isNotEmpty()) {
			if (lengthToAdd >= 0) {
				val newDirection = inputBuffer.pop() ?: futureDirection
				val newPoint = snake.first().point + futureDirection.point

				snake.add(0, Segment(newDirection, newPoint))

				fallbackPoint = newPoint
				futureDirection = newDirection

			} else {
				++lengthToAdd
			}

			if (lengthToAdd <= 0) {
				snake.removeLastOrNull()
			} else {
				--lengthToAdd
			}
		}

		if (allowInput) {
			val inputtedDirections = arrayOf(
				window.key(GLFW_KEY_RIGHT) >= GLFW_PRESS || window.key(GLFW_KEY_D) >= GLFW_PRESS,
				window.key(GLFW_KEY_UP) >= GLFW_PRESS || window.key(GLFW_KEY_W) >= GLFW_PRESS,
				window.key(GLFW_KEY_LEFT) >= GLFW_PRESS || window.key(GLFW_KEY_A) >= GLFW_PRESS,
				window.key(GLFW_KEY_DOWN) >= GLFW_PRESS || window.key(GLFW_KEY_S) >= GLFW_PRESS
			)

			val primaryLeft = futureDirection.rotateLeft()
			val primaryRight = futureDirection.rotateRight()

			/* trying to turn in both directions at the same time */
			if (inputtedDirections[primaryLeft.ordinal] && inputtedDirections[primaryRight.ordinal]) {
				return
			}

			val primary = if (inputtedDirections[primaryLeft.ordinal]) primaryLeft
				else if (inputtedDirections[primaryRight.ordinal]) primaryRight
				else null

			if (inputBuffer.primary == null) {
				if (primary != null) {
					val wallCheckPos = snake.first().point + futureDirection.point + primary.point
					if (map.access(wallCheckPos.x, wallCheckPos.y) != MapTemplate.TYPE_WALL) {
						inputBuffer.primary = primary
					}
				}
			} else {
				primary?.let { inputBuffer.primary = it }
			}
		}
	}

	fun bodyContains(point: Point): Boolean {
		for (i in 1 until snake.size) if (point == snake[i].point) return true
		return false
	}

	fun lengthen(addedLength: Int) {
		lengthToAdd += addedLength
	}

	fun reduceToNothing() {
		lengthToAdd = -snake.size
	}

	fun getCameraPos(percent: Float): Vector {
		return if (lengthToAdd < 0 || snake.isEmpty()) {
			(snake.firstOrNull()?.point ?: fallbackPoint).toVector() + Vector(0.5f, 0.5f)
		} else {
			(snake.firstOrNull()?.point ?: fallbackPoint).toVector() + Vector(0.5f, 0.5f) + (futureDirection.point.toVector() * percent)
		}
	}

	fun getOn() = snake.first().point
	fun getMovingInto() = snake.first().point + futureDirection.point

	private fun Camera.snakeEndModel(x: Float, y: Float, width: Float, height: Float, rotation: Float): Matrix4f {
		return model.translation(x, y, 0.0f).translate(0.5f, 0.5f, 0.0f).rotateZ(rotation).translate(-0.5f, -0.5f, 0.0f).scale(width, height, 0f)
	}

	fun render(camera: Camera, percent: Float) {
		Assets.colorShader.enable()
		Assets.colorShader.setColor(0.0f, 1.0f, 0.0f, 1.0f)

		/* render future */
		if (lengthToAdd >= 0 && snake.isNotEmpty()) {
			val futurePoint = snake.first().point + futureDirection.point

			Assets.colorShader.setMVP(
				camera.projectionView(),
				camera.snakeEndModel(
					futurePoint.x.toFloat(),
					futurePoint.y.toFloat(),
					percent,
					1.0f,
					futureDirection.rotation
				)
			)
			Assets.rect.render()
		}

		/* render body segments */
		for (i in 0..snake.lastIndex - 1) {
			val segment = snake[i]
			Assets.colorShader.setMVP(
				camera.projectionView(),
				camera.model(segment.point.x.toFloat(), segment.point.y.toFloat(), 1.0f, 1.0f)
			)
			Assets.rect.render()
		}

		/* render tail */
		snake.lastOrNull()?.let { (tailDirection, tailPoint) ->
			if (lengthToAdd > 0) {
				Assets.colorShader.setMVP(camera.projectionView(), camera.model(tailPoint.x.toFloat(), tailPoint.y.toFloat(), 1.0f, 1.0f))
			} else {
				Assets.colorShader.setMVP(camera.projectionView(), camera.snakeEndModel(tailPoint.x.toFloat(), tailPoint.y.toFloat(), 1.0f - percent, 1.0f, tailDirection.inverse().rotation))
			}
			Assets.rect.render()
		}
	}
}
