package com.gnarly.game

import com.gnarly.engine.Texture
import com.gnarly.engine.Vao
import com.gnarly.engine.audio.Sound
import com.gnarly.game.shader.ColorShader
import com.gnarly.game.shader.LevelShader
import com.gnarly.game.shader.ShinyShader
import com.gnarly.game.shader.TextureShader
import org.lwjgl.opengl.GL46.*
import java.io.File
import javax.imageio.ImageIO

object Assets {
	lateinit var shinyShader: ShinyShader
	lateinit var colorShader: ColorShader
	lateinit var textureShader: TextureShader
	lateinit var levelShader: LevelShader
	lateinit var rect: Vao

	lateinit var countTextures: Array<Texture>

	lateinit var logoTexture: Texture
	lateinit var nextTexture: Texture
	lateinit var playTexture: Texture
	lateinit var retryTexture: Texture
	lateinit var spaceContinueTexture: Texture
	lateinit var spaceRetryTexture: Texture

	lateinit var menuMapTemplate: MapTemplate
	lateinit var mapTemplates: List<MapTemplate>

	lateinit var menuMusic: Sound

	fun init() {
		shinyShader = ShinyShader()
		colorShader = ColorShader()
		textureShader = TextureShader()
		levelShader = LevelShader()

		rect = Vao(floatArrayOf(
			0.0f, 0.0f, 0.0f,
			1.0f, 0.0f, 0.0f,
			1.0f, 1.0f, 0.0f,
			0.0f, 1.0f, 0.0f,
		), intArrayOf(
			0, 1, 2,
			2, 3, 0,
		)).addAttrib(floatArrayOf(
			0.0f, 1.0f,
			1.0f, 1.0f,
			1.0f, 0.0f,
			0.0f, 0.0f,
		), 2)

		fun Texture.defaultParams() = parameters(GL_NEAREST, GL_NEAREST, GL_CLAMP_TO_EDGE, GL_CLAMP_TO_EDGE)

		countTextures = Array(3) { i ->
			Texture.fromBufferedImage(ImageIO.read(File("res/texture/count-${i + 1}.png"))).defaultParams()
		}

		logoTexture = Texture.fromBufferedImage(ImageIO.read(File("res/texture/logo.png"))).defaultParams()
		nextTexture = Texture.fromBufferedImage(ImageIO.read(File("res/texture/next.png"))).defaultParams()
		playTexture = Texture.fromBufferedImage(ImageIO.read(File("res/texture/play.png"))).defaultParams()
		retryTexture = Texture.fromBufferedImage(ImageIO.read(File("res/texture/retry.png"))).defaultParams()
		spaceContinueTexture = Texture.fromBufferedImage(ImageIO.read(File("res/texture/space-continue.png"))).defaultParams()
		spaceRetryTexture = Texture.fromBufferedImage(ImageIO.read(File("res/texture/space-retry.png"))).defaultParams()

		menuMapTemplate = MapTemplate(File("res/texture/menu-level.png"), File("res/audio/menu.wav"))
		mapTemplates = File("res/level").listFiles()!!.sortedBy { it.name }.mapNotNull { folder ->
			if (!folder.isDirectory) return@mapNotNull null
			MapTemplate(folder.resolve("level.png"), folder.resolve("music.wav"))
		}

		menuMusic = Sound(File("res/audio/menu.wav"))
	}
}
