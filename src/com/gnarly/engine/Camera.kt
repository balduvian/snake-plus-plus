package com.gnarly.engine

import org.joml.Matrix4f
import org.joml.Vector3f
import kotlin.random.Random

class Camera {
	private val projection = Matrix4f()
	private val view = Matrix4f()
	private val viewProjection = Matrix4f()
	private val model = Matrix4f()
	private val mvp = Matrix4f()
	var width = 0.0f
		private set
	var height = 0.0f
		private set
	private val position = Vector3f()
	private var rotation = 0.0f

	fun setDims(width: Float, height: Float): Camera {
		this.width = width
		this.height = height
		projection.setOrtho(0f, width, 0f, height, 0f, 1f)
		return this
	}

	fun update() {
		view.translation(position.negate(Vector3f())).rotateZ(-rotation)
		projection.mul(view, viewProjection)
	}

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

	fun setRotation(angle: Float) {
		rotation = angle
	}

	fun rotate(angle: Float) {
		rotation += angle
	}

	/* MODEL */

	private fun getModel(x: Float, y: Float, width: Float, height: Float, model: Matrix4f): Matrix4f {
		return model.translation(x, y, 0.0f).scale(width, height, 0f)
	}

	private fun getModelRotateCenter(x: Float, y: Float, width: Float, height: Float, rotation: Float, model: Matrix4f): Matrix4f {
		return model.translation(x, y, 0.0f).translate(0.5f, 0.5f, 0.0f).rotateZ(rotation).translate(-0.5f, -0.5f, 0.0f).scale(width, height, 0f)
	}

	private fun getModelCentered(x: Float, y: Float, width: Float, height: Float, model: Matrix4f): Matrix4f {
		return model.translation(x, y, 0.0f).scale(width, height, 0f).translate(-0.5f, -0.5f, 0.0f)
	}

	private fun internalGetMVP(model: Matrix4f, viewProjection: Matrix4f, mvp: Matrix4f): Matrix4f {
		return viewProjection.mul(model, mvp)
	}

	/* MVP */

	fun getMVP(x: Float, y: Float, width: Float, height: Float): Matrix4f {
		return internalGetMVP(getModel(x, y, width, height, model), viewProjection, mvp)
	}

	fun getMVPRotateCenter(x: Float, y: Float, width: Float, height: Float, rotation: Float): Matrix4f {
		return internalGetMVP(getModelRotateCenter(x, y, width, height, rotation, model), viewProjection, mvp)
	}

	fun getMP(x: Float, y: Float, width: Float, height: Float): Matrix4f {
		return internalGetMVP(getModel(x, y, width, height, model), projection, mvp)
	}

	fun getMVPCentered(x: Float, y: Float, width: Float, height: Float): Matrix4f {
		return internalGetMVP(getModelCentered(x, y, width, height, model), viewProjection, mvp)
	}

	fun getMPCentered(x: Float, y: Float, width: Float, height: Float): Matrix4f {
		return internalGetMVP(getModelCentered(x, y, width, height, model), projection, mvp)
	}
}
