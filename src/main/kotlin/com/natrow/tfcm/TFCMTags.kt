package com.natrow.tfcm

import net.minecraft.core.Registry
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.level.block.Block

object TFCMTags {
  object Blocks {
    val ALABASTER = create("alabaster")

    private fun create(name: String): TagKey<Block> {
      return create(Registries.BLOCK, name)
    }
  }

  private fun <T> create(registry: ResourceKey<Registry<T>>, name: String): TagKey<T> {
    return TagKey.create(registry, ResourceLocation.fromNamespaceAndPath(TFCM.MODID, name))
  }
}
