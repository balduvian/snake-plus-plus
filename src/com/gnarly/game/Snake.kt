package com.gnarly.game

import com.gnarly.engine.Camera
import com.gnarly.engine.Vector
import com.gnarly.engine.Window
import org.lwjgl.glfw.GLFW.*

class Snake(
	var futureDirection: Direction,
    var lengthToAdd: Int,
    initialPoint: Point
) {
	var fallbackPoint = initialPoint

	data class Segment(var direction: Direction, var point: Point)

	private var bufferedDirection: Direction? = null
	private val snake: ArrayList<Segment> = arrayListOf(Segment(futureDirection, initialPoint))

	fun update(window: Window, allowInput: Boolean, addSegment: Boolean) {
		if (addSegment && snake.isNotEmpty()) {
			if (lengthToAdd >= 0) {
				val newDirection = bufferedDirection ?: futureDirection
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

			bufferedDirection = null
		}

		if (allowInput) {
			val inputDirection = if (window.key(GLFW_KEY_LEFT) >= GLFW_PRESS || window.key(GLFW_KEY_A) >= GLFW_PRESS)
				Direction.LEFT
			else if (window.key(GLFW_KEY_RIGHT) >= GLFW_PRESS || window.key(GLFW_KEY_D) >= GLFW_PRESS)
				Direction.RIGHT
			else if (window.key(GLFW_KEY_UP) >= GLFW_PRESS || window.key(GLFW_KEY_W) >= GLFW_PRESS)
				Direction.UP
			else if (window.key(GLFW_KEY_DOWN) >= GLFW_PRESS || window.key(GLFW_KEY_S) >= GLFW_PRESS)
				Direction.DOWN
			else null

			if (inputDirection != null && inputDirection != futureDirection.inverse()) {
				bufferedDirection = inputDirection
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

	fun render(camera: Camera, percent: Float) {
		Assets.colorShader.enable()
		Assets.colorShader.setColor(0.0f, 1.0f, 0.0f, 1.0f)

		/* render future */
		if (lengthToAdd >= 0 && snake.isNotEmpty()) {
			val futurePoint = snake.first().point + futureDirection.point

			Assets.colorShader.setMVP(
				camera.getMVPRotateCenter(
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
			Assets.colorShader.setMVP(camera.getMVP(segment.point.x.toFloat(), segment.point.y.toFloat(), 1.0f, 1.0f))
			Assets.rect.render()
		}

		/* render tail */
		snake.lastOrNull()?.let { (tailDirection, tailPoint) ->
			if (lengthToAdd > 0) {
				Assets.colorShader.setMVP(camera.getMVP(tailPoint.x.toFloat(), tailPoint.y.toFloat(), 1.0f, 1.0f))
			} else {
				Assets.colorShader.setMVP(camera.getMVPRotateCenter(tailPoint.x.toFloat(), tailPoint.y.toFloat(), 1.0f - percent, 1.0f, tailDirection.inverse().rotation))
			}
			Assets.rect.render()
		}
	}
}
