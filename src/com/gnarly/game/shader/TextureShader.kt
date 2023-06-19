package com.gnarly.game.shader

import com.gnarly.engine.Shader
import java.io.File

class TextureShader : Shader(File("res/shader/texture/vert.glsl"), File("res/shader/texture/frag.glsl"), arrayOf())
