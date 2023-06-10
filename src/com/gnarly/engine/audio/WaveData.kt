package com.gnarly.engine.audio

import org.lwjgl.openal.AL11.*
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem

class WaveData(stream: AudioInputStream) {
	val format: Int
	val sampleRate: Int
	val totalBytes: Int
	val bytesPerFrame: Int
	val data: ByteBuffer

	init {
		val audioFormat = stream.format
		format = openAlFormat(audioFormat.channels, audioFormat.sampleSizeInBits)
		sampleRate = audioFormat.sampleRate.toInt()
		bytesPerFrame = audioFormat.frameSize
		totalBytes = (stream.frameLength * bytesPerFrame).toInt()

		val byteArray = ByteArray(totalBytes)
		stream.read(byteArray, 0, totalBytes)
		stream.close()

		data = ByteBuffer.wrap(byteArray)
		data.flip()
	}

	companion object {
		fun create(file: File): WaveData {
			val stream = FileInputStream(file)
			val bufferedInput = BufferedInputStream(stream)
			val audioStream = AudioSystem.getAudioInputStream(bufferedInput)
			return WaveData(audioStream)
		}

		fun openAlFormat(channels: Int, bitsPerSample: Int): Int {
			return if (channels == 1) {
				if (bitsPerSample == 8) AL_FORMAT_MONO8 else AL_FORMAT_MONO16
			} else {
				if (bitsPerSample == 8) AL_FORMAT_STEREO8 else AL_FORMAT_STEREO16
			}
		}
	}
}