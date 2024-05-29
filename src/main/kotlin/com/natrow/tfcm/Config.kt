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

enum class Strictness {
    EXACT,
    CLOSE,
    ANY
}

class ServerConfig(builder: ForgeConfigSpec.Builder) {
    init {
        builder.comment("Server configuration for TerraFirmaMineColonies").push("server")
    }

    val replaceBlocks: Boolean by builder.comment(
        """
        Whether to replace blocks with the schematic's original.
        If false, the blocks in the builder's inventory are used.
        If true, the schematic's original blocks are used.
        Enabling this could be seen as cheating.
        
        Allowed values: true, false. Default: false
        """.trimIndent()
    )
        .define("replaceBlocks", false)

    val soilStrictness: Strictness by builder.comment(
        """
        How strict soil blocks must be to the schematic's original.
        Close here includes nearby soil types.
        
        Allowed values: EXACT, CLOSE, ANY. Default: EXACT
        """.trimIndent()
    )
        .define("soilStrictness", Strictness.EXACT)

    val woodStrictness: Strictness by builder.comment(
        """
        How strict wood blocks must be to the schematic's original.
        TBD: Determine definition of close in this context...
        
        Allowed values: EXACT, CLOSE, ANY. Default: EXACT
        """.trimIndent()
    )
        .define("woodStrictness", Strictness.EXACT)

    val stoneStrictness: Strictness by builder.comment(
        """
        How strict stone blocks must be to the schematic's original.
        Close here includes stone in the same geological category (sedimentary, metamorphic, etc.).
        
        Allowed values: EXACT, CLOSE, ANY. Default: EXACT
        """.trimIndent()
    )
        .define("stoneStrictness", Strictness.EXACT)

    init {
        builder.pop()
    }
}