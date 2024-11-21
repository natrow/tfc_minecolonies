package com.natrow.tfcm

import java.util.stream.Collectors
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.fml.event.config.ModConfigEvent
import net.neoforged.neoforge.common.ModConfigSpec

// An example config class. This is not required, but it's a good idea to have one to keep your
// config organized.
// Demonstrates how to use Neo's config APIs
@EventBusSubscriber(modid = TFCM.MODID, bus = EventBusSubscriber.Bus.MOD)
object Config {
  private val BUILDER: ModConfigSpec.Builder = ModConfigSpec.Builder()

  private val LOG_DIRT_BLOCK: ModConfigSpec.BooleanValue =
      BUILDER.comment("Whether to log the dirt block on common setup").define("logDirtBlock", true)

  private val MAGIC_NUMBER: ModConfigSpec.IntValue =
      BUILDER.comment("A magic number").defineInRange("magicNumber", 42, 0, Int.MAX_VALUE)

  val MAGIC_NUMBER_INTRODUCTION: ModConfigSpec.ConfigValue<String> =
      BUILDER.comment("What you want the introduction message to be for the magic number")
          .define("magicNumberIntroduction", "The magic number is... ")

  // a list of strings that are treated as resource locations for items
  private val ITEM_STRINGS: ModConfigSpec.ConfigValue<List<String>> =
      BUILDER.comment("A list of items to log on common setup.")
          .defineListAllowEmpty("items", listOf("minecraft:iron_ingot"), Config::validateItemName)

  val SPEC: ModConfigSpec = BUILDER.build()

  var logDirtBlock: Boolean = false
  var magicNumber: Int = 0
  lateinit var magicNumberIntroduction: String
  lateinit var items: Set<Item>

  private fun validateItemName(obj: Any): Boolean {
    return obj is String && BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(obj))
  }

  @SubscribeEvent
  fun onLoad(event: ModConfigEvent) {
    logDirtBlock = LOG_DIRT_BLOCK.get()
    magicNumber = MAGIC_NUMBER.get()
    magicNumberIntroduction = MAGIC_NUMBER_INTRODUCTION.get()

    // convert the list of strings into a set of items
    items =
        ITEM_STRINGS.get()
            .stream()
            .map { itemName: String? -> BuiltInRegistries.ITEM[ResourceLocation.parse(itemName)] }
            .collect(Collectors.toSet())
  }
}
