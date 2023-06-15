package com.gnarly.game.shader

import com.gnarly.engine.Shader

class FinalShader : Shader("res/shader/final/vert.glsl", "res/shader/final/frag.glsl", arrayOf(
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
