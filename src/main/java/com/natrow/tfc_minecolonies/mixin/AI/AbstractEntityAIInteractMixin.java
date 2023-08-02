package com.natrow.tfc_minecolonies.mixin.AI;

import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.jobs.AbstractJob;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIInteract;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAISkill;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = AbstractEntityAIInteract.class, remap = false)
public abstract class AbstractEntityAIInteractMixin<
        J extends AbstractJob<?, J>, B extends AbstractBuilding>
    extends AbstractEntityAISkill<J, B> {
  protected AbstractEntityAIInteractMixin(@NotNull J job) {
    super(job);
  }

  /** Inject mining animations while workers are mining a block */
  @Inject(
      method =
          "mineBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;ZZLjava/lang/Runnable;)Z",
      at =
          @At(
              value = "INVOKE_ASSIGN",
              target =
                  "Lcom/minecolonies/coremod/entity/ai/basic/AbstractEntityAIInteract;checkMiningLocation(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;)Z"))
  private void mineBlockInjector(
      BlockPos blockToMine,
      BlockPos safeStand,
      boolean damageTool,
      boolean getDrops,
      Runnable blockBreakAction,
      CallbackInfoReturnable<Boolean> cir) {
    this.worker.getCitizenItemHandler().hitBlockWithToolInHand(blockToMine);
  }
}
