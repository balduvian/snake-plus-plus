package com.gnarly.engine.texture;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import static org.lwjgl.opengl.GL46.*;

public class Texture {
	public int id, width, height;
	
	public Texture(String path) {
		try {
			BufferedImage bufferedImage = ImageIO.read(new File(path));

			var pixels = bufferedImage.getRGB(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight(), null, 0, bufferedImage.getWidth());
			for (var i = 0; i < pixels.length; ++i) pixels[i] = (pixels[i] << 8) | (pixels[i] >>> 24);

			this.id = glGenTextures();
			this.width = bufferedImage.getWidth();
			this.height = bufferedImage.getHeight();

			bind();
			glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
			glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
			glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
			glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
			glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, bufferedImage.getWidth(), bufferedImage.getHeight(), 0, GL_RGBA, GL_UNSIGNED_INT_8_8_8_8, pixels);
			unbind();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public Texture(int width, int height, int[] buffer) {
		this.id = glGenTextures();
		this.width = width;
		this.height = height;

		bind();
		glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_INT_8_8_8_8, buffer);
		unbind();
	}

	public Texture setFilterWrap(int filter, int wrap) {
		bind();

		glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, filter);
		glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, filter);
		glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, wrap);
		glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, wrap);

		unbind();
		return this;
	}

	public void bind() {
		glBindTexture(GL_TEXTURE_2D, id);
	}
	
	public void unbind() {
		glBindTexture(GL_TEXTURE_2D, 0);
	}
	
	public void destroy() {
		glDeleteTextures(id);
	}
}
