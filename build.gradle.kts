plugins {
	id("com.github.johnrengelman.shadow") version "7.1.2"
	kotlin("jvm") version "1.8.22"
	application
}

group = "com.gnarly"
version = "0.0.1"

val lwjglVersion = "3.3.2"
val jomlVersion = "1.10.5"

repositories {
	mavenCentral()
	maven(url = "https://oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
	implementation("org.lwjgl:lwjgl:${lwjglVersion}")
	implementation("org.lwjgl:lwjgl-opengl:${lwjglVersion}")
	implementation("org.lwjgl:lwjgl-glfw:${lwjglVersion}")
	implementation("org.lwjgl:lwjgl-openal:${lwjglVersion}")

	implementation("org.lwjgl:lwjgl:${lwjglVersion}:natives-windows")
	implementation("org.lwjgl:lwjgl-opengl:${lwjglVersion}:natives-windows")
	implementation("org.lwjgl:lwjgl-glfw:${lwjglVersion}:natives-windows")
	implementation("org.lwjgl:lwjgl-openal:${lwjglVersion}:natives-windows")

	implementation("org.joml:joml:${jomlVersion}")
}

sourceSets {
	main {
		java {
			srcDir("src")
		}
	}
}

tasks {
	jar {
		enabled = false
	}
	shadowJar {
		archiveFileName.set("snake-plus-plus.jar")
		manifest {
			attributes(mapOf("Main-Class" to "com/gnarly/game/MainKt"))
		}
	}
	build {
		dependsOn(shadowJar)
	}
}

kotlin {
	jvmToolchain(8)
}

application {
	mainClass.set("com/gnarly/game/MainKt")
}