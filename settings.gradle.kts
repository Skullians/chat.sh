pluginManagement {
	repositories {
		mavenCentral()
		gradlePluginPortal()

        maven("https://maven.fabricmc.net/") { name = "Fabric" }
        maven("https://maven.kikugie.dev/snapshots") { name = "KikuGie Snapshots" }
	}

	plugins {
		id("net.fabricmc.fabric-loom-remap") version providers.gradleProperty("loom_version")
	}
}

plugins {
    id("dev.kikugie.stonecutter") version "0.8.3"
}

stonecutter {
    create(rootProject) {
        versions("1.21.11", "1.21.8", "1.21.4")
        vcsVersion = "1.21.11"
    }
}
