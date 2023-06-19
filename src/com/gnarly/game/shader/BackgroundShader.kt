package com.gnarly.game.shader

import com.gnarly.engine.Shader
import java.io.File

class BackgroundShader(fragmentShader: File) : Shader(File("res/shader/background/vert.glsl"), fragmentShader, arrayOf(
	"time",
	"colorPalette",
)) {
	fun setTime(time: Float): BackgroundShader {
		uniformFloat(0, time)
		return this
	}

	fun setcolorPalette(values: FloatArray): BackgroundShader {
		uniformVec3Array(1, values)
		return this
	}
}
