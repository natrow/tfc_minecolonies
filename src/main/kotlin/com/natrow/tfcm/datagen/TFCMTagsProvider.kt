package com.natrow.tfcm.datagen

import com.natrow.tfcm.TFCM
import com.natrow.tfcm.TFCMTags
import net.dries007.tfc.common.blocks.TFCBlocks
import net.minecraft.core.HolderLookup
import net.minecraft.data.PackOutput
import net.minecraftforge.common.data.BlockTagsProvider
import net.minecraftforge.common.data.ExistingFileHelper
import java.util.concurrent.CompletableFuture


/**
 * Tags added for the TFCM mod. This should be kept to a minimum if possible...
 */
class TFCMTagsProvider(
    output: PackOutput,
    lookupProvider: CompletableFuture<HolderLookup.Provider>, existingFileHelper: ExistingFileHelper?
) : BlockTagsProvider(output, lookupProvider, TFCM.ID, existingFileHelper) {
    // no alabaster tag exists by default in TFC
    override fun addTags(pProvider: HolderLookup.Provider) {
        this.tag(TFCMTags.Blocks.ALABASTER)
            .add(*TFCBlocks.RAW_ALABASTER.values.map { e -> e.get() }.toTypedArray())
            .add(*TFCBlocks.POLISHED_ALABASTER.values.map { e -> e.get() }.toTypedArray())
            .add(*TFCBlocks.ALABASTER_BRICKS.values.map { e -> e.get() }.toTypedArray())
            .add(TFCBlocks.PLAIN_ALABASTER.get())
            .add(TFCBlocks.PLAIN_POLISHED_ALABASTER.get())
            .add(TFCBlocks.PLAIN_ALABASTER_BRICKS.get())
    }

    override fun getName(): String {
        return "TFCM Blocks Tag Provider"
    }
}
