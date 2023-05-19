package com.gnarly.game;

import com.gnarly.engine.display.Camera;
import com.gnarly.engine.display.Window;
import com.gnarly.engine.model.EffectRect;
import org.joml.Vector3f;

public class Menu {
	private Window window;
	private Camera camera;
	
	private Map map;
	private EffectRect logo;
	private EffectButton play;
	
	private Vector3f camVel;
	private Vector3f camCenter;
	private int state = 0;
	
	public Menu(Window window, Camera camera) {
		this.window = window;
		this.camera = camera;
		logo = new EffectRect(camera, "res/menu/logo.fx", "res/menu/logo.png", (camera.getWidth() - 20) / 2, 0, 0, 20, 6.3414634f, true);
		map = new Map(window, camera, "res/menu");
		map.start();
		play = new EffectButton(window, camera, "res/menu/play.png", "res/menu/logo.fx", "res/menu/hovered.fx", "res/menu/pressed.fx", (camera.getWidth() - 8) / 2, 9, 0, 8, 2.7272727f, true);

		camVel = new Vector3f(1.0f, 2.0f, 0).normalize().mul(3);

		camCenter = new Vector3f(0.0f, 0.0f, 0);

		camera.setPosition(new Vector3f(3, 3, 0));
	}

	public void update() {
		camCenter.add(camVel.mul((float) Main.dtime, new Vector3f()));

		if (camCenter.x < 0) {
			camCenter.x = 0.0f;
			camVel.x = Math.abs(camVel.x);
		} else if (camCenter.x > map.getWidth()) {
			camCenter.x = map.getWidth();
			camVel.x = -Math.abs(camVel.x);
		}

		if (camCenter.y < 0) {
			camCenter.y = 0.0f;
			camVel.y = Math.abs(camVel.y);
		} else if (camCenter.y > map.getHeight()) {
			camCenter.y = map.getHeight();
			camVel.y = -Math.abs(camVel.y);
		}

		camera.setCenter(camCenter);

		play.update();

		if (play.getState() == EffectButton.RELEASED) {
			state = 1;
		}
	}
	
	public void render() {
		map.render(true, false);
		logo.set((camera.getWidth() - 20) / 2, 0, 20, 6.3414634f);
		logo.render();
		play.set((camera.getWidth() - 8) / 2, 9, 8, 2.7272727f);
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
