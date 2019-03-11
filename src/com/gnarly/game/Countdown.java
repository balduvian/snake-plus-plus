package com.gnarly.game;

import com.gnarly.engine.display.Camera;
import com.gnarly.engine.model.ColRect;
import com.gnarly.engine.model.Rect;
import com.gnarly.engine.shaders.LevelEffect;
import com.gnarly.engine.shaders.Shader;
import com.gnarly.engine.shaders.Shader2le;
import com.gnarly.engine.texture.Texture;

public class Countdown extends Rect {

	private Shader2le shader = Shader.SHADER2LE;
	
	private Camera camera;
	
	private LevelEffect effect;
	private Texture[] nums;
	
	private double time;
	
	private ColRect dark;
	
	public Countdown(Camera camera, String path, float x, float y, float z, float width, float height) {
		super(camera, x, y, z, width, height, 0, true);
		this.camera = camera;
		effect = new LevelEffect(path);
		dark = new ColRect(camera, 0, 0, -0.05f, camera.getWidth(), camera.getHeight(), 0, 0, 0, 0.9f, true);
		nums = new Texture[] {
			new Texture("res/levels/all/one.png"),
			new Texture("res/levels/all/two.png"),
			new Texture("res/levels/all/three.png")
		};
	}
	
	public void start() {
		time = 3;
	}
	
	public void render() {
		dark.setOpacity(0.9f * (float) (Math.min(Math.pow(time, 0.33f), 1))); 
		dark.render();
		time -= Main.dtime;
		if(time > 0) {
			int num = (int) Math.ceil(time) - 1;
			float scale = (float) (time - num) * 0.75f + 0.25f;
			nums[num].bind();
			shader.enable();
			shader.setEffect(effect.getPayload(3 - time), true);
			shader.setMVP(camera.getProjection().translate(position).translate((width - width * scale) / 2, (height - height * scale) / 2, 0).scale(width * scale, height * scale, 1));
			vao.render();
			shader.disable();
			nums[num].unbind();
		}
	}
	
	public double getTime() {
		return time;
	}
}
