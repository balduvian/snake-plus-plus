package com.gnarly.game.shader

import com.gnarly.engine.Shader

class GlowShader : Shader("res/shader/glow/vert.glsl", "res/shader/glow/frag.glsl", arrayOf(
	"time",
	"levelSize",
	"wrap"
)) {
	fun setTime(time: Float): GlowShader {
		uniformFloat(0, time)
		return this
	}

	fun setLevelSize(width: Int, height: Int): GlowShader {
		uniformVec2(1, width.toFloat(), height.toFloat())
		return this
	}

	fun setWrap(wrap: Boolean): GlowShader {
		uniformBool(2, wrap)
		return this
	}
}
