package com.gnarly.game;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_A;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_DOWN;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_S;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_UP;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_W;

import java.util.LinkedList;

import org.joml.Vector2f;

import com.gnarly.engine.display.Camera;
import com.gnarly.engine.display.Window;
import com.gnarly.engine.model.ColRect;

public class Snake {

	public static final Vector2f
		DIR_UP    = new Vector2f( 0, -1),
		DIR_DOWN  = new Vector2f( 0,  1),
		DIR_LEFT  = new Vector2f(-1,  0),
		DIR_RIGHT = new Vector2f( 1,  0);
	
	public static final int
		DIR_UP_AR    = 0,
		DIR_DOWN_AR  = 1,
		DIR_LEFT_AR  = 2,
		DIR_RIGHT_AR = 3;
	
	public static final Vector2f[]
		DIRS = { DIR_UP, DIR_DOWN, DIR_LEFT, DIR_RIGHT };
	
	private static ColRect rect = null;
	
	private Window window;
	private Camera camera;
	
	private Vector2f dir, banned = null, future;
	private LinkedList<Vector2f> snake = new LinkedList<Vector2f>();
	
	private int addLength = 0;
	
	public Snake(Window window, Camera camera) {
		this.window = window;
		this.camera = camera;
		if(rect == null)
			rect = new ColRect(camera, 0, 0, -0.08f, 1, 1, 0, 1, 0, 1, false);
	}
	
	public void update(boolean full) {
		if (full) {
			snake.add(0, snake.get(0).add(future, new Vector2f()));
			if(addLength == 0)
				snake.removeLast();
			else
				--addLength;
			future = dir;
			banned = future;
		}
		if(window.keyPressed(GLFW_KEY_LEFT) > 0 || window.keyPressed(GLFW_KEY_A) > 0 && banned != DIR_RIGHT)
			dir = DIR_LEFT;
		else if(window.keyPressed(GLFW_KEY_RIGHT) > 0 || window.keyPressed(GLFW_KEY_D) > 0 && banned != DIR_LEFT)
			dir = DIR_RIGHT;
		else if(window.keyPressed(GLFW_KEY_UP) > 0 || window.keyPressed(GLFW_KEY_W) > 0 && banned != DIR_DOWN)
			dir = DIR_UP;
		else if(window.keyPressed(GLFW_KEY_DOWN) > 0 || window.keyPressed(GLFW_KEY_S) > 0 && banned != DIR_UP)
			dir = DIR_DOWN;
	}
	
	public boolean check(int width, int height) {
		Vector2f head = snake.get(0);
		if (head.x < 0 || head.x == width || head.y < 0 || head.y == height)
			return false;
		for (int i = 1; i < snake.size(); i++)
			if (head.equals(snake.get(i)))
				return false;
		return true;
	}
	
	public void lengthen() {
		++addLength;
	}
	
	public void lengthen(int length) {
		addLength += length;
	}
	
	public Vector2f getHead() {
		return new Vector2f(snake.get(0));
	}
	
	public Vector2f getFuture() {
		return snake.get(0).add(future, new Vector2f());
	}
	
	public Vector2f getFuture(float percent) {
		return new Vector2f(snake.get(0).add(future.mul(percent, new Vector2f()), new Vector2f()));
	}
	
	public boolean contains(Vector2f position) {
		for (int i = 0; i < snake.size(); i++)
			if(snake.get(i).equals(position))
				return true;
		return false;
	}
	
	public void render(float percent) {
		if(addLength > 0) {
			rect.setPosition(snake.getLast());
			rect.render();
		}
		snake.add(0, snake.get(0).add(future, new Vector2f()));
		for (int i = 1; i < snake.size(); i++) {
			rect.setPosition(snake.get(i).add(snake.get(i - 1).sub(snake.get(i), new Vector2f()).mul(percent), new Vector2f()));
			rect.render();
			if(i != snake.size() - 1 && snake.get(i - 1).x != snake.get(i + 1).x && snake.get(i - 1).y != snake.get(i + 1).y) {
				rect.setPosition(snake.get(i));
				rect.render();
			}
		}
		snake.remove(0);
	}
	
	public void reset(int x, int y, Vector2f dir) {
		snake.clear();
		snake.add(new Vector2f(x, y));
		banned = dir;
		this.dir = dir;
		future = dir;
	}
}
