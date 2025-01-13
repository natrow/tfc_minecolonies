package com.natrow.tfcm

import com.mojang.logging.LogUtils
import com.natrow.tfcm.datagen.TFCMTagsProvider
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.ModContainer
import net.neoforged.fml.common.Mod
import net.neoforged.fml.config.ModConfig
import net.neoforged.neoforge.data.event.GatherDataEvent

@Mod(TFCM.MODID)
class TFCM(modEventBus: IEventBus, modContainer: ModContainer) {
  companion object {
    const val MODID = "tfcm"
    private val LOGGER = LogUtils.getLogger()
  }

  init {
    modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC)

    modEventBus.addListener(::gatherData)
  }

  private fun gatherData(event: GatherDataEvent) {
    val output = event.generator.packOutput
    val lookupProvider = event.lookupProvider
    val existingFileHelper = event.existingFileHelper

    event.generator.addProvider(
        event.includeServer(), TFCMTagsProvider(output, lookupProvider, existingFileHelper))
  }
}
