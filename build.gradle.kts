plugins {
    id("eclipse")
    id("idea")
    id("net.minecraftforge.gradle") version "[6.0,6.2)"
    id("org.parchmentmc.librarian.forgegradle") version "1.+"
    id("org.spongepowered.mixin") version "0.7.+"
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.serialization") version "1.9.22"
}

val modId: String = "tfcm"
val modVersion: String = "0.0.0-indev"

// Toolchain versions
val minecraftVersion: String = "1.20.1"
// Forge version. This should be the same as TFC/Minecolonies.
val forgeVersion: String = "47.1.3"

// Dependency versions
val jeiVersion: String = "15.2.0.21"
val mineColoniesVersion: String = "1.1.580-BETA"
val structurizeVersion: String = "1.0.733-RELEASE"
val blockUIVersion: String = "1.0.139-BETA"
val domumOrnamentumVersion: String = "1.0.184-BETA"
val terraFirmaCraftVersion: String = "3.2.3"
val patchouliVersion: String = "81"
val kotlinForForgeVersion: String = "4.10.0"

// Development properties
val mappingsChannel: String = "parchment"
val mappingsVersion: String = "2023.09.03-1.20.1"

println("Using mappings $mappingsChannel / $mappingsVersion with version $modVersion")

base {
    archivesName = "TerraFirmaMineColonies-Forge-$minecraftVersion"
    group = "net.natrow.tfcm"
    version = modVersion
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(17)
}

idea {
    module {
        excludeDirs.add(file("run"))
    }
}

repositories {
    mavenCentral()
    mavenLocal()
    maven(url = "https://maven.blamejared.com/") // Patchouli
    maven(url = "https://ldtteam.jfrog.io/artifactory/ldtteam/") // MineColonies
    maven(url = "https://api.modrinth.com/maven") // Modrinth (TFC)
    maven(url = "https://thedarkcolour.github.io/KotlinForForge/") // Kotlin Forge
    flatDir {
        dirs("libs")
    }
}

dependencies {
    minecraft("net.minecraftforge", "forge", version = "$minecraftVersion-$forgeVersion")

    runtimeOnly(fg.deobf("mezz.jei:jei-$minecraftVersion-forge:$jeiVersion"))

    runtimeOnly(fg.deobf("com.ldtteam:minecolonies:$minecraftVersion-$mineColoniesVersion"))
    runtimeOnly(fg.deobf("com.ldtteam:structurize:$minecraftVersion-$structurizeVersion"))
    runtimeOnly(fg.deobf("com.ldtteam:blockui:$minecraftVersion-$blockUIVersion"))
    runtimeOnly(fg.deobf("com.ldtteam:domum_ornamentum:$minecraftVersion-$domumOrnamentumVersion:universal"))

    runtimeOnly(fg.deobf("maven.modrinth:terrafirmacraft:$terraFirmaCraftVersion"))
    runtimeOnly(fg.deobf("vazkii.patchouli:Patchouli:$minecraftVersion-$patchouliVersion-FORGE"))

    implementation("thedarkcolour:kotlinforforge:$kotlinForForgeVersion")
}


minecraft {
    mappings(mappingsChannel, mappingsVersion)

    runs {
        all {
            args("-mixin.config=$modId.mixins.json")

            property("forge.logging.console.level", "debug")
            property("forge.enabledGameTestNamespaces", modId)

            property("mixin.env.remapRefMap", "true")
            property("mixin.env.refMapRemappingFile", "$projectDir/build/createSrgToMcp/output.srg")

            jvmArgs("-Xmx4G", "-Xms4G")

            mods.create(modId) {
                source(sourceSets.main.get())
            }
        }

        register("client") {
            workingDirectory(project.file("run/client"))
        }

        register("server") {
            workingDirectory(project.file("run/server"))
            arg("--nogui")
        }

        register("gameTestServer") {
            workingDirectory(project.file("run/gametest"))
            arg("--nogui")
        }
    }
}

mixin {
    add(sourceSets.main.get(), "$modId.refmap.json")
}

tasks {
    processResources {
        // this can do string substitutions on mod resources, currently unused
    }

    jar {
        manifest {
            attributes["Implementation-Version"] = project.version
            attributes["MixinConfigs"] = "$modId.mixins.json"
        }
    }
}
