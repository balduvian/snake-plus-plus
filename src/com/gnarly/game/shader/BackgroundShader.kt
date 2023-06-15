package com.gnarly.game.shader

import com.gnarly.engine.Shader

class BackgroundShader : Shader("res/shader/background/vert.glsl", "res/shader/background/frag.glsl", arrayOf(
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
