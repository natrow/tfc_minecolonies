package com.natrow.tfc_minecolonies.mixin;

import com.minecolonies.api.util.constant.IToolType;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingLumberjack;
import com.minecolonies.coremod.util.WorkerUtil;
import com.natrow.tfc_minecolonies.minecolonies.TFCMinecoloniesToolType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.dries007.tfc.common.TFCTags;

@Mixin(value = WorkerUtil.class, remap = false)
public abstract class WorkerUtilMixin
{
    /**
     * Use TFC tools for appropriate blocks
     */
    @Inject(method = "getBestToolForBlock", at = @At(value = "INVOKE", target = "Lcom/minecolonies/api/util/constant/ToolType;getToolType(Ljava/lang/String;)Lcom/minecolonies/api/util/constant/IToolType;"), cancellable = true)
    private static void getBestToolForBlockInjector(BlockState state, float blockHardness, AbstractBuilding building, CallbackInfoReturnable<IToolType> cir)
    {
        // detect if block is harvestable with a Scythe
        if (state.is(TFCTags.Blocks.MINEABLE_WITH_SCYTHE))
        {
            // detect whether lumberjack is in defoliate mode
            if (building instanceof BuildingLumberjack)
            {
                if (building.getSetting(BuildingLumberjack.DEFOLIATE).getValue())
                {
                    cir.setReturnValue(TFCMinecoloniesToolType.SCYTHE);
                }
            }
        }
    }
}
