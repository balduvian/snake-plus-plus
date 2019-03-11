package com.gnarly.game;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE;

import java.io.File;

import com.gnarly.engine.display.Camera;
import com.gnarly.engine.display.Window;
import com.gnarly.engine.model.ColRect;
import com.gnarly.engine.model.EffectRect;

public class GamePanel {
  
	private Window window;
	private Camera camera;
	
	private Countdown countdown;
	
	private Map[] maps;
	private int curLevel = 0;
	
	private EffectButton retry, next;
	private EffectRect sretry, snext;
	private ColRect dark;
	
	private int state = 0;
	
	public GamePanel(Window window, Camera camera) {
		this.window = window;
		this.camera = camera;
		countdown = new Countdown(camera, "res/levels/all/countdown.fx", camera.getWidth() / 2 - 5, camera.getHeight() / 2 - 5, 0, 10, 10);
		countdown.start();
		int numLevels = new File("res/levels").list().length - 1;
		maps = new Map[numLevels];
		for (int i = 0; i < numLevels; i++)
			maps[i] = new Map(window, camera, "res/levels/level" + (i + 1));
		next = new EffectButton(window, camera, "res/levels/all/next/next.png", "res/levels/all/next/unpressed.fx", "res/levels/all/next/hovered.fx", "res/levels/all/next/pressed.fx", (camera.getWidth() - 15) / 2, (camera.getHeight() - 2) / 2, 0, 15, 2, true);
		snext = new EffectRect(camera, "res/levels/all/next/unpressed.fx", "res/levels/all/next/space.png", (camera.getWidth() - 9.7857142f) / 2, (camera.getHeight() - 1) / 2 + 2.25f, 0, 9.7857142f, 1, true);
		retry = new EffectButton(window, camera, "res/levels/all/retry/retry.png", "res/levels/all/retry/unpressed.fx", "res/levels/all/retry/hovered.fx", "res/levels/all/retry/pressed.fx", (camera.getWidth() - 10) / 2, (camera.getHeight() - 2) / 2, 0, 10, 2, true);
		sretry = new EffectRect(camera, "res/levels/all/retry/unpressed.fx", "res/levels/all/retry/space.png", (camera.getWidth() - 8.7857142f) / 2, (camera.getHeight() - 1) / 2 + 2.25f, 0, 8.7857142f, 1, true);
		dark = new ColRect(camera, 0, 0, -0.05f, camera.getWidth(), camera.getHeight(), 0, 0, 0, 0.5f, true);
	}
	
	public void update() {
		if (state == 1) {
			maps[curLevel].update();
			state = maps[curLevel].checkState();
		}
		else if(state == 2) {
			retry.update();
			if(retry.getState() == EffectButton.RELEASED || window.keyPressed(GLFW_KEY_SPACE) == 1) {
				countdown.start();
				state = 0;
				maps[curLevel].reload();
			}
		}
		else if(state == 3) {
			next.update();
			if(next.getState() == EffectButton.RELEASED || window.keyPressed(GLFW_KEY_SPACE) == 1) {
				countdown.start();
				state = 0;
				++curLevel;
				
				if(curLevel == maps.length) {
					curLevel = 0;
					state = 4;
				}
				maps[curLevel].reload();
			}
		}
		else if(countdown.getTime() < 0) {
			countdown.start();
			state = 1;
			maps[curLevel].start();
		}
	}
	
	public void render() {
		maps[curLevel].render();
		if(state == 0)
			countdown.render();
		else if(state == 2) {
			dark.render();
			retry.render();
			float scale = (float) (Math.sin(2f / 4f * Math.PI * Main.ttime) / 8 + 0.875f);
			sretry.setPosition((camera.getWidth() - sretry.getWidth() * scale) / 2, (camera.getHeight() - sretry.getHeight() * scale) / 2 + 2.25f, 0);
			sretry.setScale(scale);
			sretry.render();
		}
		else if(state == 3) {
			dark.render();
			next.render();
			float scale = (float) (Math.sin(2f / 4f * Math.PI * Main.ttime) / 8 + 0.875f);
			snext.setPosition((camera.getWidth() - snext.getWidth() * scale) / 2, (camera.getHeight() - snext.getHeight() * scale) / 2 + 2.25f, 0);
			snext.setScale(scale);
			snext.render();
		}
	}
	
	public int getState() {
		int temp = state;
		if(state == 4)
			state = 0;
		return 1 - (int) (temp / 4);
	}
	
	public void setActive() {
		maps[curLevel].camToSnake();
	}
}