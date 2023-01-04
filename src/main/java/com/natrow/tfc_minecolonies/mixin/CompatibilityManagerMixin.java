package com.natrow.tfc_minecolonies.mixin;

import java.util.Map;
import com.minecolonies.api.compatibility.CompatibilityManager;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.BlockStateStorage;
import com.mojang.logging.LogUtils;
import com.natrow.tfc_minecolonies.TFCMinecoloniesConstants;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(value = CompatibilityManager.class, remap = false)
public class CompatibilityManagerMixin
{
    private static final Logger LOGGER = LogUtils.getLogger();
    @Shadow @Final private Map<BlockStateStorage, ItemStorage> leavesToSaplingMap;

    @Inject(method = "getSaplingForLeaf", at = @At("RETURN"), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private void getSaplingForLeafInjector(BlockState block, CallbackInfoReturnable<ItemStack> cir, BlockStateStorage tempLeaf)
    {
        LOGGER.debug("getSaplingForLeafInjector {} {}", block, tempLeaf);

        if (!this.leavesToSaplingMap.containsKey(tempLeaf))
        {
            if (!TFCMinecoloniesConstants.LEAVES_TO_SAPLINGS.get().containsKey(block.getBlock()))
            {
                LOGGER.warn("Couldn't add {} to leavesToSaplingMap", tempLeaf);
            }
            ItemStack stack = TFCMinecoloniesConstants.LEAVES_TO_SAPLINGS.get().get(block.getBlock());
            this.leavesToSaplingMap.put(tempLeaf, new ItemStorage(stack, false, true));
            LOGGER.warn("Adding ({}, {}) to leavesToSaplingMap", tempLeaf, stack);
        }
    }
}
