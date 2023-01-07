package com.natrow.tfc_minecolonies.mixin.AI;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.tileentities.AbstractScarecrowTileEntity;
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
            List<Item> validFertilizers = getValidFertilizers(position);
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
                return null;
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
     * Retrieves all valid fertilizer items for the given block.
     *
     * @param position Position of TFC crop block.
     * @return A list of valid fertilizers, or null if none exist.
     */
    private @Nullable List<Item> getValidFertilizers(BlockPos position)
    {
        BlockState state = world.getBlockState(position.above());
        Block block = state.getBlock();

        // normal crops
        if (block instanceof CropBlock crop)
        {
            Optional<FarmlandBlockEntity> farmland = world.getBlockEntity(position, TFCBlockEntities.FARMLAND.get());
            if (farmland.isPresent())
            {
                FarmlandBlockEntity.NutrientType nutrient = crop.getPrimaryNutrient();
                float currentNutrient = farmland.get().getNutrient(nutrient);
                // check if soil is already fertile
                if (currentNutrient > 9.0F) return null;
                List<Item> validFertilizers = new ArrayList<>();
                for (Fertilizer fertilizer : Fertilizer.MANAGER.getValues())
                {
                    float newNutrient = getNutrient(fertilizer, nutrient);
                    if (newNutrient != 0 && newNutrient + currentNutrient <= 1.0F)
                    {
                        validFertilizers.addAll(fertilizer.getValidItems());
                    }
                }
                // if any valid fertilizers exist, attempt to use them
                if (!validFertilizers.isEmpty())
                {
                    // try using the best fertilizers first (sorted in descending order)
                    validFertilizers.sort((item1, item2) -> Float.compare(getNutrient(new ItemStack(item2), nutrient), getNutrient(new ItemStack(item1), nutrient)));
                    return validFertilizers;
                }
            }
        }

        return null;
    }

    /**
     * Helper method to get requested nutrient type from an item
     */
    private float getNutrient(ItemStack item, FarmlandBlockEntity.NutrientType nutrient)
    {
        Fertilizer fertilizer = Fertilizer.get(item);
        if (fertilizer == null) return 0F;
        else return getNutrient(fertilizer, nutrient);
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
}
