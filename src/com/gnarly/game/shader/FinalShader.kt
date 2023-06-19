package com.gnarly.game.shader

import com.gnarly.engine.Shader
import java.io.File

class FinalShader : Shader(
	File("res/shader/final/vert.glsl"), File("res/shader/final/frag.glsl"), arrayOf(
	"time",
	"colorPalette",
	"glow",
	"background"
)) {
	fun setTime(time: Float): FinalShader {
		uniformFloat(0, time)
		return this
	}

	fun setcolorPalette(values: FloatArray): FinalShader {
		uniformVec3Array(1, values)
		return this
	}

	fun setSamplers(glowUnit: Int, backgroundUnit: Int): FinalShader {
		uniformInt(2, glowUnit)
		uniformInt(3, backgroundUnit)
		return this
	}
}
