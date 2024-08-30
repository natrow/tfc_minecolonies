package com.natrow.tfcm.mixin.minecolonies.ai;

import com.google.common.reflect.TypeToken;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.minecolonies.api.colony.interactionhandling.IInteractionResponseHandler;
import com.minecolonies.api.colony.requestsystem.requestable.StackList;
import com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.constant.ToolLevelConstants;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.api.util.constant.translation.RequestSystemTranslationConstants;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingFarmer;
import com.minecolonies.core.colony.fields.FarmField;
import com.minecolonies.core.colony.jobs.JobFarmer;
import com.minecolonies.core.entity.ai.workers.crafting.AbstractEntityAICrafting;
import com.minecolonies.core.entity.ai.workers.production.agriculture.EntityAIWorkFarmer;
import com.natrow.tfcm.TFCM;
import com.natrow.tfcm.TranslationConstantsKt;
import com.natrow.tfcm.mixinimpl.minecolonies.ai.ClimateCheckError;
import com.natrow.tfcm.mixinimpl.minecolonies.ai.EntityAIWorkFarmerExt;
import com.natrow.tfcm.mixinimpl.minecolonies.ai.EntityAIWorkFarmerImplKt;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.crop.*;
import net.dries007.tfc.common.blocks.soil.FarmlandBlock;
import net.dries007.tfc.util.Fertilizer;
import net.dries007.tfc.util.Helpers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

@Mixin(value = EntityAIWorkFarmer.class, remap = false)
@Implements(@Interface(iface = EntityAIWorkFarmerExt.class, prefix = "tfcm$"))
public abstract class EntityAIWorkFarmerMixin extends AbstractEntityAICrafting<JobFarmer, BuildingFarmer> {
    @Unique
    private Logger tfcm$LOGGER = TFCM.INSTANCE.getLOGGER();

    @Shadow
    protected abstract BlockPos getSurfacePos(BlockPos position);

    public boolean tfcm$checkField(FarmField field, @NotNull Predicate<BlockPos> predicate) {
        tfcm$LOGGER.debug("checkField()");
        BlockPos start = field.getPosition();
        for (int z = -field.getRadius(Direction.NORTH); z <= field.getRadius(Direction.SOUTH); z++) {
            for (int x = -field.getRadius(Direction.WEST); x <= field.getRadius(Direction.EAST); x++) {
                if (x == 0 && z == 0) continue; // skip field block itself
                BlockPos pos = getSurfacePos(start.south(z).east(x)); // apply offset
                if (!predicate.test(pos)) return false;
            }
        }
        return true;
    }

    /**
     * Fake constructor (deleted when mixin is applied)
     */
    public EntityAIWorkFarmerMixin(@NotNull JobFarmer job) {
        super(job);
    }

    /**
     * Detect which items can be used as fertilizer
     */
    @Inject(method = "isCompost", at = @At("HEAD"), cancellable = true)
    private void isTFCCompost(ItemStack itemStack, CallbackInfoReturnable<Boolean> cir) {
        tfcm$LOGGER.debug("isTFCCompost()");
        cir.setReturnValue(Fertilizer.get(itemStack) != null);
    }

    /**
     * Request TFC fertilizer items
     */
    @WrapOperation(method = "prepareForFarming", at = @At(value = "NEW", target = "(Ljava/util/List;Ljava/lang/String;II)Lcom/minecolonies/api/colony/requestsystem/requestable/StackList;"))
    private StackList requestTFCCompost(List<ItemStack> stacks, String description, int count, int minCount, Operation<StackList> original) {
        tfcm$LOGGER.debug("requestTFCCompost()");
        // get all valid items from the fertilizer manager
        final List<ItemStack> fertilizerItems = Fertilizer.MANAGER.getValues().stream().flatMap(f -> f.getValidItems().stream().map(ItemStack::new)).toList();
        return new StackList(fertilizerItems, description, count, minCount);
    }

