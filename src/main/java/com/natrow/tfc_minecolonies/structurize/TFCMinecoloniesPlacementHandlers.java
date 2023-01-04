package com.natrow.tfc_minecolonies.structurize;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.ldtteam.structurize.placement.handlers.placement.IPlacementHandler;
import com.ldtteam.structurize.placement.handlers.placement.PlacementHandlers;
import com.natrow.tfc_minecolonies.TFCMinecoloniesConstants;
import com.natrow.tfc_minecolonies.item.TFCMinecoloniesItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import org.jetbrains.annotations.Nullable;

import net.dries007.tfc.common.blocks.StainedWattleBlock;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.ThatchBedBlock;
import net.dries007.tfc.common.blocks.devices.CharcoalForgeBlock;
import net.dries007.tfc.common.blocks.devices.FirepitBlock;
import net.dries007.tfc.common.blocks.devices.GrillBlock;
import net.dries007.tfc.common.blocks.devices.IngotPileBlock;
import net.dries007.tfc.common.blocks.devices.PotBlock;
import net.dries007.tfc.common.blocks.devices.SheetPileBlock;
import net.dries007.tfc.common.blocks.rock.RockAnvilBlock;
import net.dries007.tfc.common.blocks.soil.FarmlandBlock;
import net.dries007.tfc.common.blocks.soil.ISoilBlock;
import net.dries007.tfc.common.blocks.soil.PathBlock;
import net.dries007.tfc.common.items.HideItemType;
import net.dries007.tfc.common.items.TFCItems;
import net.dries007.tfc.util.Helpers;

import static com.ldtteam.structurize.api.util.constant.Constants.UPDATE_FLAG;

/**
 * Placement Handlers determine the behavior the builder uses while creating structures in survival mode.
 * Several TFC blocks require custom support to function as intended.
 */
public final class TFCMinecoloniesPlacementHandlers
{
    public static void registerHandlers()
    {
        // Override default placement handler for path blocks
        PlacementHandlers.add(new TFCSoilBlocksPlacementHandler());
        PlacementHandlers.add(new TFCThatchBedPlacementHandler());
        PlacementHandlers.add(new TFCStoneAnvilPlacementHandler());
        PlacementHandlers.add(new TFCWattlePlacementHandler());
        PlacementHandlers.add(new TFCFirepitPlacementHandler());
        PlacementHandlers.add(new TFCForgeHandler());
        PlacementHandlers.add(new TFCTileEntityPlacementHandler());
    }

    /**
     * Thatch beds need 2 thatch and one large raw hide to build
     */
    public static class TFCThatchBedPlacementHandler implements IPlacementHandler
    {
        @Override
        public boolean canHandle(Level level, BlockPos blockPos, BlockState blockState)
        {
            return blockState.getBlock() instanceof ThatchBedBlock;
        }

        @Override
        public IPlacementHandler.ActionProcessingResult handle(Level world, BlockPos pos, BlockState blockState, @Nullable CompoundTag tileEntityData, boolean complete, BlockPos centerPos)
        {
            if (blockState.getValue(BedBlock.PART) == BedPart.HEAD)
            {
                Direction facing = blockState.getValue(BedBlock.FACING);
                world.setBlock(pos.relative(facing.getOpposite()), blockState.setValue(BedBlock.PART, BedPart.FOOT), UPDATE_FLAG);
                world.setBlock(pos, blockState.setValue(BedBlock.PART, BedPart.HEAD), UPDATE_FLAG);
                if (tileEntityData != null)
                {
                    PlacementHandlers.handleTileEntityPlacement(tileEntityData, world, pos);
                    PlacementHandlers.handleTileEntityPlacement(tileEntityData, world, pos.relative(facing.getOpposite()));
                }

                return ActionProcessingResult.SUCCESS;
            }
            else
            {
                return ActionProcessingResult.PASS;
            }
        }

        @Override
        public List<ItemStack> getRequiredItems(Level world, BlockPos pos, BlockState blockState, @Nullable CompoundTag tileEntityData, boolean complete)
        {
            if (blockState.getValue(BedBlock.PART) == BedPart.HEAD)
            {
                List<ItemStack> list = new ArrayList<>();
                list.add(new ItemStack(TFCBlocks.THATCH.get().asItem(), 2));
                list.add(new ItemStack(TFCItems.HIDES.get(HideItemType.RAW).get(HideItemType.Size.LARGE).get(), 1));
                return list;
            }
            else
            {
                return Collections.emptyList();
            }
        }
    }

