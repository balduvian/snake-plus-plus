package com.gnarly.game;

import org.joml.Vector3f;

import com.gnarly.engine.audio.Sound;
import com.gnarly.engine.display.Camera;
import com.gnarly.engine.display.Window;
import com.gnarly.engine.model.EffectRect;

public class Menu {

	private Window window;
	private Camera camera;
	
	private Map map;
	private EffectRect logo;
	private EffectButton play;
	
	private Vector3f camVel;
	private int state = 0;
	
	public Menu(Window window, Camera camera) {
		this.window = window;
		this.camera = camera;
		logo = new EffectRect(camera, "res/menu/logo.fx", "res/menu/logo.png", (camera.getWidth() - 20) / 2, 0, 0, 20, 6.3414634f, true);
		map = new Map(window, camera, "res/menu");
		map.start();
		play = new EffectButton(window, camera, "res/menu/play.png", "res/menu/logo.fx", "res/menu/hovered.fx", "res/menu/pressed.fx", (camera.getWidth() - 8) / 2, 9, 0, 8, 2.7272727f, true);
		camVel = new Vector3f((float) (Math.random() * 2 - 1), (float) (Math.random() * 2 - 1), 0);
		camVel.normalize();
		camVel.mul(3);
		camera.setPosition(new Vector3f(3, 3, 0));
	}

	public void update() {
		camera.translate(camVel.mul((float) Main.dtime, new Vector3f()));
		int change = map.checkCamera();
		if((change & 0x01) == 1)
			camVel.x = -camVel.x;
		if((change & 0x02) == 2)
			camVel.y = -camVel.y;
		play.update();
		if(play.getState() == EffectButton.RELEASED)
			state = 1;
	}
	
	public void render() {
		map.render();
		logo.render();
		play.render();
	}
	
	public int getState() {
		int temp = state;
		if (state != 0) {
			map.stop();
			state = 0;
		}
		return temp;
	}
}