    /**
     * Attempt to request the required fertilizer (and sticks if applicable) for the current field
     */
    @Inject(method = "prepareForFarming", at = @At(value = "INVOKE", target = "Lcom/minecolonies/api/colony/ICitizenData;setVisibleStatus(Lcom/minecolonies/api/entity/citizen/VisibleCitizenStatus;)V"), cancellable = true)
    private void requestSpecificCompostOrSticks(CallbackInfoReturnable<IAIState> cir, @Local FarmField farmField) {
        tfcm$LOGGER.debug("requestSpecificCompostOrSticks()");
        final Predicate<ItemStack> findFertilizers = EntityAIWorkFarmerImplKt.isFertilizer(farmField);
        if (findFertilizers == null) return;

        // count amount of fertilizer available
        final int fertilizerInBuilding = InventoryUtils.hasBuildingEnoughElseCount(building, findFertilizers, 1);
        final int fertilizerInInventory = InventoryUtils.getItemCountInItemHandler(worker.getInventoryCitizen(), findFertilizers);

        if (fertilizerInBuilding + fertilizerInInventory <= 0) {
            // request more
            if (building.requestFertilizer() && !building.hasWorkerOpenRequestsOfType(worker.getCitizenData().getId(), TypeToken.of(StackList.class))) {
                final List<ItemStack> fertilizerItems = EntityAIWorkFarmerImplKt.getFertilizers(farmField);
                if (fertilizerItems != null && !fertilizerItems.isEmpty()) {
                    worker.getCitizenData().createRequestAsync(new StackList(fertilizerItems, RequestSystemTranslationConstants.REQUEST_TYPE_FERTILIZER, Constants.STACKSIZE, 1));
                }
            }
        } else if (fertilizerInInventory <= 0 && fertilizerInBuilding > 0) {
            // go back and gather materials
            needsCurrently = new Tuple<>(findFertilizers, Constants.STACKSIZE);
            cir.setReturnValue(AIWorkerState.GATHERING_REQUIRED_MATERIALS);
            return;
        }

        // request sticks if necessary
        if (EntityAIWorkFarmerImplKt.getCrop(farmField) instanceof ClimbingCropBlock) {
            final Predicate<ItemStack> findSticks = itemStack -> Helpers.isItem(itemStack.getItem(), Tags.Items.RODS_WOODEN);
            final int sticksInBuilding = InventoryUtils.hasBuildingEnoughElseCount(building, findSticks, 1);
            final int sticksInInventory = InventoryUtils.getItemCountInItemHandler(worker.getInventoryCitizen(), findSticks);

            if (sticksInBuilding + sticksInInventory <= 0) {
                if (!building.hasWorkerOpenRequestsOfType(worker.getCitizenData().getId(), TypeToken.of(StackList.class))) {
                    final List<ItemStack> stickItems = Objects.requireNonNull(ForgeRegistries.ITEMS.tags()).getTag(Tags.Items.RODS_WOODEN).stream().map(ItemStack::new).toList();
                    worker.getCitizenData().createRequestAsync(new StackList(stickItems, TranslationConstantsKt.REQUEST_STICKS, Constants.STACKSIZE, 1));
                }
            } else if (sticksInInventory <= 0 && sticksInBuilding > 0) {
                // go back and gather materials
                needsCurrently = new Tuple<>(findSticks, Constants.STACKSIZE);
                cir.setReturnValue(AIWorkerState.GATHERING_REQUIRED_MATERIALS);
            }
        }
    }

