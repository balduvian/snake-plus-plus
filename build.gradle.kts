plugins {
	id("com.github.johnrengelman.shadow") version "7.1.2"
	application
}

group = "com.gnarly"
version = "1.0.0"

val lwjglVersion = "3.3.1"
val jomlVersion = "1.10.4"

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
	shadowJar {
		archiveFileName.set("${project.name}.jar")
		manifest {
			attributes(mapOf("Main-Class" to "game/GameKt"))
		}
	}
	jar {
		archiveFileName.set("${project.name}.jar")
		manifest {
			attributes(mapOf("Main-Class" to "game/GameKt"))
		}
	}
	build {
		dependsOn(shadowJar)
	}
}

application {
	mainClass.set("com/gnarly/game/Main")
}