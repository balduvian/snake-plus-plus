package com.gnarly.game

object Global {
	var DEBUG_MODE = false
	var START_LEVEL = 1

	fun parseArgs(args: Array<String>) {
		var index = 0

		while (index < args.size) {
			val current = args[index]

			if (current == "--debug" || current == "-d") {
				DEBUG_MODE = true
			} else if (current.startsWith("--level=")) {
				current.substringAfter('=').toIntOrNull()?.let {
					START_LEVEL = it
				} ?: println("Invalid starting level")
			} else if (current == "-l") {
				if (index == args.lastIndex) {
					println("-l specified with no starting level")
				} else {
					val nextArg = args[++index]

					nextArg.toIntOrNull()?.let {
						START_LEVEL = it
					} ?: println("Could not read \"${nextArg}\" as a starting level")
				}
			} else {
				println("Unknown argument \"${current}\"")
			}
			++index
		}

		if (DEBUG_MODE) {
			println("Starting in debug mode")
		}

		if (DEBUG_MODE || START_LEVEL != 1) {
			println("Starting on level $START_LEVEL")
		}
	}

	private var mapTemplate: MapTemplate? = null

	fun newMapTemplate(mapTemplate: MapTemplate?): MapTemplate? {
		this.mapTemplate?.music?.destroy()
		this.mapTemplate?.backgroundShader?.destroy()
		this.mapTemplate = mapTemplate
		return mapTemplate
	}
}