    /**
     * Check if a block will become a TFC farmland block after being hoed
     */
    @ModifyExpressionValue(method = "findHoeableSurface", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z"))
    private boolean hoeableOnlyIfTFCFarmland(boolean original, @Local(name = "toolModifiedState") BlockState result) {
        tfcm$LOGGER.debug("hoeOnlyIfTFCFarmland()");
        return result.getBlock() instanceof FarmlandBlock;
    }

    /**
     * Place the correct farmland block after hoeing
     */
    @ModifyExpressionValue(method = "hoeIfAble", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;defaultBlockState()Lnet/minecraft/world/level/block/state/BlockState;"))
    private BlockState hoeTFCSoil(BlockState original, @Local(argsOnly = true) BlockPos position) {
        tfcm$LOGGER.debug("hoeTFCSoil()");
        final BlockState soil = world.getBlockState(position);
        final BlockHitResult blockHitResult = new BlockHitResult(Vec3.ZERO, Direction.UP, position, false);
        final UseOnContext useOnContext = new UseOnContext(world,
                null,
                InteractionHand.MAIN_HAND,
                getInventory().getStackInSlot(InventoryUtils.getFirstSlotOfItemHandlerContainingTool(getInventory(), ToolType.HOE, ToolLevelConstants.TOOL_LEVEL_WOOD_OR_GOLD, building.getMaxToolLevel())),
                blockHitResult);
        return soil.getToolModifiedState(useOnContext, ToolActions.HOE_TILL, true);
    }

    /**
     * Check for TFC farmland, room for crops to grow, and water for flooded crops
     */
    @Inject(method = "findPlantableSurface", at = @At(value = "HEAD"), cancellable = true)
    private void findPlantableSurfaceTFC(BlockPos position, FarmField farmField, CallbackInfoReturnable<BlockPos> cir) {
        tfcm$LOGGER.debug("findPlantableSurfaceTFC()");
        position = getSurfacePos(position);
        if (position != null && !farmField.isNoPartOfField(world, position) && world.getBlockState(position).getBlock() instanceof FarmlandBlock) {
            CropBlock crop = EntityAIWorkFarmerImplKt.getCrop(farmField);

            if (crop instanceof SpreadingCropBlock) {
                // spreading crops need adjacent blocks to also be empty
                if (world.isEmptyBlock(position.above())
                        && world.isEmptyBlock(position.north().above())
                        && world.isEmptyBlock(position.south().above())
                        && world.isEmptyBlock(position.east().above())
                        && world.isEmptyBlock(position.west().above())) {
                    cir.setReturnValue(position);
                    return;
                }
            } else if (crop instanceof DoubleCropBlock) {
                // double crops need the block above them to also be empty
                if (world.isEmptyBlock(position.above())
                        && world.isEmptyBlock(position.above().above())) {
                    cir.setReturnValue(position);
                    return;
                }
            } else if (crop instanceof FloodedCropBlock) {
                // flooded crops can only grow in water
                if (world.getBlockState(position.above()).is(Blocks.WATER)) {
                    cir.setReturnValue(position);
                    return;
                }
            }
            // regular crops don't need any special requirements
            else if (crop != null && world.isEmptyBlock(position.above())) {
                // allow other crops to be grown
                cir.setReturnValue(position);
                return;
            }
        }
        cir.setReturnValue(null);
    }

    /**
     * Check if a TFC crop has the right growing conditions in the field.
     * If this didn't exist, the farmer's crops would simply die every time they're planted.
     */
    @Inject(method = "canGoPlanting", at = @At(value = "INVOKE", target = "Lcom/minecolonies/api/entity/citizen/AbstractEntityCitizen;getCitizenInventoryHandler()Lcom/minecolonies/api/entity/citizen/citizenhandlers/ICitizenInventoryHandler;"), cancellable = true)
    private void checkSeedClimate(FarmField farmField, CallbackInfoReturnable<IAIState> cir, @Local ItemStack seeds) {
        tfcm$LOGGER.debug("checkSeedClimate()");
        // attempt to get a TFC crop from the current seeds
        if (seeds.getItem() instanceof BlockItem seedsBlock && seedsBlock.getBlock() instanceof CropBlock crop) {
            // create a predicate that will check the climate and trigger interactions when necessary
            final Predicate<BlockPos> predicate = (pos) -> {
                // do the actual climate check
                final List<ClimateCheckError> errors = EntityAIWorkFarmerImplKt.checkClimate(world, pos, crop);

                // trigger an interaction for each error
                errors.forEach(error -> {
                    final IInteractionResponseHandler interaction = EntityAIWorkFarmerImplKt.createInteraction(error, farmField);
                    worker.getCitizenData().triggerInteraction(interaction);
                });

                // predicate succeeds if there were no errors
                return errors.isEmpty();
            };

            // if the check fails, skip planting entirely
            if (!tfcm$checkField(farmField, predicate)) {
                cir.setReturnValue(AIWorkerState.PREPARING);
                tfcm$LOGGER.debug("checkSeedClimate() FAILED");
            } else {
                tfcm$LOGGER.debug("checkSeedClimate() SUCCESS");
            }
        }
    }

    /**
     * Ignore liquids when searching for the surface
     */
    @ModifyExpressionValue(method = "getSurfacePos(Lnet/minecraft/core/BlockPos;Ljava/lang/Integer;)Lnet/minecraft/core/BlockPos;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;liquid()Z"))
    private boolean ignoreLiquid(boolean original, @Local BlockState curBlockState, @Local Block curBlock) {
        tfcm$LOGGER.debug("ignoreLiquid()");
        return false;
    }

    /**
     * Ignore TFC melons, pumpkins, etc. when searching for the surface
     */
    @ModifyExpressionValue(method = "getSurfacePos(Lnet/minecraft/core/BlockPos;Ljava/lang/Integer;)Lnet/minecraft/core/BlockPos;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;isSolid()Z"))
    private boolean ignoreTFCCrops(boolean original, @Local Block currBlock) {
        tfcm$LOGGER.debug("ignoreTFCCrops()");
        return original && !(currBlock instanceof DecayingBlock) && !(currBlock == TFCBlocks.ROTTEN_MELON.get()) && !(currBlock == TFCBlocks.ROTTEN_PUMPKIN.get());
    }

    @Inject(method = "plantCrop", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;setBlockAndUpdate(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z"), cancellable = true)
    private void skipSpreadables(ItemStack item, BlockPos position, CallbackInfoReturnable<Boolean> cir, @Local BlockItem seed) {
        tfcm$LOGGER.debug("skipSpreadables()");
        if (seed.getBlock() instanceof SpreadingCropBlock
                && (world.getBlockState(position.north().above()).getBlock() instanceof SpreadingCropBlock
                || world.getBlockState(position.north().above()).getBlock() instanceof SpreadingCropBlock
                || world.getBlockState(position.north().above()).getBlock() instanceof SpreadingCropBlock
                || world.getBlockState(position.north().above()).getBlock() instanceof SpreadingCropBlock)) {
            cir.setReturnValue(true);
        }
    }

    @ModifyExpressionValue(method = "plantCrop", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;defaultBlockState()Lnet/minecraft/world/level/block/state/BlockState;"))
    private BlockState plantFloodedCrops(BlockState original, @Local BlockItem seed, @Local(argsOnly = true) BlockPos pos) {
        tfcm$LOGGER.debug("plantFloodedCrops()");
        if (seed.getBlock() instanceof FloodedCropBlock crop) {
            FluidState fluidState = world.getFluidState(pos.above());
            if (!fluidState.isEmpty() && crop.getFluidProperty().canContain(fluidState.getType())) {
                return original.setValue(crop.getFluidProperty(), crop.getFluidProperty().keyFor(fluidState.getType()));
            }
        }
        return original;
    }
}
