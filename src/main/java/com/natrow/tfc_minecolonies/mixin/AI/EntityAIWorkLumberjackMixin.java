package com.natrow.tfc_minecolonies.mixin.AI;

import com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.util.constant.IToolType;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.modules.settings.BoolSetting;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingLumberjack;
import com.minecolonies.coremod.colony.jobs.JobLumberjack;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAICrafting;
import com.minecolonies.coremod.entity.ai.citizen.lumberjack.EntityAIWorkLumberjack;
import com.natrow.tfc_minecolonies.minecolonies.TFCMinecoloniesToolType;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = EntityAIWorkLumberjack.class, remap = false)
public abstract class EntityAIWorkLumberjackMixin extends AbstractEntityAICrafting<JobLumberjack, BuildingLumberjack>
{
    public EntityAIWorkLumberjackMixin(@NotNull JobLumberjack job)
    {
        super(job);
    }

    @Inject(method = "prepareForWoodcutting", at = @At("HEAD"), cancellable = true)
    private void prepareForWoodcutting(CallbackInfoReturnable<IAIState> cir)
    {
        final IToolType axe = ToolType.AXE;
        final IToolType secondary = building.getSetting(BuildingLumberjack.REPLANT).getValue() ? TFCMinecoloniesToolType.SCYTHE // Scythe required for sapling drops
            : building.getOptionalSetting(AbstractBuilding.USE_SHEARS).orElse(new BoolSetting(true)).getValue() ? ToolType.SHEARS : ToolType.HOE;

        // require axe and either shears, a hoe, or a scythe depending on the building's settings.
        if (checkForToolOrWeapon(axe) || checkForToolOrWeapon(secondary))
        {
            cir.setReturnValue(AIWorkerState.START_WORKING);
        }
        else
        {
            cir.setReturnValue(AIWorkerState.LUMBERJACK_SEARCHING_TREE);
        }
    }
}
