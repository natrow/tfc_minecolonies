package com.natrow.tfc_minecolonies.mixin;

import java.util.ArrayList;
import java.util.List;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.coremod.entity.ai.citizen.lumberjack.Tree;
import com.mojang.logging.LogUtils;
import com.natrow.tfc_minecolonies.TFCMinecoloniesConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.dries007.tfc.common.blocks.wood.TFCLeavesBlock;

@Mixin(value = Tree.class, remap = false)
public abstract class TreeMixin
{
    private static final Logger LOGGER = LogUtils.getLogger();

    @Inject(method = "getSaplingsForLeaf", at = @At("HEAD"), cancellable = true)
    private static void getSaplingsForLeafInjector(ServerLevel world, BlockPos position, CallbackInfoReturnable<List<ItemStack>> cir)
    {
        Block block = world.getBlockState(position).getBlock();

        if (block instanceof TFCLeavesBlock leaves)
        {
            ArrayList<ItemStack> list = new ArrayList<>();
            list.add(TFCMinecoloniesConstants.LEAVES_TO_SAPLINGS.get().get(leaves));
            LOGGER.debug("getSaplingsForLeafInjector {} {}", leaves, list.get(0));
            cir.setReturnValue(list);
        }
    }

    @Inject(method = "supposedToCut", at = @At(value = "HEAD"), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private static void supposedToCutInjector(LevelReader world, List<ItemStorage> treesToNotCut, BlockPos leafPos, CallbackInfoReturnable<Boolean> cir)
    {
        ItemStack sap = IColonyManager.getInstance().getCompatibilityManager().getSaplingForLeaf(world.getBlockState(leafPos));

        LOGGER.debug("supposedToCutInjector {} {}", world.getBlockState(leafPos).getBlock(), sap);
    }
}
