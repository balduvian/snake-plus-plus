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
import org.joml.Vector3f;

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

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
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
		setCamera(snake.getFuture((float) (time * speed)).add(0.5f, 0.5f));
	}
	
	private void setCamera(Vector2f position) {
		camera.setCenter(position.x, position.y);
		//if(camera.getX() < 0)
		//	camera.setX(0);
		//else if(camera.getX() + camera.getWidth() > width)
		//	camera.setX(width - camera.getWidth());
		//if(camera.getY() < 0)
		//	camera.setY(0);
		//else if(camera.getY() + camera.getHeight() > height)
		//	camera.setY(height - camera.getHeight());
	}
	
	public void camToSnake() {
		setCamera(snake.getHead().add(0.5f, 0.5f));
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

	public static int posMod(int a, int b) {
		return ((a % b) + b) % b;
	}

	public static long posMod(long a, long b) {
		return ((a % b) + b) % b;
	}

	public static long xorShift64(long a) {
		a ^= (a << 21);
		a ^= (a >>> 35);
		a ^= (a << 4);
		return a;
	}

	private static long psudeoRand(int a, int b) {
		var g = ((long)b) & ((1L << 32) - 1);
		return xorShift64(((long)a << 32) | g);
	}

	public int warpAccess(int x, int y) {
		return map[posMod(x, width)][posMod(y, height)];
	}

	private static final int[] wallsPattern = new int[109];

	static {
		for (var i = 0; i < wallsPattern.length; ++i) {
			wallsPattern[i] = Math.random() < 0.5 ? TYPE_EMPTY : TYPE_WALL;
		}
	}

	public int boundedAccess(int x, int y) {
		if (x < 0 || x >= width || y < 0 || y >= height) return wallsPattern[(int)posMod(psudeoRand(x, y), wallsPattern.length)];
		return map[x][y];
	}

	public int access(int x, int y, boolean unlimited) {
		return unlimited ? warpAccess(x, y) : boundedAccess(x, y);
	}

	public void render() {
		render(false, true);
	}

	public void render(boolean unlimited, boolean renderSnake) {
		if (renderSnake) {
			snake.render((float) (time * speed));
		}

		var minX = (int)Math.floor(camera.getX());
		var minY = (int)Math.floor(camera.getY());

		var maxX = (int)Math.ceil(camera.getX() + camera.getWidth());
		var maxY = (int)Math.ceil(camera.getY() + camera.getHeight());

		for (int i = minX; i <= maxX; i++) {
			for (int j = minY; j <= maxY; j++) {
				switch (access(i, j, unlimited)) {
					case TYPE_WALL -> {
						if (access(i - 1, j, unlimited) != TYPE_WALL || access(i, j - 1, unlimited) != TYPE_WALL || access(i - 1, j - 1, unlimited) != TYPE_WALL) { // Top Left  -1, -1
							border.set(i, j, 0.1f, 0.1f);
							border.render();
						}
						if (access(i, j - 1, unlimited) != TYPE_WALL) { // Top Middle 0, -1
							border.set(i + 0.1f, j, 0.8f, 0.1f);
							border.render();
						}
						if (access(i + 1, j, unlimited) != TYPE_WALL || access(i, j - 1, unlimited) != TYPE_WALL || access(i + 1, j - 1, unlimited) != TYPE_WALL) { // Top Right +1, -1
							border.set(i + 0.9f, j, 0.1f, 0.1f);
							border.render();
						}
						if (access(i - 1,  j, unlimited) != TYPE_WALL) { // Middle Left   -1,  0
							border.set(i, j + 0.1f, 0.1f, 0.8f);
							border.render();
						}
						if (access(i + 1, j, unlimited) != TYPE_WALL) { // Middle Right  +1,  0
							border.set(i + 0.9f, j + 0.1f, 0.1f, 0.8f);
							border.render();
						}
						if (access(i - 1, j, unlimited) != TYPE_WALL || access(i, j + 1, unlimited) != TYPE_WALL || access(i - 1, j + 1, unlimited) != TYPE_WALL) { // Bottom Left   -1, +1
							border.set(i, j + 0.9f, 0.1f, 0.1f);
							border.render();
						}
						if (access(i, j + 1, unlimited) != TYPE_WALL) { // Bottom Middle  0, +1
							border.set(i + 0.1f, j + 0.9f, 0.8f, 0.1f);
							border.render();
						}
						if (access(i + 1, j, unlimited) != TYPE_WALL || access(i, j + 1, unlimited) != TYPE_WALL || access(i + 1, j + 1, unlimited) != TYPE_WALL) { // Bottom Right  +1, +1
							border.set(i + 0.9f, j + 0.9f, 0.1f, 0.1f);
							border.render();
						}
					}
					case TYPE_END -> {
						border.set(i, j, 1, 1);
						border.render();
					}
					case TYPE_SPEED -> {
						speedIcon.set(i, j, 1, 1);
						speedIcon.render();
					}
					case TYPE_LENGTH -> {
						lengthIcon.set(i, j, 1, 1);
						lengthIcon.render();
					}
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
