package com.gnarly.engine

import org.lwjgl.opengl.GL46.*

class Vao(vertices: FloatArray, indices: IntArray, val drawMode: Int) {
	private var numAttribs = 0
	private val vao: Int = glGenVertexArrays()
	private val ibo: Int
	private val count: Int
	private val vbos = IntArray(15)

	init {
		glBindVertexArray(vao)
		addAttrib(vertices, 3)
		ibo = glGenBuffers()
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo)
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW)
		count = indices.size
	}

	fun addAttrib(data: FloatArray, size: Int): Vao {
		val vbo = glGenBuffers()
		vbos[numAttribs] = vbo
		glBindBuffer(GL_ARRAY_BUFFER, vbo)
		glBufferData(GL_ARRAY_BUFFER, data, GL_STATIC_DRAW)
		glVertexAttribPointer(numAttribs, size, GL_FLOAT, false, 0, 0)
		glEnableVertexAttribArray(numAttribs)
		++numAttribs
		return this
	}

	fun render() {
		glBindVertexArray(vao)
		glDrawElements(drawMode, count, GL_UNSIGNED_INT, 0)
	}

	fun destroy() {
		for (vbo in vbos) glDeleteBuffers(vbo)
		glDeleteBuffers(ibo)
		glDeleteVertexArrays(vao)
	}
}
