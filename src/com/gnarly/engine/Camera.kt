package com.gnarly.engine

import org.joml.Matrix4f
import org.joml.Vector3f

class Camera {
	private val projection = Matrix4f()
	private val view = Matrix4f()
	private val viewProjection = Matrix4f()
	val model = Matrix4f()
	private val mvp = Matrix4f()
	var width = 0.0f
		private set
	var height = 0.0f
		private set

	private val position = Vector3f()
	var rotation = 0.0f
	var scale = 1.0f

	fun setDims(width: Float, height: Float): Camera {
		this.width = width
		this.height = height
		projection.setOrtho(0.0f, width, 0.0f, height, 0f, 1f)
		return this
	}

	fun update() {
		val invScale = 1.0f / scale

		view.translation(
			-position.x * invScale,
			-position.y * invScale,
			0.0f
		)
			.translate(width / 2.0f, height / 2.0f, 0.0f)
			.scale(invScale)
			.translate(-width / 2.0f, -height / 2.0f, 0.0f)
			.translate(width / 2.0f + position.x, height / 2.0f + position.y, 0.0f)
			.rotateZ(-rotation)
			.translate(-width / 2.0f - position.x, -height / 2.0f - position.y, 0.0f)

		projection.mul(view, viewProjection)
	}

	//.rotateZ(-rotation)

	fun fledgeling(): Boolean {
		return width == 0.0f
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

	fun setPosition(position: Vector3f) {
		this.position.x = position.x
		this.position.y = position.y
	}

	fun setCenter(x: Float, y: Float) {
		position.x = x - width / 2.0f
		position.y = y - height / 2.0f
	}

	fun setCenter(vector: Vector) {
		position.x = vector.x - width / 2.0f
		position.y = vector.y - height / 2.0f
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

	fun modelRotateCentered(x: Float, y: Float, width: Float, height: Float, rotation: Float): Matrix4f {
		return model.translation(x, y, 0.0f).scale(width, height, 0f).translate(0.5f, 0.5f, 0.0f).rotateZ(rotation).translate(-0.5f, -0.5f, 0.0f)
	}

	fun modelCentered(x: Float, y: Float, width: Float, height: Float): Matrix4f {
		return model.translation(x, y, 0.0f).scale(width, height, 0f).translate(-0.5f, -0.5f, 0.0f)
	}
}
