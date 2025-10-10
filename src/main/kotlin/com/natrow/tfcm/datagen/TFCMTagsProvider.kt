package com.natrow.tfcm.datagen

import com.ldtteam.domumornamentum.tag.ModTags
import com.natrow.tfcm.TFCM
import com.natrow.tfcm.TFCMTags
import java.util.concurrent.CompletableFuture
import net.dries007.tfc.common.TFCTags
import net.dries007.tfc.common.blocks.TFCBlocks
import net.minecraft.core.HolderLookup
import net.minecraft.data.PackOutput
import net.minecraft.tags.BlockTags
import net.neoforged.neoforge.common.Tags
import net.neoforged.neoforge.common.data.BlockTagsProvider
import net.neoforged.neoforge.common.data.ExistingFileHelper

class TFCMTagsProvider(
    output: PackOutput,
    lookupProvider: CompletableFuture<HolderLookup.Provider>,
    existingFileHelper: ExistingFileHelper?
) : BlockTagsProvider(output, lookupProvider, TFCM.ID, existingFileHelper) {
  override fun addTags(lookupProvider: HolderLookup.Provider) {
    // no alabaster tag exists by default in TFC
    this.tag(TFCMTags.Blocks.ALABASTER)
        .add(*TFCBlocks.RAW_ALABASTER.values.map { e -> e.get() }.toTypedArray())
        .add(*TFCBlocks.POLISHED_ALABASTER.values.map { e -> e.get() }.toTypedArray())
        .add(*TFCBlocks.ALABASTER_BRICKS.values.map { e -> e.get() }.toTypedArray())
        .add(TFCBlocks.PLAIN_ALABASTER.get())
        .add(TFCBlocks.PLAIN_POLISHED_ALABASTER.get())
        .add(TFCBlocks.PLAIN_ALABASTER_BRICKS.get())

    // Domum tags, which control which materials can be used for domum decorations.
    // This is part for compatibility and part for balance

    this.tag(ModTags.GLOBAL_DEFAULT).replace(true).addTags(BlockTags.PLANKS)

    this.tag(ModTags.DOORS_MATERIALS).addTags(BlockTags.LOGS)

    this.tag(ModTags.FENCE_MATERIALS).addTags(BlockTags.LOGS)

    this.tag(ModTags.PAPERWALL_CENTER)
        .add(TFCBlocks.THATCH.get())
        .addTags(
            Tags.Blocks.STONES,
            Tags.Blocks.COBBLESTONES,
            BlockTags.STONE_BRICKS,
            TFCTags.Blocks.MUD_BRICKS,
            Tags.Blocks.SANDSTONE_BLOCKS,
            TFCMTags.Blocks.ALABASTER)

    this.tag(ModTags.PAPERWALL_FRAME).replace(true).addTags(ModTags.GLOBAL_DEFAULT, BlockTags.LOGS)

    this.tag(ModTags.PILLAR_MATERIALS)
        .addTags(
            Tags.Blocks.STONES,
            Tags.Blocks.COBBLESTONES,
            BlockTags.STONE_BRICKS,
            TFCTags.Blocks.MUD_BRICKS,
            Tags.Blocks.SANDSTONE_BLOCKS,
            TFCMTags.Blocks.ALABASTER)

    this.tag(ModTags.SHINGLES_ROOF)
        .add(TFCBlocks.THATCH.get())
        .addTags(
            Tags.Blocks.STONES,
            Tags.Blocks.COBBLESTONES,
            BlockTags.STONE_BRICKS,
            TFCTags.Blocks.MUD_BRICKS,
            Tags.Blocks.SANDSTONE_BLOCKS,
            TFCMTags.Blocks.ALABASTER)

    this.tag(ModTags.SHINGLES_SUPPORT).addTags(BlockTags.LOGS)

    this.tag(ModTags.SLAB_MATERIALS)
        .addTags(
            Tags.Blocks.STONES,
            Tags.Blocks.COBBLESTONES,
            BlockTags.STONE_BRICKS,
            TFCTags.Blocks.MUD_BRICKS,
            Tags.Blocks.SANDSTONE_BLOCKS,
            TFCMTags.Blocks.ALABASTER)

    this.tag(ModTags.STAIRS_MATERIALS)
        .addTags(
            Tags.Blocks.STONES,
            Tags.Blocks.COBBLESTONES,
            BlockTags.STONE_BRICKS,
            TFCTags.Blocks.MUD_BRICKS,
            Tags.Blocks.SANDSTONE_BLOCKS,
            TFCMTags.Blocks.ALABASTER)

    this.tag(ModTags.TIMBERFRAMES_CENTER)
        .add(TFCBlocks.THATCH.get())
        .addTags(
            Tags.Blocks.STONES,
            Tags.Blocks.COBBLESTONES,
            BlockTags.STONE_BRICKS,
            TFCTags.Blocks.MUD_BRICKS,
            Tags.Blocks.SANDSTONE_BLOCKS,
            TFCMTags.Blocks.ALABASTER)

    this.tag(ModTags.TIMBERFRAMES_FRAME)
        .replace(true)
        .addTags(ModTags.GLOBAL_DEFAULT, BlockTags.LOGS)

    this.tag(ModTags.TRAPDOORS_MATERIALS).addTags(BlockTags.LOGS)

    this.tag(ModTags.WALL_MATERIALS)
        .addTags(
            Tags.Blocks.STONES,
            Tags.Blocks.COBBLESTONES,
            BlockTags.STONE_BRICKS,
            TFCTags.Blocks.MUD_BRICKS,
            Tags.Blocks.SANDSTONE_BLOCKS,
            TFCMTags.Blocks.ALABASTER)
  }
}
