package com.natrow.tfcm

import com.mojang.logging.LogUtils
import com.natrow.tfcm.datagen.TFCMTagsProvider
import com.natrow.tfcm.structurize.registerPlacementHandlers
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.ModContainer
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.fml.common.Mod
import net.neoforged.fml.config.ModConfig
import net.neoforged.neoforge.data.event.GatherDataEvent

@Mod(TFCM.ID)
class TFCM(modContainer: ModContainer) {
  @EventBusSubscriber
  companion object {
    const val ID = "tfcm"
    val LOGGER = LogUtils.getLogger()

    @SubscribeEvent
    private fun onGatherData(event: GatherDataEvent) {
      val output = event.generator.packOutput
      val lookupProvider = event.lookupProvider
      val existingFileHelper = event.existingFileHelper

      event.generator.addProvider(
          event.includeServer(), TFCMTagsProvider(output, lookupProvider, existingFileHelper))
    }
  }

  init {
    modContainer.registerConfig(ModConfig.Type.SERVER, Config.SPEC)
    registerPlacementHandlers()
  }
}
