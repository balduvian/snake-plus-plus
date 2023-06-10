package com.gnarly.game.shader

import com.gnarly.engine.Shader

class LevelShader : Shader("res/shader/level/vert.glsl", "res/shader/level/frag.glsl", arrayOf(
	"cameraDims",
	"offset",
	"time",
	"levelSize",
	"colorPalette"
)) {
	fun setCameraDims(width: Float, height: Float): LevelShader {
		uniformVec2(0, width, height)
		return this
	}

	fun setOffset(x: Float, y: Float): LevelShader {
		uniformVec2(1, x, y)
		return this
	}

	fun setTime(time: Float): LevelShader {
		uniformFloat(2, time)
		return this
	}

	fun setLevelSize(width: Int, height: Int): LevelShader {
		uniformVec2(3, width.toFloat(), height.toFloat())
		return this
	}

	fun setcolorPalette(values: FloatArray): LevelShader {
		uniformVec3Array(4, values)
		return this
	}
}
