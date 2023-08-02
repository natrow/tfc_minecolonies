package com.natrow.tfc_minecolonies.mixin;

import com.ldtteam.structurize.api.util.ItemStackUtils;
import com.ldtteam.structurize.api.util.Log;
import com.ldtteam.structurize.blocks.ModBlocks;
import com.ldtteam.structurize.placement.AbstractBlueprintIterator;
import com.ldtteam.structurize.placement.BlockPlacementResult;
import com.ldtteam.structurize.placement.StructurePlacer;
import com.ldtteam.structurize.placement.handlers.placement.IPlacementHandler;
import com.ldtteam.structurize.placement.handlers.placement.PlacementHandlers;
import com.ldtteam.structurize.placement.structure.IStructureHandler;
import com.ldtteam.structurize.util.BlockUtils;
import com.ldtteam.structurize.util.ChangeStorage;
import com.ldtteam.structurize.util.InventoryUtils;
import com.natrow.tfc_minecolonies.structurize.TFCMPlacementHandlers;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = StructurePlacer.class, remap = false)
public class StructurePlacerMixin {
  @Final @Shadow protected IStructureHandler handler;
  @Final @Shadow protected AbstractBlueprintIterator iterator;

  @Inject(method = "handleBlockPlacement", at = @At("HEAD"), cancellable = true)
  private void handleBlockPlacementInjector(
      Level world,
      BlockPos worldPos,
      BlockPos localPos,
      ChangeStorage storage,
      BlockState localState,
      CompoundTag tileEntityData,
      CallbackInfoReturnable<BlockPlacementResult> cir) {
    final BlockState worldState = world.getBlockState(worldPos);
    boolean sameBlockInWorld = worldState.getBlock() == localState.getBlock();

    if (!(worldState.getBlock() instanceof AirBlock)) {
      if (!handler.allowReplace()) {
        cir.setReturnValue(
            new BlockPlacementResult(worldPos, BlockPlacementResult.Result.BREAK_BLOCK));
        return;
      }
    }

    for (final CompoundTag compound :
        this.iterator.getBluePrintPositionInfo(localPos).getEntities()) {
      if (compound != null) {
        try {
          final BlockPos pos =
              this.handler.getWorldPos().subtract(handler.getBluePrint().getPrimaryBlockOffset());

          final Optional<EntityType<?>> type = EntityType.by(compound);
          if (type.isPresent()) {
            final Entity entity = type.get().create(world);
            if (entity != null) {
              entity.deserializeNBT(compound);

              entity.setUUID(UUID.randomUUID());
              final Vec3 posInWorld = entity.position().add(pos.getX(), pos.getY(), pos.getZ());
              entity.moveTo(
                  posInWorld.x, posInWorld.y, posInWorld.z, entity.getYRot(), entity.getXRot());

              final List<? extends Entity> list =
                  world.getEntitiesOfClass(
                      entity.getClass(),
                      new AABB(posInWorld.add(1, 1, 1), posInWorld.add(-1, -1, -1)));
              boolean foundEntity = false;
              for (Entity worldEntity : list) {
                if (worldEntity.position().equals(posInWorld)) {
                  foundEntity = true;
                  break;
                }
              }

              if (foundEntity || (entity instanceof Mob && !handler.isCreative())) {
                continue;
              }

              final List<ItemStack> requiredItems = new ArrayList<>();
              if (!handler.isCreative()) {
                requiredItems.addAll(ItemStackUtils.getListOfStackForEntity(entity, pos));
                if (!InventoryUtils.hasRequiredItems(handler.getInventory(), requiredItems)) {
                  cir.setReturnValue(
                      new BlockPlacementResult(
                          worldPos, BlockPlacementResult.Result.MISSING_ITEMS, requiredItems));
                  return;
                }
              }

              world.addFreshEntity(entity);
              if (storage != null) {
                storage.addToBeKilledEntity(entity);
              }

              for (final ItemStack tempStack : requiredItems) {
                if (!ItemStackUtils.isEmpty(tempStack)) {
                  InventoryUtils.consumeStack(tempStack, handler.getInventory());
                }
              }
              this.handler.triggerEntitySuccess(localPos, requiredItems, true);
            }
          }
        } catch (final RuntimeException e) {
          Log.getLogger().info("Couldn't restore entity", e);
        }
      }
    }

    BlockEntity worldEntity = null;
    if (tileEntityData != null) {
      worldEntity = world.getBlockEntity(worldPos);
    }

    if (BlockUtils.areBlockStatesEqual(
        localState,
        worldState,
        handler::replaceWithSolidBlock,
        handler.fancyPlacement(),
        handler::shouldBlocksBeConsideredEqual,
        tileEntityData,
        worldEntity)) {
      cir.setReturnValue(new BlockPlacementResult(worldPos, BlockPlacementResult.Result.SUCCESS));
      return;
    }

    if (localState.getBlock() == ModBlocks.blockSolidSubstitution.get()
        && handler.fancyPlacement()) {
      localState =
          this.handler.getSolidBlockForPos(
              worldPos,
              localPos.getY() + 1 < handler.getBluePrint().getSizeY()
                  ? handler.getBluePrint().getBlockState(localPos.above())
                  : null);
    }
    if (localState.getBlock() == ModBlocks.blockTagSubstitution.get() && handler.fancyPlacement()) {
      localState = Blocks.AIR.defaultBlockState();
    }

    for (final IPlacementHandler placementHandler : PlacementHandlers.handlers) {
      if (placementHandler.canHandle(world, worldPos, localState)) {
        final List<ItemStack> requiredItems = new ArrayList<>();

        if (!sameBlockInWorld && !this.handler.isCreative()) {
          if (placementHandler
              instanceof TFCMPlacementHandlers.TFCMPlaceholderHandler tfcmPlaceholderHandler) {
            for (final ItemStack stack :
                tfcmPlaceholderHandler.getRequiredItemsWithCtx(
                    world, worldPos, localState, tileEntityData, handler.getSettings())) {
              if (!stack.isEmpty() && !this.handler.isStackFree(stack)) {
                requiredItems.add(stack);
              }
            }
          } else {
            for (final ItemStack stack :
                placementHandler.getRequiredItems(
                    world, worldPos, localState, tileEntityData, false)) {
              if (!stack.isEmpty() && !this.handler.isStackFree(stack)) {
                requiredItems.add(stack);
              }
            }
          }

          if (!this.handler.hasRequiredItems(requiredItems)) {
            cir.setReturnValue(
                new BlockPlacementResult(
                    worldPos, BlockPlacementResult.Result.MISSING_ITEMS, requiredItems));
            return;
          }
        }

        if (!(worldState.getBlock() instanceof AirBlock)) {
          if (!sameBlockInWorld
              && worldState.getMaterial() != Material.AIR
              && !(worldState.getBlock() instanceof DoublePlantBlock
                  && worldState.getValue(DoublePlantBlock.HALF).equals(DoubleBlockHalf.UPPER))) {
            placementHandler.handleRemoval(handler, world, worldPos, tileEntityData);
          }
        }

        this.handler.prePlacementLogic(worldPos, localState, requiredItems);

        final IPlacementHandler.ActionProcessingResult result =
            placementHandler.handle(
                world,
                worldPos,
                localState,
                tileEntityData,
                !this.handler.fancyPlacement(),
                this.handler.getWorldPos(),
                this.handler.getSettings());
        if (result == IPlacementHandler.ActionProcessingResult.DENY) {
          placementHandler.handleRemoval(handler, world, worldPos, tileEntityData);
          cir.setReturnValue(new BlockPlacementResult(worldPos, BlockPlacementResult.Result.FAIL));
          return;
        }

        this.handler.triggerSuccess(localPos, requiredItems, true);

        if (result == IPlacementHandler.ActionProcessingResult.PASS) {
          cir.setReturnValue(
              new BlockPlacementResult(worldPos, BlockPlacementResult.Result.SUCCESS));
        }

        if (!this.handler.isCreative() && !sameBlockInWorld) {
          for (final ItemStack tempStack : requiredItems) {
            if (!ItemStackUtils.isEmpty(tempStack)) {
              InventoryUtils.consumeStack(tempStack, handler.getInventory());
            }
          }
        }

        cir.setReturnValue(new BlockPlacementResult(worldPos, BlockPlacementResult.Result.SUCCESS));
      }
    }
    cir.setReturnValue(new BlockPlacementResult(worldPos, BlockPlacementResult.Result.FAIL));
  }

