package com.gnarly.engine.model;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import com.gnarly.engine.display.Camera;
import com.gnarly.engine.shaders.LevelEffect;
import com.gnarly.engine.shaders.Shader;
import com.gnarly.engine.shaders.Shader2le;
import com.gnarly.engine.texture.Texture;
import com.gnarly.game.Main;

public class EffectRect extends Rect {

	private Shader2le shader = Shader.SHADER2LE;
	
	private Texture texture;
	
	private LevelEffect effect;
	
	public EffectRect(Camera camera, String path, float x, float y, float z, float width, float height, boolean gui) {
		super(camera, x, y, z, width, height, 0, gui);
		effect = new LevelEffect(path);
		texture = null;
	}
	
	public EffectRect(Camera camera, String effect, String texture, float x, float y, float z, float width, float height, boolean gui) {
		super(camera, x, y, z, width, height, 0, gui);
		this.effect = new LevelEffect(effect);
		this.texture = new Texture(texture);
		this.gui = gui;
	}
	
	public void render() {
		if(texture != null)
			texture.bind();
		shader.enable();
		shader.setEffect(effect.getPayload(Main.ttime), texture != null);
		Matrix4f cmat = gui ? camera.getProjection() : camera.getMatrix();
		shader.setMVP(cmat.translate(position.add(width * scale / 2, height * scale / 2, 0, new Vector3f())).rotateZ(rotation * 3.1415927f / 180).scale(width * scale, height * scale, 1).translate(-0.5f, -0.5f, 0));
		vao.render();
		shader.disable();
		if(texture != null)
			texture.unbind();
	}
	
	public void setEffect(String path) {
		effect = new LevelEffect(path);
	}
	
	public void setTexture(String path) {
		texture = new Texture(path);
	}
}
