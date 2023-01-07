package com.natrow.tfc_minecolonies.mixin.AI;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import com.google.common.reflect.TypeToken;
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
import com.minecolonies.coremod.colony.jobs.JobFarmer;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAICrafting;
import com.minecolonies.coremod.entity.ai.citizen.farmer.EntityAIWorkFarmer;
import com.minecolonies.coremod.tileentities.ScarecrowTileEntity;
import com.mojang.logging.LogUtils;
import com.natrow.tfc_minecolonies.TFCMinecoloniesConstants;
import com.natrow.tfc_minecolonies.TFCMinecoloniesTags;
import com.natrow.tfc_minecolonies.minecolonies.TFCMinecoloniesFakePlayerManager;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WebBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.FakePlayer;
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

import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.blockentities.FarmlandBlockEntity;
import net.dries007.tfc.common.blockentities.TFCBlockEntities;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.crop.Crop;
import net.dries007.tfc.common.blocks.crop.CropBlock;
import net.dries007.tfc.common.blocks.crop.CropHelpers;
import net.dries007.tfc.common.blocks.crop.DeadCropBlock;
import net.dries007.tfc.common.blocks.crop.DecayingBlock;
import net.dries007.tfc.common.items.TFCItems;
import net.dries007.tfc.util.Fertilizer;

@Mixin(value = EntityAIWorkFarmer.class, remap = false)
public abstract class EntityAIWorkFarmerMixin extends AbstractEntityAICrafting<JobFarmer, BuildingFarmer>
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
        Predicate<ItemStack> findFertilizers = isFertilizer(field);
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
                final List<ItemStack> fertilizerItems = getFertilizers(field);
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
        world.setBlockAndUpdate(position, TFCMinecoloniesConstants.SOIL_TO_FARMLAND.get().getOrDefault(world.getBlockState(position).getBlock(), Blocks.FARMLAND).defaultBlockState());
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
        if (position != null && !field.isNoPartOfField(world, position) && !(world.getBlockState(position.above()).getBlock() instanceof CropBlock) && !(world.getBlockState(position.above()).getBlock() instanceof BlockScarecrow) && world.getBlockState(position).is(TFCMinecoloniesTags.Blocks.HOEABLE))
        {
            cir.setReturnValue(position);
        }
        else
        {
            cir.setReturnValue(null);
        }
    }

    /**
     * By default, minecolonies only checks for vanilla-compatible farmland. I've changed
     * this to use TFC-compatible farmland blocks ONLY.
     */
    @Inject(method = "findPlantableSurface", at = @At("HEAD"), cancellable = true)
    private void findPlantableSurfaceInjector(BlockPos position, ScarecrowTileEntity field, CallbackInfoReturnable<BlockPos> cir)
    {
        position = getSurfacePos(position);
        if (position != null && !field.isNoPartOfField(world, position) && !(world.getBlockState(position.above()).getBlock() instanceof CropBlock) && !(world.getBlockState(position.above()).getBlock() instanceof BlockScarecrow) && world.getBlockState(position).is(TFCTags.Blocks.FARMLAND))
        {
            cir.setReturnValue(position);
        }
        else
        {
            cir.setReturnValue(null);
        }
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
            // Crop.isMaxAge() doesn't work on TFC crops - potential bug
            if (crop.isMaxAge(state))
            {
                return position;
            }

            // check if soil can be amended
            List<Item> validFertilizers = getFertilizers(crop, world.getBlockEntity(position, TFCBlockEntities.FARMLAND.get()).orElse(null));
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
                            FakePlayer fake = TFCMinecoloniesFakePlayerManager.setupFakePlayer(world, worker.blockPosition(), worker.getMainHandItem());
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
        // normal check only looks at vanilla seeds, TFC has their own version of pumpkins & melons
        if ((seed == TFCItems.CROP_SEEDS.get(Crop.PUMPKIN).get() || seed == TFCItems.CROP_SEEDS.get(Crop.MELON).get()) && prevPos != null && !world.isEmptyBlock(prevPos.above()))
        {
            cir.setReturnValue(true);
            return;
        }
        // play a little sound
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
     * Helper method to get requested nutrient type from an item
     */
    private float getNutrient(Item item, FarmlandBlockEntity.NutrientType nutrient)
    {
        Fertilizer fertilizer = Fertilizer.get(new ItemStack(item, 1));
        return fertilizer == null ? 0.0F : getNutrient(fertilizer, nutrient);
    }

    /**
     * Helper method to get nutrient type from a fertilizer definition
     */
    private float getNutrient(Fertilizer fertilizer, FarmlandBlockEntity.NutrientType nutrient)
    {
        return switch (nutrient)
            {
                case NITROGEN -> fertilizer.getNitrogen();
                case PHOSPHOROUS -> fertilizer.getPhosphorus();
                case POTASSIUM -> fertilizer.getPotassium();
            };
    }

    /**
     * Attempt to get a crop using a field's seed slot
     */
    private CropBlock getCrop(ScarecrowTileEntity field)
    {
        ItemStack seed = field.getSeed();
        if (seed != null)
        {
            Block cropBlock = ((BlockItem) seed.getItem()).getBlock();
            if (cropBlock instanceof CropBlock crop) return crop;
        }
        return null;
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
     * Determines whether an item stack is valid fertilizer for a given field.
     */
    private Predicate<ItemStack> isFertilizer(ScarecrowTileEntity field)
    {
        CropBlock crop = getCrop(field);
        if (crop == null) return null;
        FarmlandBlockEntity.NutrientType nutrient = crop.getPrimaryNutrient();
        return itemStack -> {
            Fertilizer fertilizer = Fertilizer.get(itemStack);
            if (fertilizer == null) return false;
            else return getNutrient(fertilizer, nutrient) > 0.0F;
        };
    }

    /**
     * Finds all valid fertilizer items for a given field
     */
    private List<ItemStack> getFertilizers(ScarecrowTileEntity field)
    {
        CropBlock crop = getCrop(field);
        if (crop == null) return null;
        else return getFertilizers(crop, null).stream().map(ItemStack::new).toList();
    }

    /**
     * Retrieves all valid fertilizer items for the given crop, without waste
     */
    private List<Item> getFertilizers(CropBlock crop, @Nullable FarmlandBlockEntity farmland)
    {
        FarmlandBlockEntity.NutrientType nutrient = crop.getPrimaryNutrient();
        float currentNutrient = farmland == null ? 0.0F : farmland.getNutrient(nutrient);

        return Fertilizer.MANAGER
            .getValues()
            .stream()
            .filter(fertilizer -> {
                float newNutrient = getNutrient(fertilizer, nutrient);
                return newNutrient > 0.0F && newNutrient + currentNutrient <= 1.0F;
            })
            .flatMap(fertilizer -> fertilizer.getValidItems().stream())
            .sorted((item1, item2) -> Float.compare(getNutrient(item1, nutrient), getNutrient(item2, nutrient)))
            .toList();
    }
}
