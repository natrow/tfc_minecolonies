package com.natrow.tfcm.structurize

import com.ldtteam.structurize.api.util.constant.Constants
import com.ldtteam.structurize.placement.handlers.placement.IPlacementHandler
import com.ldtteam.structurize.placement.handlers.placement.PlacementHandlers
import com.natrow.tfcm.Config
import com.natrow.tfcm.TFCM
import net.dries007.tfc.common.blocks.StainedWattleBlock
import net.dries007.tfc.common.blocks.TFCBlocks
import net.dries007.tfc.common.blocks.ThatchBedBlock
import net.dries007.tfc.common.blocks.devices.*
import net.dries007.tfc.common.blocks.rock.Rock
import net.dries007.tfc.common.blocks.rock.RockAnvilBlock
import net.dries007.tfc.common.blocks.soil.ISoilBlock
import net.dries007.tfc.common.blocks.wood.Wood
import net.dries007.tfc.common.items.HideItemType
import net.dries007.tfc.common.items.TFCItems
import net.dries007.tfc.util.Helpers
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.BedBlock
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.BedPart
import net.minecraftforge.registries.ForgeRegistries

/**
 * Placement handlers fix behavior when a builder needs to place blocks from Minecolonies
 */
object PlacementHandlers {
    init {
        PlacementHandlers.add(ThatchBedPlacementHandler())
        PlacementHandlers.add(StoneAnvilPlacementHandler())
        PlacementHandlers.add(WattlePlacementHandler())
        PlacementHandlers.add(SoilPlacementHandler())
        PlacementHandlers.add(FirepitPlacementHandler())
        PlacementHandlers.add(ForgePlacementHandler())
        PlacementHandlers.add(TileEntityPlacementHandler())
    }
}

/**
 * Thatch beds are similar to normal beds but require 2 thatch and 1 large hide
 */
class ThatchBedPlacementHandler : IPlacementHandler {
    override fun canHandle(world: Level, pos: BlockPos, blockState: BlockState): Boolean {
        return blockState.block is ThatchBedBlock
    }

    override fun handle(
        world: Level,
        pos: BlockPos,
        blockState: BlockState,
        tileEntityData: CompoundTag?,
        complete: Boolean,
        centerPos: BlockPos
    ): IPlacementHandler.ActionProcessingResult {
        if (blockState.getValue(BedBlock.PART) == BedPart.HEAD) {
            val facing = blockState.getValue(BedBlock.FACING)
            world.setBlock(
                pos.relative(facing.opposite),
                blockState.setValue(BedBlock.PART, BedPart.FOOT),
                Constants.UPDATE_FLAG
            )
            world.setBlock(pos, blockState.setValue(BedBlock.PART, BedPart.HEAD), Constants.UPDATE_FLAG)
            PlacementHandlers.handleTileEntityPlacement(tileEntityData, world, pos)
            PlacementHandlers.handleTileEntityPlacement(tileEntityData, world, pos.relative(facing.opposite))
            return IPlacementHandler.ActionProcessingResult.SUCCESS
        } else {
            return IPlacementHandler.ActionProcessingResult.PASS
        }
    }

    override fun getRequiredItems(
        world: Level,
        pos: BlockPos,
        blockState: BlockState,
        tileEntityData: CompoundTag?,
        complete: Boolean
    ): MutableList<ItemStack> {
        return if (blockState.getValue(BedBlock.PART) == BedPart.HEAD) {
            mutableListOf(
                ItemStack(TFCBlocks.THATCH.get().asItem(), 2),
                ItemStack(TFCItems.HIDES[HideItemType.RAW]!![HideItemType.Size.LARGE]!!.get().asItem())
            )
        } else {
            mutableListOf()
        }
    }
}

/**
 * Anvils require the corresponding raw stone block
 */
class StoneAnvilPlacementHandler : IPlacementHandler {
    private val anvilToRock: Map<Block, Block> by lazy {
        TFCBlocks.ROCK_ANVILS.keys.associate { e -> e.anvil.get() to e.getBlock(Rock.BlockType.RAW).get() }
    }

    override fun canHandle(world: Level, pos: BlockPos, blockState: BlockState): Boolean {
        return blockState.block is RockAnvilBlock
    }

    override fun handle(
        world: Level,
        pos: BlockPos,
        blockState: BlockState,
        tileEntityData: CompoundTag?,
        complete: Boolean,
        centerPos: BlockPos
    ): IPlacementHandler.ActionProcessingResult {
        world.setBlock(pos, blockState.block.defaultBlockState(), Constants.UPDATE_FLAG)
        return IPlacementHandler.ActionProcessingResult.SUCCESS
    }

    override fun getRequiredItems(
        world: Level,
        pos: BlockPos,
        blockState: BlockState,
        tileEntityData: CompoundTag?,
        complete: Boolean
    ): MutableList<ItemStack> {
        return mutableListOf(ItemStack(anvilToRock[blockState.block]!!.asItem()))
    }
}

/**
 * Wattle can be decorated with up to 4 sticks
 */
class WattlePlacementHandler : IPlacementHandler {
    override fun canHandle(world: Level, pos: BlockPos, blockState: BlockState): Boolean {
        return blockState.block is StainedWattleBlock
    }

    override fun handle(
        world: Level,
        pos: BlockPos,
        blockState: BlockState,
        tileEntityData: CompoundTag?,
        complete: Boolean,
        centerPos: BlockPos
    ): IPlacementHandler.ActionProcessingResult {
        world.setBlock(pos, blockState, Constants.UPDATE_FLAG)
        return IPlacementHandler.ActionProcessingResult.SUCCESS
    }

