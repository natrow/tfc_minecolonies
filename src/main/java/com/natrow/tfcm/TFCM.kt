package com.natrow.tfcm

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

    private val LOGGER: Logger = LogManager.getLogger(ID)

    init {
        LOGGER.log(Level.INFO, "Hello World!")

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