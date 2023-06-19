package com.gnarly.game

import com.gnarly.engine.Texture
import com.gnarly.engine.Vao
import com.gnarly.engine.audio.Sound
import com.gnarly.game.shader.*
import org.joml.Math.cos
import org.joml.Math.sin
import org.lwjgl.opengl.GL46.*
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.PI
import kotlin.math.tan

object Assets {
	lateinit var shinyShader: ShinyShader
	lateinit var colorShader: ColorShader
	lateinit var textureShader: TextureShader
	lateinit var levelShader: LevelShader
	lateinit var glowShader: GlowShader
	lateinit var finalShader: FinalShader

	lateinit var rect: Vao
	lateinit var centerRect: Vao
	lateinit var transitionTriangle: Vao
	lateinit var oneWayTriangle: Vao
	lateinit var circle: Vao

	lateinit var countTextures: Array<Texture>

	lateinit var logoTexture: Texture
	lateinit var nextTexture: Texture
	lateinit var playTexture: Texture
	lateinit var retryTexture: Texture
	lateinit var spaceContinueTexture: Texture
	lateinit var spaceRetryTexture: Texture
	lateinit var transitionTexture: Texture

	lateinit var menuMapTemplate: MapTemplate
	lateinit var levelMapTemplates: List<MapTemplate>

	lateinit var deathMusic: Sound
	lateinit var countdownMusic: Sound
	lateinit var winMusic: Sound

	fun init() {
		shinyShader = ShinyShader()
		colorShader = ColorShader()
		textureShader = TextureShader()
		levelShader = LevelShader()
		glowShader = GlowShader()
		finalShader = FinalShader()

		rect = Vao(floatArrayOf(
			0.0f, 0.0f, 0.0f,
			1.0f, 0.0f, 0.0f,
			1.0f, 1.0f, 0.0f,
			0.0f, 1.0f, 0.0f,
		), intArrayOf(
			0, 1, 2,
			2, 3, 0,
		), GL_TRIANGLES).addAttrib(floatArrayOf(
			0.0f, 1.0f,
			1.0f, 1.0f,
			1.0f, 0.0f,
			0.0f, 0.0f,
		), 2)
		centerRect = Vao(floatArrayOf(
			-0.5f, -0.5f, 0.0f,
			0.5f, -0.5f, 0.0f,
			0.5f, 0.5f, 0.0f,
			-0.5f, 0.5f, 0.0f,
		), intArrayOf(
			0, 1, 2,
			2, 3, 0,
		), GL_TRIANGLES).addAttrib(floatArrayOf(
			0.0f, 0.0f,
			1.0f, 0.0f,
			1.0f, 1.0f,
			0.0f, 1.0f,
		), 2)
		transitionTriangle = Vao(floatArrayOf(
			0.0f, 0.0f, 0.0f,
			tan(PI.toFloat() / 8.0f), 1.0f, 0.0f,
			0.0f, 1.0f, 0.0f,
		), intArrayOf(
			0, 1, 2,
		), GL_TRIANGLES)
		oneWayTriangle = Vao(floatArrayOf(
			0.5f * cos(0.0f), 0.5f * sin(0.0f), 0.0f,
			0.5f * cos(2.0f * PI.toFloat() / 3.0f), 0.5f * sin(2.0f * PI.toFloat() / 3.0f), 0.0f,
			0.5f * cos(4.0f * PI.toFloat() / 3.0f), 0.5f * sin(4.0f * PI.toFloat() / 3.0f), 0.0f,
		), intArrayOf(
			0, 1, 2
		), GL_TRIANGLES)
		circle = Vao(FloatArray(32 * 3) { i ->
			if (i % 3 == 0) {
				0.5f * cos(2.0f * PI.toFloat() * ((i / 3).toFloat() / 32.0f))
			} else if (i % 3 == 1) {
				0.5f * sin(2.0f * PI.toFloat() * ((i / 3).toFloat() / 32.0f))
			} else {
				0.0f
			}
		}, IntArray(32) { it }, GL_TRIANGLE_FAN)

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
		transitionTexture = Texture.fromBufferedImage(ImageIO.read(File("res/texture/transition.png"))).defaultParams()

		menuMapTemplate = MapTemplate.loadFromFolder((File("res/level/menu")))

		levelMapTemplates = File("res/level")
			.listFiles()!!
			.filter { it.name.startsWith("level") }
			.sortedBy { it.name.substring(5).toInt() }
			.mapNotNull { folder -> if (folder.isDirectory) MapTemplate.loadFromFolder(folder) else null }

		deathMusic = Sound(File("res/audio/death.wav"))
		countdownMusic = Sound(File("res/audio/countdown.wav"))
		winMusic =  Sound(File("res/audio/win.wav"))
		winMusic.setVolume(2.0f)
	}
}
