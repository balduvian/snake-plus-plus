package com.gnarly.engine

import org.joml.Matrix4f
import org.joml.Vector3f
import kotlin.math.absoluteValue

class Camera {
	private val projection = Matrix4f()
	private val view = Matrix4f()
	private val viewProjection = Matrix4f()
	val model = Matrix4f()
	var width = 0.0f
		private set
	var height = 0.0f
		private set

	private val position = Vector3f()
	var rotation = 0.0f
	var scale = 1.0f

	fun setDims(left: Float, right: Float, down: Float, up: Float): Camera {
		this.width = (right - left).absoluteValue
		this.height = (up - down).absoluteValue
		projection.setOrtho(left, right, down, up, 0f, 1f)
		return this
	}

	fun update() {
		view.scaling(1.0f / scale)
			.rotateZ(-rotation)
			.translate(-position.x, -position.y, 0.0f)

		projection.mul(view, viewProjection)
	}

	var x: Float
		get() = position.x
		set(x) {
			position.x = x
		}
	var y: Float
		get() = position.y
		set(y) {
			position.y = y
		}

	fun getPosition(): Vector3f {
		return Vector3f(position)
	}

	fun setPosition(x: Float, y: Float) {
		position[x, y] = position.z
	}

	fun setPosition(vector: Vector) {
		this.position.x = vector.x
		this.position.y = vector.y
	}

	fun translate(x: Float, y: Float, z: Float) {
		position.add(x, y, z)
	}

	fun translate(transform: Vector3f?) {
		position.add(transform)
	}

	fun rotate(angle: Float) {
		rotation += angle
	}

	fun projectionView(): Matrix4f {
		return viewProjection
	}

	fun projection(): Matrix4f {
		return projection
	}

	/* MODEL */

	fun model(x: Float, y: Float, width: Float, height: Float): Matrix4f {
		return model.translation(x, y, 0.0f).scale(width, height, 0f)
	}

	fun model(x: Float, y: Float, scale: Float): Matrix4f {
		return model.translation(x, y, 0.0f).scale(scale)
	}

	fun modelRotate(x: Float, y: Float, scale: Float): Matrix4f {
		return model.translation(x, y, 0.0f).scale(scale)
	}

	fun model(x: Float, y: Float, width: Float, height: Float, rotation: Float): Matrix4f {
		return model.translation(x, y, 0.0f).rotateZ(rotation).scale(width, height, 0f)
	}

	fun modelRotateCentered(x: Float, y: Float, width: Float, height: Float, rotation: Float): Matrix4f {
		return model.translation(x, y, 0.0f).scale(width, height, 0f).translate(0.5f, 0.5f, 0.0f).rotateZ(rotation).translate(-0.5f, -0.5f, 0.0f)
	}

	fun modelCentered(x: Float, y: Float, width: Float, height: Float): Matrix4f {
		return model.translation(x, y, 0.0f).scale(width, height, 0f).translate(-0.5f, -0.5f, 0.0f)
	}
}
