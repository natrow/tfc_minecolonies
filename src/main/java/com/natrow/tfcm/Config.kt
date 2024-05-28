package com.natrow.tfcm

import net.minecraftforge.common.ForgeConfigSpec
import kotlin.reflect.KProperty

operator fun <T> ForgeConfigSpec.ConfigValue<T>.getValue(any: Any?, property: KProperty<*>): T {
    return get()
}

@Suppress("MemberVisibilityCanBePrivate")
object Config {
    val SERVER: ServerConfig
    val SERVER_SPEC: ForgeConfigSpec

    init {
        with(ForgeConfigSpec.Builder().configure(::ServerConfig)) {
            SERVER = left
            SERVER_SPEC = right
        }
    }
}

@Suppress("unused")
class ServerConfig(builder: ForgeConfigSpec.Builder) {
    init {
        builder.comment("Server configuration for TerraFirmaMineColonies").push("server")
    }

    val logDirtBlock: Boolean by builder.comment("Whether to log the dirt block on common setup")
        .define("logDirtBlock", true)
    val magicNumber: Int by builder.comment("A magic number").defineInRange("magicNumber", 42, 0, Int.MAX_VALUE)
    val magicNumberIntroduction: String by
    builder.comment("What you want the introduction message to be for the magic number")
        .define("magicNumberIntroduction", "The magic number is...")

    init {
        builder.pop()
    }
}