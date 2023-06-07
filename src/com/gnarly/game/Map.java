package com.gnarly.game;

import com.gnarly.engine.audio.Sound;
import com.gnarly.engine.display.Camera;
import com.gnarly.engine.display.Window;
import com.gnarly.engine.model.EffectRect;
import com.gnarly.engine.model.Vao;
import com.gnarly.engine.shaders.Shader;
import com.gnarly.engine.texture.Texture;
import org.joml.Vector2f;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import static org.lwjgl.opengl.GL46.*;

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

	private Texture dataTexture;
	private Vao levelVao;

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

	public void start() {
		sound.play(true);
	}

	public void stop() {
		sound.stop();
	}

	public static int posMod(int a, int b) {
		return ((a % b) + b) % b;
	}

	public int warpAccess(int x, int y) {
		return map[posMod(x, width)][posMod(y, height)];
	}

	public int boundedAccess(int x, int y) {
		if (x < 0 || x >= width || y < 0 || y >= height) return TYPE_WALL;
		return map[x][y];
	}

	public int access(int x, int y, boolean unlimited) {
		return unlimited ? warpAccess(x, y) : boundedAccess(x, y);
	}

	public void render() {
		render(false, true);
	}

	public void render(boolean unlimited, boolean renderSnake) {
		glDisable(GL_DEPTH_TEST);

		Shader.LEVEL_SHADER.enable();

		Shader.LEVEL_SHADER.setMVP(camera.getProjection().scale(camera.getWidth(), camera.getHeight(), 1.0f));
		Shader.LEVEL_SHADER.setTime((float)Main.ttime);
		Shader.LEVEL_SHADER.setLevelSize(dataTexture.width - 1, dataTexture.height - 1);
		Shader.LEVEL_SHADER.setCameraDims(camera.getWidth(), camera.getHeight());
		Shader.LEVEL_SHADER.setOffset(camera.getX(), camera.getY());
		Shader.LEVEL_SHADER.setcolorPalette(new float[] {
			0.500f, 0.500f, 0.500f,
			0.468f, 0.438f, 0.168f,
			1.000f, 0.878f, 1.000f,
			0.000f, 0.333f, 0.667f
		});

		dataTexture.bind();
		levelVao.render();

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
					case TYPE_END -> {
						border.set(i, j, 1, 1);
						border.render();
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
			Scanner scanner = new Scanner(new FileInputStream(path));
			speed = scanner.nextInt();
			snake.lengthen(scanner.nextInt() - 1);
			scanner.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void loadMap(String path) {
		try {
			InputStream stream = new FileInputStream(path);
			BufferedImage level = ImageIO.read(stream);

			stream.close();
			width = level.getWidth() + 2;
			height = level.getHeight() + 2;
			map = new int[width][height];

			var INT_WALL = 0x00ff00ff;
			var INT_LENGTH = 0x400000ff;
			var INT_SPEED = 0x7f0000ff;

			var textureBuffer = new int[width * height];

			for (int i = 0; i < width; i++) {
				for (int j = 0; j < height; j++) {
					int textureIndex = j * width + i;

					if (i == 0 || j == 0 || i == width - 1 || j == height - 1) {
						map[i][j] = TYPE_WALL;
						textureBuffer[textureIndex] = INT_WALL;
					} else {
						int pixel = level.getRGB(i - 1, j - 1);
						if (pixel == 0xffffffff) {
							map[i][j] = TYPE_WALL;
							textureBuffer[textureIndex] = INT_WALL;
						} else if (pixel >= 0xff00ff00 && pixel <= 0xff00ff03) {
							map[i][j] = TYPE_START;
							snake.reset(i, j, Snake.DIRS[pixel & 0x03]);
						} else if (pixel == 0xffff0000) {
							map[i][j] = TYPE_END;
						} else if (pixel == 0xff0000FF) {
							map[i][j] = TYPE_LENGTH;
							textureBuffer[textureIndex] = INT_LENGTH;
						} else if (pixel == 0xffff7f00) {
							map[i][j] = TYPE_SPEED;
							textureBuffer[textureIndex] = INT_SPEED;
						} else {
							map[i][j] = TYPE_EMPTY;
						}
					}
				}
			}


			dataTexture = new Texture(width, height, textureBuffer).setFilterWrap(GL_NEAREST, GL_CLAMP_TO_EDGE);
			levelVao = new Vao(
				new float[] {
					1, 0, 0, // Top left
					1, 1, 0, // Bottom left
					0, 1, 0, // Bottom right
					0, 0, 0  // Top right
				},
				new int [] {
					0, 1, 3,
					1, 2, 3
				}
			);

			if (path.contains("2")) {
				for (var i = 0; i < textureBuffer.length; ++i)
					textureBuffer[i] = (textureBuffer[i] >>> 8) | ((textureBuffer[i] & 0xff) << 24);
				var bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
				bi.setRGB(0, 0, width, height, textureBuffer, 0, width);
				ImageIO.write(bi, "PNG", new File("level-render.png"));
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
