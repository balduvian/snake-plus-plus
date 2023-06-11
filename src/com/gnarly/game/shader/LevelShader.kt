package com.gnarly.game.shader

import com.gnarly.engine.Shader

class LevelShader : Shader("res/shader/level/vert.glsl", "res/shader/level/frag.glsl", arrayOf(
	"time",
	"levelSize",
	"colorPalette"
)) {
	fun setTime(time: Float): LevelShader {
		uniformFloat(0, time)
		return this
	}

	fun setLevelSize(width: Int, height: Int): LevelShader {
		uniformVec2(1, width.toFloat(), height.toFloat())
		return this
	}

	fun setcolorPalette(values: FloatArray): LevelShader {
		uniformVec3Array(2, values)
		return this
	}
}
