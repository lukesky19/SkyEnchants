plugins {
    `java-library`
    `maven-publish`
}

group = "com.github.lukesky19"
version = "0.4.0.0"

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc-repo"
    }
    maven("https://oss.sonatype.org/content/groups/public/") {
        name = "sonatype"
    }
    maven("https://jitpack.io") {
        name = "jitpack"
    }
    maven("https://repo.codemc.org/repository/maven-public/") {
        name = "codemc"
    }
    maven("https://repo.rosewooddev.io/repository/public/") {
        name = "RoseWood"
    }
    maven("https://repo.codemc.org/repository/maven-public/") {
        name = "codemc"
    }
}

dependencies {
    // Paper
    compileOnly("io.papermc.paper:paper-api:26.1.2.build.+")

    // SkyLib
    compileOnly("com.github.lukesky19:SkyLib:2.0.0.0")

    // Integration
    compileOnly("world.bentobox:bentobox:2.7.0-SNAPSHOT")
    compileOnly("org.black_ixx:playerpoints:3.3.3")
    compileOnly("dev.rosewood:rosestacker:1.5.37")
    compileOnly("com.github.lukesky19:SkyTools:0.1.0.0")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

tasks {
    processResources {
        val props = mapOf("version" to version)
        inputs.properties(props)
        filteringCharset = "UTF-8"
        val filePatterns = listOf("plugin.yml", "paper-plugin.yml")

        filePatterns.forEach { filePattern ->
            filesMatching(filePattern) {
                expand(props)
            }
        }
    }

    // This allows usage of @apiNode in javadocs
    javadoc {
        (options as StandardJavadocDocletOptions).tags("apiNote:a:API Note:")
    }

    jar {
        manifest {
            attributes["paperweight-mappings-namespace"] = "mojang"
        }
        archiveClassifier.set("")
    }

    build {
        dependsOn(publishToMavenLocal)
        dependsOn(javadoc)
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}