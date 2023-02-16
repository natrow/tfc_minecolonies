package com.natrow.tfc_minecolonies.mixin;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.coremod.entity.ai.citizen.lumberjack.Tree;
import com.natrow.tfc_minecolonies.TFCMConstants;
import com.natrow.tfc_minecolonies.minecolonies.ITreeExtension;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.dries007.tfc.common.blocks.wood.LogBlock;
import net.dries007.tfc.common.blocks.wood.TFCLeavesBlock;

@Mixin(value = Tree.class, remap = false)
public abstract class TreeMixin implements ITreeExtension
{
    /**
     * Converts a leaf block to a sapling using a lookup map rather than loot tables.
     * TFC doesn't do sapling drops unless the correct tool is used.
     */
    @Inject(method = "getSaplingsForLeaf", at = @At("HEAD"), cancellable = true)
    private static void getSaplingsForLeafInjector(ServerLevel world, BlockPos position, CallbackInfoReturnable<List<ItemStack>> cir)
    {
        Block block = world.getBlockState(position).getBlock();

        if (block instanceof TFCLeavesBlock leaves)
        {
            ArrayList<ItemStack> list = new ArrayList<>();
            list.add(TFCMConstants.LEAVES_TO_SAPLINGS.get().get(leaves));
            cir.setReturnValue(list);
        }
    }

    /**
     * Special treatment for TFC trees. This helps prevent the lumberjack from leaving behind a field of leafless trees.
     */
    @Inject(method = "hasEnoughLeavesAndIsSupposedToCut", at = @At("HEAD"), cancellable = true)
    private static void hasEnoughLeavesAndIsSupposedToCutInjector(@NotNull LevelReader world, BlockPos pos, List<ItemStorage> treesToNotCut, CallbackInfoReturnable<Boolean> cir)
    {
        final BlockState currentState = world.getBlockState(pos);
        final Block currentBlock = currentState.getBlock();

        if (currentBlock instanceof LogBlock)
        {
            // TFC trees have an extra block state indicating whether they are natural
            if (currentState.getValue(LogBlock.NATURAL))
            {
                final ItemStack sapling = TFCMConstants.LOG_TO_SAPLINGS.get().get(currentBlock);
                for (final ItemStorage stack : treesToNotCut)
                {
                    // Check if sapling is on blacklist
                    if (ItemStackUtils.compareItemStacksIgnoreStackSize(sapling, stack.getItemStack()))
                    {
                        cir.setReturnValue(false);
                        return;
                    }
                }
                cir.setReturnValue(true);
                return;
            }
            cir.setReturnValue(false);
        }
    }

    @Shadow private LinkedList<BlockPos> leaves;
    @Shadow private LinkedList<BlockPos> woodBlocks;

    /**
     * Recalculate whether leaves are "real" using their block tags.
     * This is necessary because leaves can be destroyed rapidly in TFC and breaking all the air blocks slows down the forester to a crawl.
     */
    @Override
    public void recalcLeaves(Level level)
    {
        leaves.removeIf(leaf -> !level.getBlockState(leaf).is(BlockTags.LEAVES));
    }

    /**
     * Recalculate whether logs are "real" using their block tags.
     * This is necessary because all logs are destroyed in a single axe swing in TFC.
     */
    @Override
    public int recalcWood(Level level)
    {
        int startSize = woodBlocks.size();
        woodBlocks.removeIf(wood -> !level.getBlockState(wood).is(BlockTags.LOGS));
        return woodBlocks.size() - startSize;
    }
}
