plugins {
    id("java-library")
    id("xyz.jpenilla.run-paper") version "3.0.2"
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven {
        url = uri("https://repo.extendedclip.com/releases/")
    }
    maven { url = uri("https://repo.codemc.io/repository/maven-releases/") }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.1.2.build.+")
    compileOnly("me.clip:placeholderapi:2.12.2")
    compileOnly("com.github.retrooper:packetevents-spigot:2.12.0")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}

val serverLauncher = javaToolchains.launcherFor {
    vendor = JvmVendorSpec.JETBRAINS
    languageVersion = JavaLanguageVersion.of(25)
}

tasks {
    runServer {
        // Configure the Minecraft version for our task.
        // This is the only required configuration besides applying the plugin.
        // Your plugin's jar (or shadowJar if present) will be used automatically.
        minecraftVersion("26.1.2")
        javaLauncher = serverLauncher
        jvmArgs("-Xms2G", "-Xmx2G", "-Dcom.mojang.eula.agree=true", "-XX:+AllowEnhancedClassRedefinition")

        downloadPlugins {
            modrinth("HYKaKraK", "h0ncTpUP")
            modrinth("gG7VFbG0", "t5Bd9Ajx")
            modrinth("lKEzGugV", "pIvQcXW8")
        }
    }

    processResources {
        val props = mapOf("version" to version, "description" to project.description)
        filesMatching("plugin.yml") {
            expand(props)
        }
    }
}
