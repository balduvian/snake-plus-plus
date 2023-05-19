package com.gnarly.game;

import com.gnarly.engine.display.Camera;
import com.gnarly.engine.display.Window;
import com.gnarly.engine.model.EffectRect;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_1;

public class EffectButton {

	public static final int
		UNPRESSED = 0,
		RELEASED  = 1,
		PRESSED   = 2,
		HELD      = 3;
	
	private Window window;
	private Camera camera;
	
	private EffectRect[] states;
	
	private float x, y, width, height;
	
	private int state, tex;
	
	public EffectButton(Window window, Camera camera, String texture, String eff1, String eff2, String eff3, float x, float y, float depth, float width, float height, boolean gui) {
		this.window = window;
		this.camera = camera;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		states = new EffectRect[3];
		states[0] = new EffectRect(camera, eff1, texture, x, y, depth, width, height, gui);
		states[1] = new EffectRect(camera, eff2, texture, x, y, depth, width, height, gui);
		states[2] = new EffectRect(camera, eff3, texture, x, y, depth, width, height, gui);
		tex = 0;
		state = 0;
	}
	
	public void update() {
		if(contains(window.getMouseCoords(camera))) {
			if(window.mousePressed(GLFW_MOUSE_BUTTON_1) > 0) {
				tex = 2;
				if(state <= RELEASED)
					state = PRESSED;
				else
					state = HELD;
			}
			else {
				tex = 1;
				if(state >= PRESSED)
					state = RELEASED;
				else
					state = UNPRESSED;
			}
		}
		else {
			tex = 0;
			state = UNPRESSED;
		}
	}

	public void set(float x, float y, float width, float height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;

		for (var state : states) {
			state.set(x, y, width, height);
		}
	}

	public void render() {
		states[tex].render();
	}
	
	public boolean contains(Vector3f coords) {
		return coords.x >= x && coords.y >= y && coords.x < x + width && coords.y < y + height;
	}
	
	public int getState() {
		return state;
	}
}
