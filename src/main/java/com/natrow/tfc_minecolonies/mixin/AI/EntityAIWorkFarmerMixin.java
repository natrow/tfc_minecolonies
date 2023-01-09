package com.natrow.tfc_minecolonies.mixin.AI;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.interactionhandling.ChatPriority;
import com.minecolonies.api.colony.requestsystem.requestable.StackList;
import com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.tileentities.AbstractScarecrowTileEntity;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.constant.translation.RequestSystemTranslationConstants;
import com.minecolonies.coremod.blocks.BlockScarecrow;
import com.minecolonies.coremod.colony.buildings.modules.FarmerFieldModule;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingFarmer;
import com.minecolonies.coremod.colony.interactionhandling.PosBasedInteraction;
import com.minecolonies.coremod.colony.jobs.JobFarmer;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAICrafting;
import com.minecolonies.coremod.entity.ai.citizen.farmer.EntityAIWorkFarmer;
import com.minecolonies.coremod.tileentities.ScarecrowTileEntity;
import com.mojang.logging.LogUtils;
import com.natrow.tfc_minecolonies.TFCMConstants;
import com.natrow.tfc_minecolonies.TFCMTranslationConstants;
import com.natrow.tfc_minecolonies.minecolonies.TFCMFakePlayerManager;
import com.natrow.tfc_minecolonies.minecolonies.TFCMFarmerExtension;
import com.natrow.tfc_minecolonies.tags.TFCMTags;
import com.natrow.tfc_minecolonies.util.TFCMCropUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WebBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.TriPredicate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.dries007.tfc.common.blockentities.TFCBlockEntities;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.crop.CropBlock;
import net.dries007.tfc.common.blocks.crop.CropHelpers;
import net.dries007.tfc.common.blocks.crop.DeadCropBlock;
import net.dries007.tfc.common.blocks.crop.DecayingBlock;
import net.dries007.tfc.common.blocks.crop.DoubleCropBlock;
import net.dries007.tfc.common.blocks.crop.FloodedCropBlock;
import net.dries007.tfc.common.blocks.crop.SpreadingCropBlock;
import net.dries007.tfc.common.blocks.soil.FarmlandBlock;
import net.dries007.tfc.util.Fertilizer;
import net.dries007.tfc.util.climate.Climate;
import net.dries007.tfc.util.climate.ClimateRange;

@Mixin(value = EntityAIWorkFarmer.class, remap = false)
public abstract class EntityAIWorkFarmerMixin extends AbstractEntityAICrafting<JobFarmer, BuildingFarmer> implements TFCMFarmerExtension
{
    private static final Logger LOGGER = LogUtils.getLogger();
    @Shadow @Final private static int MAX_DEPTH;
    @Shadow private @Nullable BlockPos prevPos;
    @Shadow private @Nullable BlockPos workingOffset;
    @Shadow private boolean shouldDumpInventory;

    public EntityAIWorkFarmerMixin(@NotNull JobFarmer job)
    {
        super(job);
    }

    @Override
    public boolean checkField(BlockPos fieldPos, TriPredicate<BlockPos, ClimateRange, Level> predicate)
    {
        BlockEntity blockEntity = world.getBlockEntity(fieldPos);
        if (blockEntity instanceof ScarecrowTileEntity field)
        {
            CropBlock crop = TFCMCropUtil.getCrop(field);
            if (crop == null) return true;
            ClimateRange climate = crop.getClimateRange();

            BlockPos start = field.getPosition();
            for (int z = -field.getRadius(Direction.NORTH); z <= field.getRadius(Direction.SOUTH); z++)
            {
                for (int x = -field.getRadius(Direction.WEST); x <= field.getRadius(Direction.EAST); x++)
                {
                    if (x == 0 && z == 0) continue; // skip field block itself
                    BlockPos pos = getSurfacePos(start.south(z).east(x));
                    if (!predicate.test(pos, climate, world)) return false;
                }
            }
        }
        return true;
    }

    @Shadow
    protected abstract BlockPos findHoeableSurface(@NotNull BlockPos position, @NotNull ScarecrowTileEntity field);

    @Shadow
    protected abstract BlockPos getSurfacePos(BlockPos position);

    @Shadow
    protected abstract BlockPos getSurfacePos(BlockPos position, Integer depth);

    @Shadow
    protected abstract BlockPos nextValidCell(AbstractScarecrowTileEntity field);

    @Shadow
    protected abstract BlockPos findPlantableSurface(@NotNull BlockPos position, @NotNull ScarecrowTileEntity field);

