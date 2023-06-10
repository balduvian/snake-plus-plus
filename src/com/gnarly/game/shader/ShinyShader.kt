package com.gnarly.game.shader

import com.gnarly.engine.Shader

class ShinyShader : Shader("res/shader/shiny/vert.glsl", "res/shader/shiny/frag.glsl", arrayOf(
	"color1",
	"color2",
	"time",
	"freq",
	"rgb",
	"loop",
	"textured",
)) {
	fun setEffect(time: Float, frequency: Float, rgb: Boolean, loop: Boolean, textured: Boolean): ShinyShader {
		uniformVec4(0, 0.0f, 1.0f, 0.0f, 1.0f)
		uniformVec4(1, 1.0f, 0.0f, 0.0f, 1.0f)
		uniformFloat(2, time)
		uniformFloat(3, frequency)
		uniformInt(4, if (rgb) 1 else 0)
		uniformInt(5, if (loop) 1 else 0)
		uniformInt(6, if (textured) 1 else 0)
		return this
	}
}
