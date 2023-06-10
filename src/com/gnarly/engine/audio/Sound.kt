package com.gnarly.engine.audio

import org.lwjgl.openal.AL11.*
import java.io.File

class Sound(file: File) {
	private val buffer: Int = alGenBuffers()
	private val sourceId: Int = alGenSources()

	init {
		val waveData = WaveData.create(file)
		alBufferData(buffer, waveData.format, waveData.data, waveData.sampleRate)
		alSourcei(sourceId, AL_BUFFER, buffer)
		alSourcef(sourceId, AL_GAIN, 1f)
		alSourcef(sourceId, AL_PITCH, 1f)
	}

	fun play(loop: Boolean) {
		alSourcei(sourceId, AL_LOOPING, if (loop) 1 else 0)
		alSource3f(sourceId, AL_POSITION, 0f, 0f, 0f)
		alSourcePlay(sourceId)
	}

	fun stop() {
		alSourceStop(sourceId)
	}

	fun setVolume(volume: Float) {
		alSourcef(sourceId, AL_GAIN, volume)
	}

	fun destroy() {
		alDeleteBuffers(buffer)
		alDeleteSources(sourceId)
	}
}
