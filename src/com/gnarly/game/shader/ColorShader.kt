package com.gnarly.game.shader

import com.gnarly.engine.Shader

class ColorShader : Shader("res/shader/color/vert.glsl", "res/shader/color/frag.glsl", arrayOf("color")) {
	fun setColor(r: Float, g: Float, b: Float, a: Float): ColorShader {
		uniformVec4(0, r, g, b, a)
		return this
	}
}
