package com.natrow.tfcm

import net.minecraftforge.common.ForgeConfigSpec
import kotlin.reflect.KProperty

operator fun <T> ForgeConfigSpec.ConfigValue<T>.getValue(any: Any?, property: KProperty<*>): T {
    return get()
}

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

class ServerConfig(builder: ForgeConfigSpec.Builder) {
    init {
        builder.comment("Server configuration for TerraFirmaMineColonies").push("server")
    }

    val firepitLog: String by builder.comment(
        """
        Which wood type can be used by the builder to create fire pits.
        
        Allowed values: any item. Default: "tfc:wood/log/oak"
        """.trimIndent()
    )
        .define("firepitLog", "tfc:wood/log/oak")

    init {
        builder.pop()
    }
}