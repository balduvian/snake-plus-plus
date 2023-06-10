package com.gnarly.engine

import org.lwjgl.opengl.GL46.*
import java.awt.image.BufferedImage

class Texture(val id: Int, var width: Int, var height: Int) {
	companion object {
		fun fromBufferedImage(bufferedImage: BufferedImage): Texture {
			val pixels = bufferedImage.getRGB(0, 0, bufferedImage.width, bufferedImage.height, null, 0, bufferedImage.width)
			for (i in pixels.indices) pixels[i] = pixels[i] shl 8 or (pixels[i] ushr 24)

			val id = glGenTextures()
			val width = bufferedImage.width
			val height = bufferedImage.height

			glBindTexture(GL_TEXTURE_2D, id)
			glTexImage2D(
				GL_TEXTURE_2D,
				0,
				GL_RGBA,
				bufferedImage.width,
				bufferedImage.height,
				0,
				GL_RGBA,
				GL_UNSIGNED_INT_8_8_8_8,
				pixels
			)

			return Texture(id, width, height)
		}

		fun fromBuffer(width: Int, height: Int, buffer: IntArray): Texture {
			val id = glGenTextures()

			glBindTexture(GL_TEXTURE_2D, id)
			glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_INT_8_8_8_8, buffer)

			return Texture(id, width, height)
		}

		fun empty(): Texture {
			val id = glGenTextures()

			return Texture(id, 0, 0)
		}

		fun bindNone(index: Int = 0) {
			glActiveTexture(GL_TEXTURE0 + index)
			glBindTexture(GL_TEXTURE_2D, 0)
		}
	}

	fun parameters(minFilter: Int, magFiler: Int, horzWrap: Int, vertWrap: Int): Texture {
		bind()
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, minFilter)
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, magFiler)
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, horzWrap)
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, vertWrap)

		return this
	}

	fun setImage(width: Int, height: Int, buffer: IntArray): Texture {
		this.width = width
		this.height = height

		bind()
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_INT_8_8_8_8, buffer)

		return this
	}

	fun bind(index: Int = 0): Texture {
		glActiveTexture(GL_TEXTURE0 + index)
		glBindTexture(GL_TEXTURE_2D, id)

		return this
	}

	fun destroy() {
		glDeleteTextures(id)
	}
}