  @Inject(method = "getResourceRequirements", at = @At("HEAD"), cancellable = true)
  private void getResourceRequirementsInjector(
      Level world,
      BlockPos worldPos,
      BlockPos localPos,
      BlockState localState,
      CompoundTag tileEntityData,
      CallbackInfoReturnable<BlockPlacementResult> cir) {

    final BlockState worldState = world.getBlockState(worldPos);
    boolean sameBlockInWorld = worldState.getBlock() == localState.getBlock();

    final List<ItemStack> requiredItems = new ArrayList<>();
    for (final CompoundTag compound : iterator.getBluePrintPositionInfo(localPos).getEntities()) {
      if (compound != null) {
        try {
          final BlockPos pos =
              this.handler.getWorldPos().subtract(handler.getBluePrint().getPrimaryBlockOffset());

          final Optional<EntityType<?>> type = EntityType.by(compound);
          if (type.isPresent()) {
            final Entity entity = type.get().create(world);
            if (entity != null) {
              entity.deserializeNBT(compound);

              final Vec3 posInWorld = entity.position().add(pos.getX(), pos.getY(), pos.getZ());
              final List<? extends Entity> list =
                  world.getEntitiesOfClass(
                      entity.getClass(),
                      new AABB(posInWorld.add(1, 1, 1), posInWorld.add(-1, -1, -1)));
              boolean foundEntity = false;
              for (Entity worldEntity : list) {
                if (worldEntity.position().equals(posInWorld)) {
                  foundEntity = true;
                  break;
                }
              }

              if (foundEntity) {
                continue;
              }

              requiredItems.addAll(ItemStackUtils.getListOfStackForEntity(entity, pos));
            }
          }
        } catch (final RuntimeException e) {
          Log.getLogger().info("Couldn't restore entity", e);
        }
      }
    }

    if (localState.getBlock() == ModBlocks.blockSolidSubstitution.get()
        && handler.fancyPlacement()) {
      localState =
          this.handler.getSolidBlockForPos(
              worldPos,
              localPos.getY() + 1 < handler.getBluePrint().getSizeY()
                  ? handler.getBluePrint().getBlockState(localPos.above())
                  : null);
    }
    if (localState.getBlock() == ModBlocks.blockTagSubstitution.get() && handler.fancyPlacement()) {
      localState = Blocks.AIR.defaultBlockState();
    }

    for (final IPlacementHandler placementHandler : PlacementHandlers.handlers) {
      if (placementHandler.canHandle(world, worldPos, localState)) {
        if (!sameBlockInWorld) {
          if (placementHandler
              instanceof TFCMPlacementHandlers.TFCMPlaceholderHandler tfcmPlaceholderHandler) {
            for (final ItemStack stack :
                tfcmPlaceholderHandler.getRequiredItemsWithCtx(
                    world, worldPos, localState, tileEntityData, handler.getSettings())) {
              if (!stack.isEmpty() && !this.handler.isStackFree(stack)) {
                requiredItems.add(stack);
              }
            }
          } else {
            for (final ItemStack stack :
                placementHandler.getRequiredItems(
                    world, worldPos, localState, tileEntityData, false)) {
              if (!stack.isEmpty() && !this.handler.isStackFree(stack)) {
                requiredItems.add(stack);
              }
            }
          }
        }
        cir.setReturnValue(
            new BlockPlacementResult(
                worldPos, BlockPlacementResult.Result.MISSING_ITEMS, requiredItems));
        return;
      }
    }
    cir.setReturnValue(
        new BlockPlacementResult(
            worldPos, BlockPlacementResult.Result.MISSING_ITEMS, requiredItems));
  }
}
