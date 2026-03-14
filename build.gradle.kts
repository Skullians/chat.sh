import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
	id("net.fabricmc.fabric-loom-remap")
	`maven-publish`
	id("org.jetbrains.kotlin.jvm") version "2.3.10"
}

version = "${property("mod_version")}+${sc.current.version}"
base.archivesName = property("mod_id") as String

repositories {

}

dependencies {
    minecraft("com.mojang:minecraft:${sc.current.version}")
	mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:${property("fabric_loader")}")

	modImplementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_api")}")
	modImplementation("net.fabricmc:fabric-language-kotlin:${property("fabric_kotlin")}")

    val adventurePlatform = "net.kyori:adventure-platform-fabric:${property("adventure_platform")}"
    modImplementation(adventurePlatform)
    include(adventurePlatform)

    implementation(libs.reflections)
    implementation(libs.caffeine)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks {
    processResources {
        inputs.property("version", version)
        inputs.property("minecraft", sc.current.version)

        val props = mapOf(
            "version" to version,
            "minecraft" to sc.current.version
        )

        filesMatching("fabric.mod.json") { expand(props) }
    }

    jar {
        inputs.property("archivesName", base.archivesName)

        from("LICENSE") {
            rename { "${it}_${base.archivesName.get()}" }
        }
    }

    withType<JavaCompile>().configureEach {
        options.release = 21
    }

    withType<Test> {
        useJUnitPlatform()
    }

    register<Copy>("buildAndCollect") {
        group = "build"
        from(remapJar.map { it.archiveFile }, remapSourcesJar.map { it.archiveFile })
        into(rootProject.rootDir.resolve("target/"))
        dependsOn("build")
    }
}

kotlin {
	compilerOptions {
		jvmTarget = JvmTarget.JVM_21
	}
}

java {
	withSourcesJar()

	sourceCompatibility = JavaVersion.VERSION_21
	targetCompatibility = JavaVersion.VERSION_21
}

publishing {
	publications {
		register<MavenPublication>("mavenJava") {
			artifactId = base.archivesName.get()
			from(components["java"])
		}
	}
}
