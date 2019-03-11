package com.gnarly.engine.shaders;

import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniform1f;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL20.glUniform4f;

import com.gnarly.game.Main;

public class Shader2le extends Shader {

	int color1Loc, color2Loc, timeLoc, rgbLoc, loopLoc, freqLoc, texLoc;
	
	protected Shader2le() {
		super("res/shaders/s2le/vert.gls", "res/shaders/s2le/frag.gls");
		getUniforms();
	}
	
	@Override
	protected void getUniforms() {
		color1Loc = glGetUniformLocation(program, "color1");
		color2Loc = glGetUniformLocation(program, "color2");
		timeLoc   = glGetUniformLocation(program, "time");
		rgbLoc    = glGetUniformLocation(program, "rgb");
		loopLoc   = glGetUniformLocation(program, "loop");
		freqLoc   = glGetUniformLocation(program, "freq");
		texLoc    = glGetUniformLocation(program, "textured");
	}
	
	public void setEffect(LevelEffect.EffectPayload payload, boolean textured) {
		glUniform4f(color1Loc, payload.c1.x, payload.c1.y, payload.c1.z, payload.c1.w);
		glUniform4f(color2Loc, payload.c2.x, payload.c2.y, payload.c2.z, payload.c2.w);
		glUniform1f(timeLoc, payload.time);
		glUniform1f(freqLoc, payload.freq);
		glUniform1i(rgbLoc,  payload.rgb  ? 1 : 0);
		glUniform1i(loopLoc, payload.loop ? 1 : 0);
		glUniform1i(texLoc,      textured ? 1 : 0);
	}
}