    /**
     * Anvils should be made with the corresponding raw stone type for a given stone anvil
     */
    public static class TFCStoneAnvilPlacementHandler implements IPlacementHandler
    {
        @Override
        public boolean canHandle(Level world, BlockPos pos, BlockState blockState)
        {
            return blockState.getBlock() instanceof RockAnvilBlock;
        }

        @Override
        public IPlacementHandler.ActionProcessingResult handle(Level world, BlockPos pos, BlockState blockState, @Nullable CompoundTag tileEntityData, boolean complete, BlockPos centerPos)
        {
            return world.setBlock(pos, blockState.getBlock().defaultBlockState(), UPDATE_FLAG) ? ActionProcessingResult.SUCCESS : ActionProcessingResult.DENY;
        }

        @Override
        public List<ItemStack> getRequiredItems(Level world, BlockPos pos, BlockState blockState, @Nullable CompoundTag tileEntityData, boolean complete)
        {
            List<ItemStack> itemList = new ArrayList<>();
            itemList.add(new ItemStack(TFCMinecoloniesConstants.ANVIL_TO_ROCK.get().get(blockState.getBlock()).asItem(), 1));
            return itemList;
        }
    }

    /**
     * Wattle can hold up to 4 sticks, encoded into its BlockState
     */
    public static class TFCWattlePlacementHandler implements IPlacementHandler
    {
        @Override
        public boolean canHandle(Level level, BlockPos blockPos, BlockState blockState)
        {
            return blockState.getBlock() instanceof StainedWattleBlock;
        }

        @Override
        public ActionProcessingResult handle(
            final Level world,
            final BlockPos pos,
            final BlockState blockState,
            @Nullable final CompoundTag tileEntityData,
            final boolean complete,
            final BlockPos centerPos)
        {
            return world.setBlock(pos, blockState, UPDATE_FLAG) ? ActionProcessingResult.SUCCESS : ActionProcessingResult.DENY;
        }

        @Override
        public List<ItemStack> getRequiredItems(Level level, BlockPos blockPos, BlockState blockState, @Nullable CompoundTag compoundTag, boolean b)
        {
            List<ItemStack> itemList = new ArrayList<>();

            if (blockState.getBlock() instanceof StainedWattleBlock block)
            {
                itemList.add(new ItemStack(block.asItem(), 1));
                int numSticks = 0;
                if (blockState.getValue(StainedWattleBlock.TOP)) {numSticks++;}
                if (blockState.getValue(StainedWattleBlock.BOTTOM)) {numSticks++;}
                if (blockState.getValue(StainedWattleBlock.LEFT)) {numSticks++;}
                if (blockState.getValue(StainedWattleBlock.RIGHT)) {numSticks++;}
                if (numSticks != 0)
                {
                    itemList.add(new ItemStack(Items.STICK, numSticks));
                }
            }

            return itemList;
        }
    }

    /**
     * Farmland & paths need to require the correct type of dirt
     */
    public static class TFCSoilBlocksPlacementHandler implements IPlacementHandler
    {
        @Override
        public boolean canHandle(Level level, BlockPos blockPos, BlockState blockState)
        {
            return blockState.getBlock() instanceof PathBlock || blockState.getBlock() instanceof FarmlandBlock;
        }

        @Override
        public ActionProcessingResult handle(
            final Level world,
            final BlockPos pos,
            final BlockState blockState,
            @Nullable final CompoundTag tileEntityData,
            final boolean complete,
            final BlockPos centerPos)
        {
            return world.setBlock(pos, blockState.getBlock().defaultBlockState(), UPDATE_FLAG) ? ActionProcessingResult.SUCCESS : ActionProcessingResult.DENY;
        }

        @Override
        public List<ItemStack> getRequiredItems(Level level, BlockPos blockPos, BlockState blockState, @Nullable CompoundTag compoundTag, boolean b)
        {
            List<ItemStack> itemList = new ArrayList<>();

            if (blockState.getBlock() instanceof ISoilBlock block)
            {
                itemList.add(new ItemStack(block.getDirt().getBlock().asItem(), 1));
            }

            return itemList;
        }
    }

