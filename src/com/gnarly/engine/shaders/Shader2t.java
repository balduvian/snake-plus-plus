package com.gnarly.engine.shaders;

public class Shader2t extends Shader {
	
	protected Shader2t() {
		super("res/shaders/s2t/vert.gls", "res/shaders/s2t/frag.gls");
	}

	@Override
	protected void getUniforms() {}
}