package com.gnarly.game

import com.gnarly.engine.Camera
import com.gnarly.engine.Texture
import org.joml.Matrix4f

class TextureBox(var x: Float, var y: Float, var width: Float, var height: Float) {
	companion object {
		const val PIXEL_SCALE = 8.0f

		fun fromTexture(texture: Texture): TextureBox {
			return TextureBox(0.0f, 0.0f, texture.width / PIXEL_SCALE, texture.height / PIXEL_SCALE)
		}
	}

	fun translate(x: Float, y: Float): TextureBox {
		this.x += x
		this.y += y
		return this
	}

	fun setDown(y: Float): TextureBox {
		this.y = y
		return this
	}

	fun setTop(y: Float): TextureBox {
		this.y = y - height
		return this
	}

	fun setLeft(x: Float): TextureBox {
		this.x = x
		return this
	}

	fun setRight(x: Float): TextureBox {
		this.x = x - width
		return this
	}

	fun setCenterX(x: Float): TextureBox {
		this.x = x - width / 2.0f
		return this
	}

	fun setCenterY(y: Float): TextureBox {
		this.y = y - height / 2.0f
		return this
	}

	fun scale(scale: Float): TextureBox {
		this.width = width * scale
		this.height = height * scale
		return this
	}

	fun scaleFromCenter(scale: Float): TextureBox {
		val newWidth = width * scale
		val newHeight = height * scale

		this.x -= (newWidth - width) / 2.0f
		this.y -= (newHeight - height) / 2.0f

		this.width = newWidth
		this.height = newHeight

		return this
	}
}

fun Camera.getMVP(textureBox: TextureBox): Matrix4f {
	return getMVP(textureBox.x, textureBox.y, textureBox.width, textureBox.height)
}

fun Camera.getMP(textureBox: TextureBox): Matrix4f {
	return getMP(textureBox.x, textureBox.y, textureBox.width, textureBox.height)
}