    override fun getRequiredItems(
        world: Level,
        pos: BlockPos,
        blockState: BlockState,
        tileEntityData: CompoundTag?,
        complete: Boolean
    ): MutableList<ItemStack> {
        var numSticks = 0
        if (blockState.getValue(StainedWattleBlock.TOP)) numSticks++
        if (blockState.getValue(StainedWattleBlock.BOTTOM)) numSticks++
        if (blockState.getValue(StainedWattleBlock.LEFT)) numSticks++
        if (blockState.getValue(StainedWattleBlock.RIGHT)) numSticks++
        return mutableListOf(ItemStack(blockState.block.asItem()), ItemStack(Items.STICK, numSticks))
    }
}

/**
 * Soil blocks require the right type of dirt
 */
class SoilPlacementHandler : IPlacementHandler {
    override fun canHandle(world: Level, pos: BlockPos, blockState: BlockState): Boolean {
        return blockState.block is ISoilBlock
    }

    override fun handle(
        world: Level,
        pos: BlockPos,
        blockState: BlockState,
        tileEntityData: CompoundTag?,
        complete: Boolean,
        centerPos: BlockPos
    ): IPlacementHandler.ActionProcessingResult {
        world.setBlock(pos, blockState.block.defaultBlockState(), Constants.UPDATE_FLAG)
        return IPlacementHandler.ActionProcessingResult.SUCCESS
    }

    override fun getRequiredItems(
        world: Level,
        pos: BlockPos,
        blockState: BlockState,
        tileEntityData: CompoundTag?,
        complete: Boolean
    ): MutableList<ItemStack> {
        return mutableListOf(ItemStack((blockState.block as ISoilBlock).dirt.block.asItem()))
    }
}

/**
 * Fire pits require 3 sticks and 1 log
 */
class FirepitPlacementHandler : IPlacementHandler {
    override fun canHandle(world: Level, pos: BlockPos, blockState: BlockState): Boolean {
        return blockState.block is FirepitBlock
    }

    override fun handle(
        world: Level,
        pos: BlockPos,
        blockState: BlockState,
        tileEntityData: CompoundTag?,
        complete: Boolean,
        centerPos: BlockPos
    ): IPlacementHandler.ActionProcessingResult {
        world.setBlock(pos, blockState.block.defaultBlockState(), Constants.UPDATE_FLAG)
        return IPlacementHandler.ActionProcessingResult.SUCCESS
    }

    override fun getRequiredItems(
        world: Level,
        pos: BlockPos,
        blockState: BlockState,
        tileEntityData: CompoundTag?,
        complete: Boolean
    ): MutableList<ItemStack> {
        var logItem: Item = ForgeRegistries.ITEMS.getValue(ResourceLocation(Config.SERVER.firepitLog))!!
        if (logItem == Items.AIR) {
            TFCM.LOGGER.log(
                org.apache.logging.log4j.Level.WARN,
                "Invalid item ID for firepitLog. Using fallback value."
            )
            logItem = TFCBlocks.WOODS[Wood.OAK]!![Wood.BlockType.LOG]!!.get().asItem()
        }

        val list = mutableListOf(
            ItemStack(logItem), ItemStack(Items.STICK, 3)
        )

        if (blockState.block is PotBlock) {
            list.add(ItemStack(TFCItems.POT.get()))
        }

        if (blockState.block is GrillBlock) {
            list.add(ItemStack(TFCItems.WROUGHT_IRON_GRILL.get()))
        }

        return list
    }
}

/**
 * Forges require 8 charcoal
 */
class ForgePlacementHandler : IPlacementHandler {
    override fun canHandle(world: Level, pos: BlockPos, blockState: BlockState): Boolean {
        return blockState.block is CharcoalForgeBlock
    }

    override fun handle(
        world: Level,
        pos: BlockPos,
        blockState: BlockState,
        tileEntityData: CompoundTag?,
        complete: Boolean,
        centerPos: BlockPos
    ): IPlacementHandler.ActionProcessingResult {
        world.setBlock(pos, blockState.block.defaultBlockState(), Constants.UPDATE_FLAG)
        return IPlacementHandler.ActionProcessingResult.SUCCESS
    }

    override fun getRequiredItems(
        world: Level,
        pos: BlockPos,
        blockState: BlockState,
        tileEntityData: CompoundTag?,
        complete: Boolean
    ): MutableList<ItemStack> {
        return mutableListOf(ItemStack(Items.CHARCOAL, 8))
    }
}

/**
 * Several tile entities have hidden inventories that aren't detected by Minecolonies automatically.
 */
class TileEntityPlacementHandler : IPlacementHandler {
    override fun canHandle(world: Level, pos: BlockPos, blockState: BlockState): Boolean {
        return blockState.block is SheetPileBlock || blockState.block is IngotPileBlock
    }

    override fun handle(
        world: Level,
        pos: BlockPos,
        blockState: BlockState,
        tileEntityData: CompoundTag?,
        complete: Boolean,
        centerPos: BlockPos
    ): IPlacementHandler.ActionProcessingResult {
        world.setBlock(pos, blockState, Constants.UPDATE_FLAG)
        if (tileEntityData != null) {
            PlacementHandlers.handleTileEntityPlacement(tileEntityData, world, pos)
            return IPlacementHandler.ActionProcessingResult.SUCCESS
        } else {
            return IPlacementHandler.ActionProcessingResult.DENY
        }
    }

    override fun getRequiredItems(
        world: Level,
        pos: BlockPos,
        blockState: BlockState,
        tileEntityData: CompoundTag?,
        complete: Boolean
    ): MutableList<ItemStack> {
        val list: MutableList<ItemStack> = mutableListOf()
        if (tileEntityData != null) {
            Helpers.readItemStacksFromNbt(list, tileEntityData.getList("stacks", Tag.TAG_COMPOUND.toInt()))
        }
        return list
    }
}
