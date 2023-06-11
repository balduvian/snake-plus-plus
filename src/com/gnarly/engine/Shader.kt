package com.gnarly.engine

import org.joml.Matrix4f
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL46.*
import java.io.BufferedReader
import java.io.FileReader
import java.io.IOException

open class Shader(vertPath: String, fragPath: String, uniformNames: Array<String>) {
	private val program: Int = glCreateProgram()
	private val mvpLoc: Int
	private val modelLoc: Int
	private val uniformLocs: IntArray
	private val internalMVPArray = FloatArray(16)
	private val internalMVP = Matrix4f()

	init {
		val vert = loadShader(vertPath, GL_VERTEX_SHADER)
		val frag = loadShader(fragPath, GL_FRAGMENT_SHADER)
		glAttachShader(program, vert)
		glAttachShader(program, frag)
		glLinkProgram(program)
		glDetachShader(program, vert)
		glDetachShader(program, frag)
		glDeleteShader(vert)
		glDeleteShader(frag)
		mvpLoc = glGetUniformLocation(program, "mvp")
		modelLoc = glGetUniformLocation(program, "model")
		uniformLocs = IntArray(uniformNames.size)
		for (i in uniformNames.indices) {
			uniformLocs[i] = glGetUniformLocation(program, uniformNames[i])
		}
	}

	private fun loadShader(path: String, type: Int): Int {
		val file = StringBuilder()
		try {
			val reader = BufferedReader(FileReader(path))
			var line: String?
			while (reader.readLine().also { line = it } != null) {
				file.append(line).append('\n')
			}
			reader.close()
		} catch (e: IOException) {
			e.printStackTrace()
		}
		val source = file.toString()
		val shader = glCreateShader(type)
		glShaderSource(shader, source)
		glCompileShader(shader)
		if (glGetShaderi(shader, GL_COMPILE_STATUS) != 1) {
			throw RuntimeException("Failed to compile shader: " + path + "! " + glGetShaderInfoLog(shader))
		}
		return shader
	}

	fun setMVP(projView: Matrix4f, model: Matrix4f): Shader {
		projView.mul(model, internalMVP)
		if (modelLoc != -1) glUniformMatrix4fv(modelLoc, false, model[internalMVPArray])
		glUniformMatrix4fv(mvpLoc, false, internalMVP[internalMVPArray])
		return this
	}

	fun uniformFloat(index: Int, v: Float): Shader {
		glUniform1f(uniformLocs[index], v)
		return this
	}

	fun uniformVec2(index: Int, v0: Float, v1: Float): Shader {
		glUniform2f(uniformLocs[index], v0, v1)
		return this
	}

	fun uniformVec3(index: Int, v0: Float, v1: Float, v2: Float): Shader {
		glUniform3f(uniformLocs[index], v0, v1, v2)
		return this
	}

	fun uniformVec3Array(index: Int, values: FloatArray): Shader {
		glUniform3fv(uniformLocs[index], values)
		return this
	}

	fun uniformVec4(index: Int, v0: Float, v1: Float, v2: Float, v3: Float): Shader {
		glUniform4f(uniformLocs[index], v0, v1, v2, v3)
		return this
	}

	fun uniformInt(index: Int, i: Int): Shader {
		glUniform1i(uniformLocs[index], i)
		return this
	}

	fun enable(): Shader {
		glUseProgram(program)
		return this
	}

	fun disable() {
		glUseProgram(0)
	}

	fun destroy() {
		glDeleteProgram(program)
	}
}
