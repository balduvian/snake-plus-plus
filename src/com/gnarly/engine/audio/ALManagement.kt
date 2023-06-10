package com.gnarly.engine.audio

import org.lwjgl.openal.AL
import org.lwjgl.openal.ALC
import org.lwjgl.openal.ALC11.*
import org.lwjgl.openal.ALCCapabilities
import java.nio.IntBuffer

class ALManagement {
	private val device: Long
	private val context: Long
	private val deviceCaps: ALCCapabilities

	init {
		device = alcOpenDevice(alcGetString(0L, ALC_DEVICE_SPECIFIER))
		check(device != 0L) { "Failed to open the default device." }
		deviceCaps = ALC.createCapabilities(device)

		context = alcCreateContext(device, null as IntBuffer?)
		check(context != 0L) { "Failed to create an OpenAL context." }
		alcMakeContextCurrent(context)

		AL.createCapabilities(deviceCaps)
	}

	fun destroy() {
		alcDestroyContext(context)
		alcCloseDevice(device)
	}
}
