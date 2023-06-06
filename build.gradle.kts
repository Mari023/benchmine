buildscript {
    repositories {
        mavenCentral()
    }
}

plugins {
    id("fabric-loom") version "1.0-SNAPSHOT"
    id("io.github.juuxel.loom-quiltflower") version "1.10.0"
    id("com.diffplug.spotless") version "5.12.4"
    id("maven-publish")
    java
    idea
}

val modVersion: String by project
val modloader: String by project
val minecraftVersion: String by project
val fabricLoaderVersion: String by project
val fabricApiVersion: String by project
val modMenuVersion: String by project

version = "$modVersion-SNAPSHOT"

val pr = System.getenv("PR_NUMBER") ?: ""
if (pr != "") {
    version = "$modVersion+pr$pr"
}

val tag = System.getenv("TAG") ?: ""
if (tag != "") {
    if (!tag.startsWith("${modloader}/")) {
        throw GradleException("Tags for the $modloader version should start with ${modloader}/: $tag")
    }
    version = tag.substring("${modloader}/".length)
}

dependencies {
    minecraft("com.mojang:minecraft:${minecraftVersion}")
    mappings(loom.officialMojangMappings())

    modImplementation("net.fabricmc:fabric-loader:${fabricLoaderVersion}")
    modApi("net.fabricmc.fabric-api:fabric-api:${fabricApiVersion}")
    modApi("com.terraformersmc:modmenu:${modMenuVersion}")
}

repositories {
    mavenLocal()
    mavenCentral()
    maven {
        url = uri("https://modmaven.dev/")
        content {
            includeGroup("net.fabricmc.fabric-api")
        }
    }
    maven {
        url = uri("https://maven.parchmentmc.net/")
        content {
            includeGroup("org.parchmentmc.data")
        }
    }
    maven {
        url = uri("https://maven.terraformersmc.com/")
        content {
            includeGroup("com.terraformersmc")
        }
    }
}

java {
    withSourcesJar()
}

tasks {
    jar {
        finalizedBy("remapJar")
    }
    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(17)
    }
    withType<GenerateModuleMetadata> {
        enabled = false
    }
}

configure<com.diffplug.gradle.spotless.SpotlessExtension> {
    java {
        target("/src/*/java/**/*.java")

        endWithNewline()
        indentWithSpaces()
        removeUnusedImports()
        toggleOffOn()
        eclipse().configFile("codeformat/codeformat.xml")
        importOrderFile("codeformat/benchmine.importorder")
    }
}
