package com.gnarly.game;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import javax.imageio.ImageIO;

import org.joml.Vector2f;

import com.gnarly.engine.audio.Sound;
import com.gnarly.engine.display.Camera;
import com.gnarly.engine.display.Window;
import com.gnarly.engine.model.EffectRect;

public class Map {
	
	private static final int
		TYPE_EMPTY  = 0,
		TYPE_WALL   = 1,
		TYPE_START  = 2,
		TYPE_END    = 3,
		TYPE_LENGTH = 4,
		TYPE_SPEED  = 5;
	
	private EffectRect border, speedIcon, lengthIcon;
	
	private Window window;
	private Camera camera;
	
	private Sound sound;

	private String level;
	
	private int width, height;
	private int[][] map;
	
	private Snake snake;

	private int speed = 0;
	private double time = 0;
	
	private int state = 1;
	
	public Map(Window window, Camera camera, String level) {
		this.window = window;
		this.camera = camera;
		this.level = level;
		border = new EffectRect(camera, level + "/effect.fx", 0, 0, -0.1f, 1, 1, false);
		speedIcon = new EffectRect(camera, level + "/effect.fx", "res/levels/all/speed.png", 0, 0, -0.1f, 1, 1, false);
		lengthIcon = new EffectRect(camera, level + "/effect.fx", "res/levels/all/length.png", 0, 0, -0.1f, 1, 1, false);
		snake = new Snake(window, camera);
		sound = new Sound(level + "/music.wav");
		loadProps(level + "/data.prop");
		loadMap(level + "/level.png");
	}
	
	public void update() {
		time += Main.dtime;
		if(time >= 1f / speed) {
			snake.update(true);
			time = 0;
		}
		else
			snake.update(false);
		if(!snake.check(width, height))
			state = 2;
		Vector2f head = snake.getHead();
		int headType = map[(int) head.x][(int) head.y];
		switch (headType) {
			case TYPE_WALL:
				state = 2;
				break;
			case TYPE_END:
				state = 3;
				border.setEffect("res/levels/all/victory.fx");
			case TYPE_LENGTH:
				map[(int) head.x][(int) head.y] = TYPE_EMPTY;
				snake.lengthen(10);
				break;
			case TYPE_SPEED:
				map[(int) head.x][(int) head.y] = TYPE_EMPTY;
				speed += 2;
				break;
		}
		setCamera(snake.getFuture((float) (time * speed)));
	}
	
	private void setCamera(Vector2f position) {
		camera.setCenter(position.x, position.y);
		if(camera.getX() < 0)
			camera.setX(0);
		else if(camera.getX() + camera.getWidth() > width)
			camera.setX(width - camera.getWidth());
		if(camera.getY() < 0)
			camera.setY(0);
		else if(camera.getY() + camera.getHeight() > height)
			camera.setY(height - camera.getHeight());
	}
	
	public void camToSnake() {
		setCamera(snake.getHead());
	}
	
	public void reload() {
		loadProps(level + "/data.prop");
		loadMap(level + "/level.png");
		border.setEffect(level + "/effect.fx");
		camToSnake();
		state = 1;
	}
	
	public int checkCamera() {
		int ret = 0;
		if (camera.getX() < 0 || camera.getX() + camera.getWidth() > width)
			ret += 1;
		if (camera.getY() < 0 || camera.getY() + camera.getHeight() > height)
			ret += 2;
		return ret;
	}
	
	public void start() {
		sound.play(true);
	}

	public void stop() {
		sound.stop();
	}
	
