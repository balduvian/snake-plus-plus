package com.gnarly.engine

import org.lwjgl.opengl.GL46.*

class FrameBuffer(val numBuffers: Int) {
	var width: Int = 0
	var height: Int = 0
	private val drawBuffers: IntArray

	private val id = glGenFramebuffers()
	private val textures = ArrayList<Int>()

	init {
		for (i in 0 until numBuffers) addAttachment()
		drawBuffers = IntArray(numBuffers) { GL_COLOR_ATTACHMENT0 + it }
	}

	private fun addAttachment(): FrameBuffer {
		glBindFramebuffer(GL_FRAMEBUFFER, id)

		val textureId = glGenTextures()
		glBindTexture(GL_TEXTURE_2D, textureId)

		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)

		glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0 + textures.size, GL_TEXTURE_2D, textureId, 0)

		textures.add(textureId)

		glBindTexture(GL_TEXTURE_2D, 0)
		glBindFramebuffer(GL_FRAMEBUFFER, 0)

		return this
	}

	fun resize(width: Int, height: Int): FrameBuffer {
		this.width = width
		this.height = height

		textures.forEach { textureId ->
			glBindTexture(GL_TEXTURE_2D, textureId)

			glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, 0L)
		}

		glBindTexture(GL_TEXTURE_2D, 0)

		return this
	}

	fun use(): FrameBuffer {
		glBindFramebuffer(GL_FRAMEBUFFER, id)
		glDrawBuffers(drawBuffers)

		return this
	}

	fun bindTexture(index: Int, slot: Int): FrameBuffer {
		glActiveTexture(GL_TEXTURE0 + slot)
		glBindTexture(GL_TEXTURE_2D, textures[index])

		return this
	}

	companion object {
		fun useDefault() {
			glBindFramebuffer(GL_FRAMEBUFFER, 0)
		}
	}

	fun destroy() {
		glDeleteFramebuffers(id)
	}
}
