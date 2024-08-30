package com.natrow.tfcm.mixinimpl.minecolonies.ai

import com.minecolonies.api.colony.interactionhandling.ChatPriority
import com.minecolonies.core.colony.fields.FarmField
import com.minecolonies.core.colony.interactionhandling.PosBasedInteraction
import com.natrow.tfcm.*
import net.dries007.tfc.common.blockentities.FarmlandBlockEntity
import net.dries007.tfc.common.blocks.crop.CropBlock
import net.dries007.tfc.common.blocks.crop.DoubleCropBlock
import net.dries007.tfc.common.blocks.crop.FloodedCropBlock
import net.dries007.tfc.common.blocks.soil.FarmlandBlock
import net.dries007.tfc.util.Fertilizer
import net.dries007.tfc.util.climate.Climate
import net.dries007.tfc.util.climate.ClimateRange
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Blocks
import thedarkcolour.kotlinforforge.forge.vectorutil.v3d.component1
import thedarkcolour.kotlinforforge.forge.vectorutil.v3d.component2
import thedarkcolour.kotlinforforge.forge.vectorutil.v3d.component3
import java.util.function.Predicate

/**
 * Public functions the Mixin creates
 */
interface EntityAIWorkFarmerExt {
    /**
     * Check all the farmland blocks of a field against a predicate
     *
     * @param field     is the field to be checked
     * @param predicate is a predicate that checks a single farmland block
     * @return true if all blocks pass the predicate
     */
    fun checkField(field: FarmField, predicate: Predicate<BlockPos>): Boolean
}


/**
 * Creates a predicate for a field which will determine if an item is a valid fertilizer for its crop
 */
fun isFertilizer(field: FarmField): Predicate<ItemStack>? {
    val crop = getCrop(field)
    val nutrient = crop?.primaryNutrient ?: return null
    return Predicate {
        val fertilizer = Fertilizer.get(it)
        if (fertilizer != null) {
            fertilizer.getNutrient(nutrient) > 0.0F
        } else {
            false
        }
    }
}

/**
 * Get all the valid fertilizers for a field's specific crop
 */
fun getFertilizers(field: FarmField): List<ItemStack>? {
    val crop = getCrop(field) ?: return null
    return getFertilizers(crop, null)
}

/**
 * Get all the valid fertilizers for a crop, without wasting
 */
fun getFertilizers(crop: CropBlock, farmland: FarmlandBlockEntity?): List<ItemStack> {
    val nutrient = crop.primaryNutrient
    val currentLevel = farmland?.getNutrient(nutrient) ?: 0.0F

    return Fertilizer.MANAGER.values.filter {
        val fertilizerLevel = it.getNutrient(nutrient)
        fertilizerLevel > 0.0F && fertilizerLevel + currentLevel <= 1.0F
    }.flatMap { it.validItems }
        .map { ItemStack(it) }
}

/**
 * Attempt to get a TFC CropBlock from a field
 */
fun getCrop(field: FarmField): CropBlock? {
    return ((field.seed.item as? BlockItem)?.block) as? CropBlock
}

/**
 * Types of errors that can happen during a climate check
 */
enum class ClimateCheckError {
    TOO_DRY,
    TOO_WET,
    TOO_COLD,
    TOO_HOT,
    TOO_TALL,
    NO_WATER;

    fun toTranslationConstant(): String {
        return when (this) {
            TOO_DRY -> CROP_TOO_DRY
            TOO_WET -> CROP_TOO_WET
            TOO_COLD -> CROP_TOO_COLD
            TOO_HOT -> CROP_TOO_HOT
            TOO_TALL -> CROP_TOO_TALL
            NO_WATER -> CROP_NO_WATER
        }
    }
}

/**
 * Check whether a crop can grow on a block, returning a list of errors
 */
fun checkClimate(world: Level, pos: BlockPos, crop: CropBlock): List<ClimateCheckError> {
    if (world.getBlockState(pos).block !is FarmlandBlock) {
        return mutableListOf()
    }

    val climate = crop.climateRange
    val hydration = FarmlandBlock.getHydration(world, pos)
    val temperature = Climate.getTemperature(world, pos)
    val errors = mutableListOf<ClimateCheckError>()

    when (climate.checkHydration(hydration, false)) {
        ClimateRange.Result.LOW -> errors.add(ClimateCheckError.TOO_DRY)
        ClimateRange.Result.HIGH -> errors.add(ClimateCheckError.TOO_WET)
        else -> {}
    }

    when (climate.checkTemperature(temperature, false)) {
        ClimateRange.Result.LOW -> errors.add(ClimateCheckError.TOO_COLD)
        ClimateRange.Result.HIGH -> errors.add(ClimateCheckError.TOO_HOT)
        else -> {}
    }

    // check if double crops have room to grow
    if (crop is DoubleCropBlock && !(world.isEmptyBlock(pos.above(2)) || world.getBlockState(pos.above(2)).block is DoubleCropBlock)) {
        errors.add(ClimateCheckError.TOO_TALL)
    }

    // check if crop has water
    if (crop is FloodedCropBlock && !(world.getBlockState(pos.above())
            .`is`(Blocks.WATER) || world.getBlockState(pos.above()).block is FloodedCropBlock)
    ) {
        errors.add(ClimateCheckError.NO_WATER)
    }

    return errors
}

/**
 * Create an interaction handler for a climate error
 */
fun createInteraction(error: ClimateCheckError, field: FarmField): PosBasedInteraction {
    val key = error.toTranslationConstant()
    val pos = field.position
    val (x, y, z) = pos
    val name = field.seed.displayName

    return PosBasedInteraction(
        Component.translatable(key, name, x, y, z),
        ChatPriority.BLOCKING,
        Component.translatable(key),
        pos
    )
}