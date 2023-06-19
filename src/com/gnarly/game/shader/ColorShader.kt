package com.gnarly.game.shader

import com.gnarly.engine.Shader
import java.io.File

class ColorShader : Shader(File("res/shader/color/vert.glsl"), File("res/shader/color/frag.glsl"), arrayOf("color")) {
	fun setColor(r: Float, g: Float, b: Float, a: Float): ColorShader {
		uniformVec4(0, r, g, b, a)
		return this
	}
}