    @Shadow
    protected abstract BlockPos findHarvestableSurface(@NotNull BlockPos position);

    /**
     * Detect items which can be used as Fertilizer
     */
    @Inject(method = "isCompost", at = @At("HEAD"), cancellable = true)
    private void isCompostInjector(ItemStack itemStack, CallbackInfoReturnable<Boolean> cir)
    {
        cir.setReturnValue(Fertilizer.get(itemStack) != null);
    }

    /**
     * Reset fertilizer list & use TFC fertilizers instead
     */
    @ModifyVariable(method = "prepareForFarming", name = "compostAbleItems", at = @At("LOAD"))
    private List<ItemStack> prepareForFarmingInjector(List<ItemStack> value)
    {
        // Note: this is recomputed at runtime to ensure any data pack changes are accounted for.
        final List<ItemStack> items = Fertilizer.MANAGER.getValues().stream().flatMap(f -> f.getValidItems().stream().map(i -> new ItemStack(i, 1))).collect(Collectors.toList());
        LOGGER.warn("Overwriting fertilizer items... ({})", items);
        return items;
    }

    /**
     * Try to get the correct type of fertilizer for the current field.
     */
    @Inject(method = "prepareForFarming", at = @At(value = "INVOKE", target = "Lcom/minecolonies/coremod/entity/ai/citizen/farmer/EntityAIWorkFarmer;checkIfShouldExecute(Lcom/minecolonies/coremod/tileentities/ScarecrowTileEntity;Ljava/util/function/Predicate;)Z", ordinal = 0), cancellable = true, locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private void prepareForFarmingInjector(CallbackInfoReturnable<IAIState> cir, final FarmerFieldModule module, int int1, int int2, BlockPos currentField, BlockEntity entity)
    {
        ScarecrowTileEntity field = (ScarecrowTileEntity) entity;
        Predicate<ItemStack> findFertilizers = TFCMCropUtil.isFertilizer(field);
        if (findFertilizers == null) return;

        // count amount of fertilizer available
        final int fertilizerInBuilding = InventoryUtils.hasBuildingEnoughElseCount(building, findFertilizers, 1);
        final int fertilizerInInventory = InventoryUtils.getItemCountInItemHandler(worker.getInventoryCitizen(), findFertilizers);

        if (fertilizerInBuilding + fertilizerInInventory <= 0)
        {
            // attempt to request more
            if (building.requestFertilizer() && !building.hasWorkerOpenRequestsOfType(worker.getCitizenData().getId(),
                TypeToken.of(StackList.class)))
            {
                final List<ItemStack> fertilizerItems = TFCMCropUtil.getFertilizers(field);
                if (fertilizerItems != null && !fertilizerItems.isEmpty())
                {
                    worker.getCitizenData().createRequestAsync(new StackList(fertilizerItems, RequestSystemTranslationConstants.REQUEST_TYPE_FERTILIZER, Constants.STACKSIZE, 1));
                }
            }
        }
        else if (fertilizerInInventory <= 0 && fertilizerInBuilding > 0)
        {
            // go back and gather materials
            needsCurrently = new Tuple<>(findFertilizers, Constants.STACKSIZE);
            cir.setReturnValue(AIWorkerState.GATHERING_REQUIRED_MATERIALS);
        }
    }

    /**
     * Replace blocks with matching TFC farmland block, otherwise vanilla farmland
     */
    @Inject(method = "hoeIfAble", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;setBlockAndUpdate(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z"), cancellable = true)
    private void hoeIfAbleInjector(BlockPos position, ScarecrowTileEntity field, CallbackInfoReturnable<Boolean> cir)
    {
        position = findHoeableSurface(position, field);
        world.setBlockAndUpdate(position, TFCMConstants.SOIL_TO_FARMLAND.get().getOrDefault(world.getBlockState(position).getBlock(), Blocks.FARMLAND).defaultBlockState());
        world.playSound(null, position, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
        worker.getCitizenItemHandler().damageItemInHand(InteractionHand.MAIN_HAND, 1);
        worker.decreaseSaturationForContinuousAction();
        cir.setReturnValue(true);
    }

    /**
     * By default, minecolonies only checks for #minecraft:dirt. This isn't sophisticated enough
     * for TFC, because adding other tillable blocks to #minecraft:dirt produces unwanted side effects.
     * Instead, I made my own tag.
     */
    @Inject(method = "findHoeableSurface", at = @At("HEAD"), cancellable = true)
    private void findHoeableSurfaceInjector(BlockPos position, ScarecrowTileEntity field, CallbackInfoReturnable<BlockPos> cir)
    {
        position = getSurfacePos(position);
        // todo: replace with simpler function
        if (position != null && !field.isNoPartOfField(world, position) && !(world.getBlockState(position.above()).getBlock() instanceof CropBlock) && !(world.getBlockState(position.above()).getBlock() instanceof BlockScarecrow) && world.getBlockState(position).is(TFCMTags.Blocks.HOEABLE))
        {
            cir.setReturnValue(position);
        }
        else
        {
            cir.setReturnValue(null);
        }
    }

    /**
     * Checks whether the field meets climate requirements for target crop
     */
    @Inject(method = "canGoPlanting", at = @At(value = "INVOKE", target = "Lcom/minecolonies/api/colony/ICitizenData;setIdleAtJob(Z)V", ordinal = 1), cancellable = true)
    private void canGoPlantingInjector(ScarecrowTileEntity currentField, BuildingFarmer buildingFarmer, CallbackInfoReturnable<IAIState> cir)
    {
        final BlockPos fieldPos = currentField.getPosition();
        final Component cropName = currentField.getSeed().getDisplayName(); // this is null-checked in the target method
        if (!checkField(fieldPos, (farmlandPos, climate, world) -> checkClimate(climate, farmlandPos, fieldPos, cropName)))
        {
            final FarmerFieldModule module = buildingFarmer.getFirstModuleOccurance(FarmerFieldModule.class);
            module.setCurrentField(null);
            worker.getCitizenData().setIdleAtJob(true);
            cir.setReturnValue(AIWorkerState.PREPARING);
        }
    }

    /**
     * By default, minecolonies uses several hardcoded values in this check. This checks against
     * TFC criteria including climate, correct block types, and whether crops have space to grow.
     */
    @Inject(method = "findPlantableSurface", at = @At("HEAD"), cancellable = true)
    private void findPlantableSurfaceInjector(BlockPos position, ScarecrowTileEntity field, CallbackInfoReturnable<BlockPos> cir)
    {
        position = getSurfacePos(position);
        if (position != null && !field.isNoPartOfField(world, position) && world.getBlockState(position).getBlock() instanceof FarmlandBlock)
        {
            CropBlock crop = TFCMCropUtil.getCrop(field);

            if (crop instanceof SpreadingCropBlock)
            {
                // spreading crops need adjacent blocks to also be empty
                if (world.isEmptyBlock(position.above())
                    && world.isEmptyBlock(position.north().above())
                    && world.isEmptyBlock(position.south().above())
                    && world.isEmptyBlock(position.east().above())
                    && world.isEmptyBlock(position.west().above()))
                {
                    cir.setReturnValue(position);
                    return;
                }
            }
            else if (crop instanceof DoubleCropBlock)
            {
                // double crops need the block above them to also be empty
                if (world.isEmptyBlock(position.above())
                    && world.isEmptyBlock(position.above().above()))
                {
                    cir.setReturnValue(position);
                    return;
                }
            }
            else if (crop instanceof FloodedCropBlock)
            {
                // flooded crops can only grow in water
                if (world.getBlockState(position.above()).is(Blocks.WATER))
                {
                    cir.setReturnValue(position);
                    return;
                }
            }
            // regular crops don't need any special requirements
            else if (crop != null && world.isEmptyBlock(position.above()))
            {
                // allow other crops to be grown
                cir.setReturnValue(position);
                return;
            }
        }
        cir.setReturnValue(null);
    }

    /**
     * Uses the new findHarvestableSurface(BlockPos, bool) method. This enables the soil
     * amendment mechanic.
     */
    @ModifyVariable(method = "harvestIfAble", at = @At("STORE"), argsOnly = true)
    private BlockPos harvestIfAbleInjector(BlockPos position)
    {
        return findHarvestableSurface(position, true);
    }

    /**
     * Removes side effects from method...
     */
    @Inject(method = "findHarvestableSurface", at = @At("HEAD"), cancellable = true)
    private void findHarvestableSurfaceInjector(BlockPos position, CallbackInfoReturnable<BlockPos> cir)
    {
        cir.setReturnValue(findHarvestableSurface(position, false));
    }

    /**
     * Replaces normal findHarvestableSurface(BlockPos) with checks for correct TFC blocks.
     * It also replaces bone meal behavior with TFC's soil amendment mechanic.
     */
    private BlockPos findHarvestableSurface(BlockPos position, boolean amendSoil)
    {
        position = getSurfacePos(position);
        if (position == null)
        {
            return null;
        }
        BlockState state = world.getBlockState(position.above());
        Block block = state.getBlock();

        // pumpkins/melons, rotten & dead crops
        if (block instanceof DecayingBlock || block == TFCBlocks.ROTTEN_MELON.get() || block == TFCBlocks.ROTTEN_MELON.get() || block instanceof DeadCropBlock)
        {
            return position;
        }

        // normal crops
        if (block instanceof CropBlock crop)
        {
            // harvest fully grown crops (except spreading blocks like pumpkin stems)
            if (crop.isMaxAge(state) && !(crop instanceof SpreadingCropBlock))
            {
                return position;
            }

            // todo: place sticks for climbing crops

            // check if soil can be amended
            List<Item> validFertilizers = TFCMCropUtil.getFertilizers(crop, world.getBlockEntity(position, TFCBlockEntities.FARMLAND.get()).orElse(null));
            if (validFertilizers != null)
            {
                // only do the amending if flag is enabled
                // returns null so that the farmer doesn't try to harvest crop
                for (Item item : validFertilizers)
                {
                    final int slot = worker.getCitizenInventoryHandler().findFirstSlotInInventoryWith(item);
                    if (slot != -1)
                    {
                        if (amendSoil)
                        {
                            // use item
                            worker.getCitizenItemHandler().setMainHeldItem(slot);
                            FakePlayer fake = TFCMFakePlayerManager.setupFakePlayer(world, worker.blockPosition(), worker.getMainHandItem());
                            CropHelpers.useFertilizer(world, fake, InteractionHand.MAIN_HAND, position);
                            world.playSound(null, position, SoundEvents.BONE_MEAL_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
                            // return so more items aren't used
                            return null;
                        }
                        else return position;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Pumpkin & melon seeds are hardcoded, this also adds sound effects.
     */
    @Inject(method = "plantCrop", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;setBlockAndUpdate(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z"), locals = LocalCapture.CAPTURE_FAILEXCEPTION, cancellable = true)
    private void plantCropInjector(ItemStack item, BlockPos position, CallbackInfoReturnable<Boolean> cir, int slot, Item seed)
    {
        // check for neighboring spreading crops
        // todo - test without this? it probably will still work
        if (((BlockItem) seed).getBlock() instanceof SpreadingCropBlock
            && (world.getBlockState(position.north().above()).getBlock() instanceof SpreadingCropBlock
            || world.getBlockState(position.south().above()).getBlock() instanceof SpreadingCropBlock
            || world.getBlockState(position.east().above()).getBlock() instanceof SpreadingCropBlock
            || world.getBlockState(position.west().above()).getBlock() instanceof SpreadingCropBlock))
        {
            cir.setReturnValue(true);
            return;
        }
        // hold seed & play a sound
        worker.getCitizenItemHandler().setMainHeldItem(slot);
        world.playSound(null, position, SoundEvents.CROP_PLANTED, SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    /**
     * Adds pumpkin, melon and rotten versions to list of crop blocks.
     */
    @Inject(method = "getSurfacePos(Lnet/minecraft/core/BlockPos;Ljava/lang/Integer;)Lnet/minecraft/core/BlockPos;", at = @At("HEAD"), cancellable = true)
    private void getSurfacePosInjector(BlockPos position, Integer depth, CallbackInfoReturnable<BlockPos> cir)
    {
        if (Math.abs(depth) > MAX_DEPTH)
        {
            cir.setReturnValue(null);
            return;
        }
        final BlockState curBlockState = world.getBlockState(position);
        @Nullable final Block curBlock = curBlockState.getBlock();
        if ((curBlockState.getMaterial().isSolid() && !(curBlock instanceof DecayingBlock) && curBlock != TFCBlocks.ROTTEN_MELON.get() && curBlock != TFCBlocks.ROTTEN_PUMPKIN.get() && !(curBlock instanceof WebBlock)) || curBlockState.getMaterial().isLiquid())
        {
            if (depth < 0)
            {
                cir.setReturnValue(position);
                return;
            }
            cir.setReturnValue(getSurfacePos(position.above(), depth + 1));
        }
        else
        {
            if (depth > 0)
            {
                cir.setReturnValue(position.below());
                return;
            }
            cir.setReturnValue(getSurfacePos(position.below(), depth - 1));
        }
    }

    /**
     * Makes the farmer a little smarter: he will only work at the next valid block instead of trying to work at all of them...
     */
    @Inject(method = "workAtField", at = @At(value = "INVOKE", target = "Lcom/minecolonies/coremod/entity/ai/citizen/farmer/EntityAIWorkFarmer;nextValidCell(Lcom/minecolonies/api/tileentities/AbstractScarecrowTileEntity;)Lnet/minecraft/core/BlockPos;"), cancellable = true, locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private void workAtFieldInjector(CallbackInfoReturnable<IAIState> cir, BuildingFarmer buildingFarmer, FarmerFieldModule _module, BlockPos field, BlockEntity entity, ScarecrowTileEntity scarecrow)
    {
        // function used to check blocks
        Predicate<BlockPos> predicate = switch ((AIWorkerState) getState())
            {
                case FARMER_HOE -> (pos -> findHoeableSurface(pos, scarecrow) != null);
                case FARMER_PLANT -> (pos -> findPlantableSurface(pos, scarecrow) != null);
                case FARMER_HARVEST -> (pos -> findHarvestableSurface(pos) != null);
                default -> null;
            };

        if (predicate == null)
        {
            LOGGER.error("Invalid predicate! Current state: {}", getState());
            cir.setReturnValue(AIWorkerState.PREPARING);
            return;
        }

        // rather than using the next space, actually check if it needs any work
        findNextWorkableCell(scarecrow, predicate);
        if (workingOffset == null)
        {
            shouldDumpInventory = true;
            scarecrow.nextState();
            prevPos = null;
            cir.setReturnValue(AIWorkerState.IDLE);
            return;
        }
        cir.setReturnValue(getState());
    }

    /**
     * Similar to normal checkIfShouldExecute except it returns the block it finds.
     */
    private void findNextWorkableCell(@NotNull final ScarecrowTileEntity field, @NotNull final Predicate<BlockPos> predicate)
    {
        for (workingOffset = nextValidCell(field); workingOffset != null; workingOffset = nextValidCell(field))
        {
            BlockPos position = field.getPosition().below().south(workingOffset.getZ()).east(workingOffset.getX());
            if (predicate.test(position)) return;
        }
    }

    /**
     * Determines whether a crop should be planted or not, and displays a warning message
     *
     * @param climate     climate the field needs to be
     * @param farmlandPos position of farmland block
     * @param fieldPos    position of field
     * @return true if climate is valid
     */
    private boolean checkClimate(ClimateRange climate, BlockPos farmlandPos, BlockPos fieldPos, Component cropName)
    {
        final float currentTemp = Climate.getTemperature(world, farmlandPos);
        final int currentHydration = FarmlandBlock.getHydration(world, farmlandPos);

        // Display warnings for hydration levels
        switch (climate.checkHydration(currentHydration, false))
        {
            case LOW -> worker.getCitizenData().triggerInteraction(new PosBasedInteraction(
                new TranslatableComponent(TFCMTranslationConstants.CROP_TOO_DRY, cropName, fieldPos.getX(), fieldPos.getY(), fieldPos.getZ()),
                ChatPriority.BLOCKING,
                new TranslatableComponent(TFCMTranslationConstants.CROP_TOO_DRY),
                fieldPos));
            case HIGH -> worker.getCitizenData().triggerInteraction(new PosBasedInteraction(
                new TranslatableComponent(TFCMTranslationConstants.CROP_TOO_WET, cropName, fieldPos.getX(), fieldPos.getY(), fieldPos.getZ()),
                ChatPriority.BLOCKING,
                new TranslatableComponent(TFCMTranslationConstants.CROP_TOO_WET),
                fieldPos));
        }

        // Display warnings for temperature levels
        switch (climate.checkTemperature(currentTemp, false))
        {
            case LOW -> worker.getCitizenData().triggerInteraction(new PosBasedInteraction(
                new TranslatableComponent(TFCMTranslationConstants.CROP_TOO_COLD, cropName, fieldPos.getX(), fieldPos.getY(), fieldPos.getZ()),
                ChatPriority.BLOCKING,
                new TranslatableComponent(TFCMTranslationConstants.CROP_TOO_COLD),
                fieldPos));
            case HIGH -> worker.getCitizenData().triggerInteraction(new PosBasedInteraction(
                new TranslatableComponent(TFCMTranslationConstants.CROP_TOO_HOT, cropName, fieldPos.getX(), fieldPos.getY(), fieldPos.getZ()),
                ChatPriority.BLOCKING,
                new TranslatableComponent(TFCMTranslationConstants.CROP_TOO_HOT),
                fieldPos));
        }

        return climate.checkBoth(currentHydration, currentTemp, false);
    }
}
