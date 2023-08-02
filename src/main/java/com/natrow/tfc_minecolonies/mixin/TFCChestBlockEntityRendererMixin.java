package com.natrow.tfc_minecolonies.mixin;

import com.natrow.tfc_minecolonies.TFCMConstants;
import com.natrow.tfc_minecolonies.block.TFCMBlocks;
import java.util.stream.Stream;
import net.dries007.tfc.client.render.blockentity.TFCChestBlockEntityRenderer;
import net.dries007.tfc.common.blockentities.TFCChestBlockEntity;
import net.dries007.tfc.common.blocks.wood.Wood;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.client.resources.model.Material;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.ChestType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = TFCChestBlockEntityRenderer.class, remap = false)
public abstract class TFCChestBlockEntityRendererMixin extends ChestRenderer<TFCChestBlockEntity> {
  @Shadow
  private static String getFolder(BlockEntity blockEntity, ChestType type) {
    return null;
  }

  @Shadow private String wood;

  /** Dummy constructor */
  public TFCChestBlockEntityRendererMixin(BlockEntityRendererProvider.Context p_173607_) {
    super(p_173607_);
  }

  /** Load TFCM chest textures from tfc_minecolonies namespace */
  @Inject(
      method =
          "getMaterial(Lnet/dries007/tfc/common/blockentities/TFCChestBlockEntity;Lnet/minecraft/world/level/block/state/properties/ChestType;)Lnet/minecraft/client/resources/model/Material;",
      at = @At("HEAD"),
      cancellable = true)
  private void getMaterialInjector(
      TFCChestBlockEntity blockEntity, ChestType chestType, CallbackInfoReturnable<Material> cir) {
    Stream.of(Wood.BlockType.CHEST, Wood.BlockType.TRAPPED_CHEST)
        .map(type -> TFCMBlocks.PLACEHOLDER_WOODS.get(type).get())
        .forEach(
            block -> {
              if (blockEntity.getBlockState().getBlock().equals(block)) {
                cir.setReturnValue(
                    new Material(
                        Sheets.CHEST_SHEET,
                        TFCMConstants.getResourceLocation(
                            "entity/chest/" + getFolder(blockEntity, chestType) + "/" + wood)));
              }
            });
  }
}
