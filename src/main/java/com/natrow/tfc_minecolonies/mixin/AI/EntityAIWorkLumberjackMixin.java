package com.natrow.tfc_minecolonies.mixin.AI;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.LUMBERJACK_GATHERING;
import static com.minecolonies.api.util.constant.Constants.TICKS_SECOND;
import static com.minecolonies.api.util.constant.StatisticsConstants.TREE_CUT;

import com.minecolonies.api.compatibility.Compatibility;
import com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.util.EntityUtils;
import com.minecolonies.api.util.constant.IToolType;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.modules.settings.BoolSetting;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingLumberjack;
import com.minecolonies.coremod.colony.jobs.JobLumberjack;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAICrafting;
import com.minecolonies.coremod.entity.ai.citizen.lumberjack.EntityAIWorkLumberjack;
import com.minecolonies.coremod.util.WorkerUtil;
import com.natrow.tfc_minecolonies.minecolonies.ITreeExtension;
import com.natrow.tfc_minecolonies.minecolonies.TFCMFakePlayerManager;
import com.natrow.tfc_minecolonies.minecolonies.TFCMToolType;
import net.dries007.tfc.common.blockentities.TickCounterBlockEntity;
import net.dries007.tfc.common.blocks.wood.TFCSaplingBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(value = EntityAIWorkLumberjack.class, remap = false)
public abstract class EntityAIWorkLumberjackMixin
    extends AbstractEntityAICrafting<JobLumberjack, BuildingLumberjack> {
  @Shadow @Final private static double XP_PER_TREE;
  @Shadow @Final private static int GATHERING_DELAY;
  @Shadow @Final private static int WAIT_BEFORE_SAPLING;
  @Shadow private boolean checkedInHut;
  @Shadow private BlockPos workFrom;

  public EntityAIWorkLumberjackMixin(@NotNull JobLumberjack job) {
    super(job);
  }

  @Shadow
  public abstract boolean walkToTree(BlockPos workAt);

  @Shadow
  protected abstract boolean checkIfStuck();

  @Shadow
  protected abstract void tryUnstuck();

  @Shadow
  protected abstract void plantSapling();

  @Shadow
  protected abstract boolean isOnSapling();

  /** Modify the tool requests to use a scythe */
  @Inject(method = "prepareForWoodcutting", at = @At("HEAD"), cancellable = true)
  private void prepareForWoodcuttingInjector(CallbackInfoReturnable<IAIState> cir) {
    final IToolType axe = ToolType.AXE;
    final IToolType secondary =
        building
                .getOptionalSetting(AbstractBuilding.USE_SHEARS)
                .orElse(new BoolSetting(true))
                .getValue()
            ? ToolType.SHEARS
            : building.getSetting(BuildingLumberjack.DEFOLIATE).getValue()
                ? TFCMToolType.SCYTHE
                : ToolType.HOE; // Speed up defoliation

    // require axe and either shears, a hoe, or a scythe depending on the building's settings.
    if (checkForToolOrWeapon(axe) || checkForToolOrWeapon(secondary)) {
      cir.setReturnValue(AIWorkerState.START_WORKING);
    } else {
      cir.setReturnValue(AIWorkerState.LUMBERJACK_SEARCHING_TREE);
    }
  }

  /**
   * Modify the tree chopping behavior to fit TFC gameplay. Start by cutting a path through leaves
   * to the trunk or by cutting all the leaves, then cut the bottom of the trunk.
   */
  @Inject(method = "chopTree", at = @At("HEAD"), cancellable = true)
  private void chopTreeInjector(CallbackInfoReturnable<IAIState> cir) {
    worker
        .getCitizenStatusHandler()
        .setLatestStatus(new TranslatableComponent("com.minecolonies.coremod.status.chopping"));

    final boolean shouldBreakLeaves = building.shouldDefoliate() || job.getTree().isNetherTree();

    // clear path to tree
    if (job.getTree().hasLogs()
        || (shouldBreakLeaves && job.getTree().hasLeaves())
        || checkedInHut) {
      if (!walkToTree(
          job.getTree().getStumpLocations().isEmpty()
              ? job.getTree().getLocation()
              : job.getTree().getStumpLocations().get(0))) {
        if (checkIfStuck()) {
          tryUnstuck();
        }
        cir.setReturnValue(getState());
        return;
      }
    }

    // plant sapling after tree removed
    if (!job.getTree().hasLogs() && (!shouldBreakLeaves || !job.getTree().hasLeaves())) {
      if (hasNotDelayed(WAIT_BEFORE_SAPLING)) {
        cir.setReturnValue(getState());
        return;
      }

      if (building.shouldReplant()) {
        plantSapling();
      } else {
        job.setTree(null);
        checkedInHut = false;
      }

      building.getColony().getStatisticsManager().increment(TREE_CUT);
      worker.getCitizenExperienceHandler().addExperience(XP_PER_TREE);
      incrementActionsDoneAndDecSaturation();
      workFrom = null;
      setDelay(TICKS_SECOND * GATHERING_DELAY);
      cir.setReturnValue(LUMBERJACK_GATHERING);
      return;
    }

    // set spawn-point if standing on a sapling
    if (isOnSapling()) {
      @Nullable final BlockPos spawnPoint = EntityUtils.getSpawnPoint(world, workFrom);
      if (spawnPoint != null) {
        WorkerUtil.setSpawnPoint(spawnPoint, worker);
      }
    }

    // remove leaves from tree
    if (job.getTree().hasLeaves() && shouldBreakLeaves) {
      final BlockPos leaf = job.getTree().peekNextLeaf();
      // custom handling on scythes
      if (!building
          .getOptionalSetting(AbstractBuilding.USE_SHEARS)
          .orElse(new BoolSetting(true))
          .getValue()) {
        if (!mineBlock(
            leaf,
            workFrom,
            false,
            false,
            TFCMFakePlayerManager.getToolBreakAction(
                world,
                leaf,
                worker.getItemInHand(InteractionHand.MAIN_HAND),
                worker.blockPosition(),
                worker.getCitizenItemHandler()))) {
          cir.setReturnValue(getState());
          return;
        }
      } else if (!mineBlock(leaf, workFrom)) {
        cir.setReturnValue(getState());
        return;
      }
      job.getTree().pollNextLeaf();
      ((ITreeExtension) job.getTree()).recalcLeaves(world);
    }

    // remove trunk
    else if (job.getTree().hasLogs()) {
      // take first log from queue
      final BlockPos log = job.getTree().peekNextLog();

      if (job.getTree().isDynamicTree()) {
        // Dynamic Trees handles drops/tool dmg upon tree break, so those are set to false here
        if (!mineBlock(
            log,
            workFrom,
            false,
            false,
            Compatibility.getDynamicTreeBreakAction(
                world,
                log,
                worker.getItemInHand(InteractionHand.MAIN_HAND),
                worker.blockPosition()))) {
          cir.setReturnValue(getState());
          return;
        }
        // Successfully mined Dynamic tree, count as 6 actions done(1+5)
        for (int i = 0; i < 6; i++) {
          this.incrementActionsDone();
        }
        // Wait 5 sec for falling trees(dyn tree feature)/drops
        setDelay(100);
      } else {
        if (!mineBlock(
            log,
            workFrom,
            false,
            false,
            TFCMFakePlayerManager.getToolBreakAction(
                world,
                log,
                worker.getItemInHand(InteractionHand.MAIN_HAND),
                worker.blockPosition(),
                worker.getCitizenItemHandler()))) {
          cir.setReturnValue(getState());
          return;
        }
      }
      job.getTree().pollNextLog();
      int blocksBroken = ((ITreeExtension) job.getTree()).recalcWood(world);
      for (int i = 0; i < blocksBroken; i++) this.incrementActionsDone();
      worker.decreaseSaturationForContinuousAction();
    }

    // continue working
    cir.setReturnValue(getState());
  }

  /**
   * Ensure that saplings are placed with correct tick counters, otherwise they can grow instantly.
   */
  @Inject(
      method = "placeSaplings",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lcom/minecolonies/api/inventory/InventoryCitizen;extractItem(IIZ)Lnet/minecraft/world/item/ItemStack;"),
      locals = LocalCapture.CAPTURE_FAILEXCEPTION)
  private void placeSaplingsMixin(
      int saplingSlot, ItemStack stack, Block block, CallbackInfo ci, BlockPos pos) {
    if (this.world.getBlockState(pos).getBlock() instanceof TFCSaplingBlock) {
      TickCounterBlockEntity.reset(this.world, pos);
    }
  }
}
