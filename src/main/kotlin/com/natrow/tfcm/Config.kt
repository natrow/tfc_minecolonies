package com.natrow.tfcm

import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.fml.event.config.ModConfigEvent
import net.neoforged.neoforge.common.ModConfigSpec

@EventBusSubscriber
object Config {
  private val BUILDER: ModConfigSpec.Builder = ModConfigSpec.Builder()

  private val FIREPIT_LOG: ModConfigSpec.ConfigValue<String> =
      BUILDER.comment("Which wood type can be used by the builder to create fire pits.")
          .define("firepitLog", "tfc:wood/log/oak", Config::validateItemName)

  val SPEC: ModConfigSpec = BUILDER.build()

  lateinit var firepit_log: Item

  private fun validateItemName(obj: Any): Boolean {
    return obj is String && BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(obj))
  }

  @SubscribeEvent
  fun onLoad(event: ModConfigEvent.Loading) {
    TFCM.LOGGER.info("Loading TFCM config {}", event.config.fileName)

    firepit_log = BuiltInRegistries.ITEM[ResourceLocation.parse(FIREPIT_LOG.get())]
  }
}
