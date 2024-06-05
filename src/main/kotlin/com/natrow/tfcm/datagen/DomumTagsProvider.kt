package com.natrow.tfcm.datagen

import com.ldtteam.domumornamentum.tag.ModTags
import com.natrow.tfcm.MissingTags
import com.natrow.tfcm.TFCM
import com.natrow.tfcm.TFCMTags
import net.dries007.tfc.common.blocks.TFCBlocks
import net.minecraft.core.HolderLookup
import net.minecraft.data.PackOutput
import net.minecraft.tags.BlockTags
import net.minecraftforge.common.Tags
import net.minecraftforge.common.data.BlockTagsProvider
import net.minecraftforge.common.data.ExistingFileHelper
import java.util.concurrent.CompletableFuture

class DomumTagsProvider(
    output: PackOutput,
    lookupProvider: CompletableFuture<HolderLookup.Provider>, existingFileHelper: ExistingFileHelper?
) : BlockTagsProvider(output, lookupProvider, TFCM.ID, existingFileHelper) {
    override fun addTags(pProvider: HolderLookup.Provider) {
        this.tag(ModTags.GLOBAL_DEFAULT)
            .replace(true)
            .addTags(BlockTags.PLANKS)

        this.tag(ModTags.DOORS_MATERIALS)
            .addTags(BlockTags.LOGS)

        this.tag(ModTags.FENCE_MATERIALS)
            .addTags(BlockTags.LOGS)

        this.tag(ModTags.PAPERWALL_CENTER)
            .add(TFCBlocks.THATCH.get())
            .addTags(
                Tags.Blocks.STONE,
                Tags.Blocks.COBBLESTONE,
                BlockTags.STONE_BRICKS,
                MissingTags.MUD_BRICKS,
                Tags.Blocks.SANDSTONE,
                TFCMTags.Blocks.ALABASTER
            )

        this.tag(ModTags.PAPERWALL_FRAME)
            .replace(true)
            .addTags(ModTags.GLOBAL_DEFAULT, BlockTags.LOGS)

        this.tag(ModTags.PILLAR_MATERIALS)
            .addTags(
                Tags.Blocks.STONE,
                Tags.Blocks.COBBLESTONE,
                BlockTags.STONE_BRICKS,
                MissingTags.MUD_BRICKS,
                Tags.Blocks.SANDSTONE,
                TFCMTags.Blocks.ALABASTER
            )

        this.tag(ModTags.SHINGLES_ROOF)
            .add(TFCBlocks.THATCH.get())
            .addTags(
                Tags.Blocks.STONE,
                Tags.Blocks.COBBLESTONE,
                BlockTags.STONE_BRICKS,
                MissingTags.MUD_BRICKS,
                Tags.Blocks.SANDSTONE,
                TFCMTags.Blocks.ALABASTER
            )

        this.tag(ModTags.SHINGLES_SUPPORT)
            .addTags(BlockTags.LOGS)

        this.tag(ModTags.SLAB_MATERIALS)
            .addTags(
                Tags.Blocks.STONE,
                Tags.Blocks.COBBLESTONE,
                BlockTags.STONE_BRICKS,
                MissingTags.MUD_BRICKS,
                Tags.Blocks.SANDSTONE,
                TFCMTags.Blocks.ALABASTER
            )

        this.tag(ModTags.STAIRS_MATERIALS)
            .addTags(
                Tags.Blocks.STONE,
                Tags.Blocks.COBBLESTONE,
                BlockTags.STONE_BRICKS,
                MissingTags.MUD_BRICKS,
                Tags.Blocks.SANDSTONE,
                TFCMTags.Blocks.ALABASTER
            )

        this.tag(ModTags.TIMBERFRAMES_CENTER)
            .add(TFCBlocks.THATCH.get())
            .addTags(
                Tags.Blocks.STONE,
                Tags.Blocks.COBBLESTONE,
                BlockTags.STONE_BRICKS,
                MissingTags.MUD_BRICKS,
                Tags.Blocks.SANDSTONE,
                TFCMTags.Blocks.ALABASTER
            )

        this.tag(ModTags.TIMBERFRAMES_FRAME)
            .replace(true)
            .addTags(ModTags.GLOBAL_DEFAULT, BlockTags.LOGS)

        this.tag(ModTags.TRAPDOORS_MATERIALS)
            .addTags(BlockTags.LOGS)

        this.tag(ModTags.WALL_MATERIALS)
            .addTags(
                Tags.Blocks.STONE,
                Tags.Blocks.COBBLESTONE,
                BlockTags.STONE_BRICKS,
                MissingTags.MUD_BRICKS,
                Tags.Blocks.SANDSTONE,
                TFCMTags.Blocks.ALABASTER
            )
    }

    override fun getName(): String {
        return "Domum Ornamentum Blocks Tag Provider"
    }
}