	public void render() {
		snake.render((float) (time * speed));
		int minX = (int) Math.max(camera.getX(), 0);
		int minY = (int) Math.max(camera.getY(), 0);
		for (int i = minX; i < Math.min(minX + camera.getWidth() + 1, width); i++) {
			for (int j = minY; j < Math.min(minY + camera.getHeight() + 1, height); j++) {
				switch (map[i][j]) {
					case TYPE_WALL:
						if (i == 0 || j == 0 || map[i - 1][j] != TYPE_WALL || map[i][j - 1] != TYPE_WALL || map[i - 1][j - 1] != TYPE_WALL) { // Top Left  -1, -1
							border.set(i, j, 0.1f, 0.1f);
							border.render();
						}
						if (j == 0 || map[i][j - 1] != TYPE_WALL) { // Top Middle 0, -1
							border.set(i + 0.1f, j, 0.8f, 0.1f);
							border.render();
						}
						if (i == width - 1 || j == 0 || map[i + 1][j] != TYPE_WALL || map[i][j - 1] != TYPE_WALL || map[i + 1][j - 1] != TYPE_WALL) { // Top Right +1, -1
							border.set(i + 0.9f, j, 0.1f, 0.1f);
							border.render();
						}
						if (i == 0 || map[i - 1][j] != TYPE_WALL) { // Middle Left   -1,  0
							border.set(i, j + 0.1f, 0.1f, 0.8f);
							border.render();
						}
						if (i == width - 1 || map[i + 1][j] != TYPE_WALL) { // Middle Right  +1,  0
							border.set(i + 0.9f, j + 0.1f, 0.1f, 0.8f);
							border.render();
						}
						if (i == 0 || j == height - 1 || map[i - 1][j] != TYPE_WALL || map[i][j + 1] != TYPE_WALL || map[i - 1][j + 1] != TYPE_WALL) { // Bottom Left   -1, +1
							border.set(i, j + 0.9f, 0.1f, 0.1f);
							border.render();
						}
						if (j == height - 1 || map[i][j + 1] != TYPE_WALL) { // Bottom Middle  0, +1
							border.set(i + 0.1f, j + 0.9f, 0.8f, 0.1f);
							border.render();
						}
						if (i == width - 1 || j == height - 1 || map[i + 1][j] != TYPE_WALL || map[i][j + 1] != TYPE_WALL || map[i + 1][j + 1] != TYPE_WALL) { // Bottom Right  +1, +1
							border.set(i + 0.9f, j + 0.9f, 0.1f, 0.1f);
							border.render();
						}
						break;
					case TYPE_END:
						border.set(i, j, 1, 1);
						border.render();
						break;
					case TYPE_SPEED:
						speedIcon.set(i, j, 1, 1);
						speedIcon.render();
						break;
					case TYPE_LENGTH:
						lengthIcon.set(i, j, 1, 1);
						lengthIcon.render();
						break;
				}
			}
		}
	}
	
	public int checkState() {
		if (state == 2)
			border.setEffect("res/levels/all/defeat.fx");
		if (state != 1)
			sound.stop();
		return state;
	}
	
	private void loadProps(String path) {
		try {
			Scanner scanner = new Scanner(new FileInputStream(new File(path)));
			speed = scanner.nextInt();
			snake.lengthen(scanner.nextInt() - 1);
			scanner.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void loadMap(String path) {
		try {
			InputStream stream = new FileInputStream(new File(path));
			BufferedImage level = ImageIO.read(stream);
			stream.close();
			width = level.getWidth() + 2;
			height = level.getHeight() + 2;
			map = new int[width][height];
			for (int i = 0; i < map.length; i++) {
				for (int j = 0; j < map[0].length; j++) {
					if(i == 0 || j == 0 || i == width - 1 || j == height - 1)
						map[i][j] = TYPE_WALL;
					else {
						int pixel = level.getRGB(i - 1, j - 1);
						if (pixel == 0xffffffff)
							map[i][j] = TYPE_WALL;
						else if (pixel >= 0xff00ff00 && pixel <= 0xff00ff03) {
							map[i][j] = TYPE_START;
							snake.reset(i, j, Snake.DIRS[pixel & 0x03]);
						}
						else if (pixel == 0xffff0000)
							map[i][j] = TYPE_END;
						else if (pixel == 0xff0000FF)
							map[i][j] = TYPE_LENGTH;
						else if (pixel == 0xffff7f00)
							map[i][j] = TYPE_SPEED;
						else
							map[i][j] = TYPE_EMPTY;
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
