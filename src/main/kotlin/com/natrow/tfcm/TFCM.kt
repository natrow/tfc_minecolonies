package com.natrow.tfcm

import com.natrow.tfcm.structurize.PlacementHandlers
import net.minecraft.client.Minecraft
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.config.ModConfig
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import thedarkcolour.kotlinforforge.forge.MOD_BUS
import thedarkcolour.kotlinforforge.forge.registerConfig
import thedarkcolour.kotlinforforge.forge.runForDist

@Mod(TFCM.ID)
object TFCM {
    const val ID = "tfcm"

    val LOGGER: Logger = LogManager.getLogger(ID)

    init {
        val obj = runForDist(
            clientTarget = {
                MOD_BUS.addListener(::onClientStartup)
                Minecraft.getInstance()
            },
            serverTarget = {
                MOD_BUS.addListener(::onServerStartup)
                "test"
            }
        )

        registerConfig(ModConfig.Type.SERVER, Config.SERVER_SPEC)

        PlacementHandlers // access object to call constructor

        println(obj)
    }

    @Suppress("UNUSED_PARAMETER")
    private fun onClientStartup(event: FMLClientSetupEvent) {
        LOGGER.log(Level.INFO, "Initializing client...")
    }

    @Suppress("UNUSED_PARAMETER")
    private fun onServerStartup(event: FMLDedicatedServerSetupEvent) {
        LOGGER.log(Level.INFO, "Server starting...")
    }
}