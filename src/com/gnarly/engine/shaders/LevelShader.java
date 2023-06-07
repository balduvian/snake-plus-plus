package com.gnarly.engine.shaders;

import org.lwjgl.opengl.GL46;

public class LevelShader extends Shader {
	int cameraDimsLoc;
	int offsetLoc;

	int timeLoc;
	int levelSizeLoc;
	int colorPaletteLoc;

	protected LevelShader() {
		super("res/shaders/level/vert.glsl", "res/shaders/level/frag.glsl");
		getUniforms();
	}
	
	@Override
	protected void getUniforms() {
		cameraDimsLoc = GL46.glGetUniformLocation(program, "cameraDims");
		offsetLoc = GL46.glGetUniformLocation(program, "offset");
		timeLoc = GL46.glGetUniformLocation(program, "time");
		levelSizeLoc = GL46.glGetUniformLocation(program, "levelSize");
		colorPaletteLoc = GL46.glGetUniformLocation(program, "colorPalette");
	}
	
	public LevelShader setCameraDims(float width, float height) {
		GL46.glUniform2f(cameraDimsLoc, width, height);
		return this;
	}

	public LevelShader setOffset(float x, float y) {
		GL46.glUniform2f(offsetLoc, x, y);
		return this;
	}

	public LevelShader setTime(float time) {
		GL46.glUniform1f(timeLoc, time);
		return this;
	}

	public LevelShader setLevelSize(int width, int height) {
		GL46.glUniform2f(levelSizeLoc, (float)width, (float)height);
		return this;
	}

	public LevelShader setcolorPalette(float[] values) {
		GL46.glUniform3fv(colorPaletteLoc, values);
		return this;
	}
}