    /**
     * Firepits require 3 sticks and 1 log, but since we must return an array of ItemStack, we use a custom
     * placeholder item.
     */
    public static class TFCFirepitPlacementHandler implements IPlacementHandler
    {
        @Override
        public boolean canHandle(Level level, BlockPos blockPos, BlockState blockState)
        {
            return blockState.getBlock() instanceof FirepitBlock;
        }

        @Override
        public ActionProcessingResult handle(
            final Level world,
            final BlockPos pos,
            final BlockState blockState,
            @Nullable final CompoundTag tileEntityData,
            final boolean complete,
            final BlockPos centerPos)
        {
            return world.setBlock(pos, blockState.getBlock().defaultBlockState(), UPDATE_FLAG) ? ActionProcessingResult.SUCCESS : ActionProcessingResult.DENY;
        }

        @Override
        public List<ItemStack> getRequiredItems(Level level, BlockPos blockPos, BlockState blockState, @Nullable CompoundTag compoundTag, boolean b)
        {
            List<ItemStack> itemList = new ArrayList<>();

            itemList.add(new ItemStack(TFCMinecoloniesItems.FIREWOOD.get(), 1));
            itemList.add(new ItemStack(Items.STICK, 3));

            if (blockState.getBlock() instanceof PotBlock)
            {
                itemList.add(new ItemStack(TFCItems.POT.get(), 1));
            }
            else if (blockState.getBlock() instanceof GrillBlock)
            {
                itemList.add(new ItemStack(TFCItems.WROUGHT_IRON_GRILL.get(), 1));
            }

            return itemList;
        }
    }

    /**
     * Forges require 8 charcoal
     */
    public static class TFCForgeHandler implements IPlacementHandler
    {
        @Override
        public boolean canHandle(Level level, BlockPos blockPos, BlockState blockState)
        {
            return blockState.getBlock() instanceof CharcoalForgeBlock;
        }

        @Override
        public ActionProcessingResult handle(
            final Level world,
            final BlockPos pos,
            final BlockState blockState,
            @Nullable final CompoundTag tileEntityData,
            final boolean complete,
            final BlockPos centerPos)
        {
            return world.setBlock(pos, blockState.getBlock().defaultBlockState(), UPDATE_FLAG) ? ActionProcessingResult.SUCCESS : ActionProcessingResult.DENY;
        }

        @Override
        public List<ItemStack> getRequiredItems(Level level, BlockPos blockPos, BlockState blockState, @Nullable CompoundTag compoundTag, boolean b)
        {
            List<ItemStack> itemList = new ArrayList<>();

            itemList.add(new ItemStack(Items.CHARCOAL, 8));

            return itemList;
        }
    }

    /**
     * Fix tile entities that have hidden inventories. These aren't detected automatically.
     */
    public static class TFCTileEntityPlacementHandler implements IPlacementHandler
    {
        @Override
        public boolean canHandle(Level level, BlockPos blockPos, BlockState blockState)
        {
            return blockState.getBlock() instanceof SheetPileBlock || blockState.getBlock() instanceof IngotPileBlock;
        }

        @Override
        public ActionProcessingResult handle(
            final Level world,
            final BlockPos pos,
            final BlockState blockState,
            @Nullable final CompoundTag tileEntityData,
            final boolean complete,
            final BlockPos centerPos)
        {
            if (world.setBlock(pos, blockState, 3))
            {
                if (tileEntityData != null)
                {
                    PlacementHandlers.handleTileEntityPlacement(tileEntityData, world, pos);
                }
                return ActionProcessingResult.SUCCESS;
            }
            else
            {
                return ActionProcessingResult.DENY;
            }
        }

        @Override
        public List<ItemStack> getRequiredItems(Level level, BlockPos blockPos, BlockState blockState, @Nullable CompoundTag compoundTag, boolean b)
        {
            List<ItemStack> itemList = new ArrayList<>();

            if (compoundTag != null)
            {
                Helpers.readItemStacksFromNbt(itemList, compoundTag.getList("stacks", Tag.TAG_COMPOUND));
            }

            return itemList;
        }
    }
}
