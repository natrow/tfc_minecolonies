package com.natrow.tfc_minecolonies.mixin;

import com.google.common.collect.ImmutableMap;
import com.natrow.tfc_minecolonies.TFCMConstants;
import com.natrow.tfc_minecolonies.block.TFCMBlocks;
import com.natrow.tfc_minecolonies.block.TFCMWood;
import java.util.Map;
import java.util.stream.Stream;
import net.dries007.tfc.client.render.blockentity.TFCSignBlockEntityRenderer;
import net.dries007.tfc.common.blocks.wood.Wood;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(value = TFCSignBlockEntityRenderer.class, remap = false)
public class TFCSignBlockEntityRendererMixin extends SignRenderer {
  @Shadow
  private static Material createSignMaterial(String domain, String name) {
    return null;
  }

  @Mutable @Shadow @Final private Map<Block, Material> materials;

  @Mutable @Shadow @Final private Map<Block, SignModel> models;

  public TFCSignBlockEntityRendererMixin(BlockEntityRendererProvider.Context p_173636_) {
    super(p_173636_);
  }

  @Inject(
      method =
          "<init>(Lnet/minecraft/client/renderer/blockentity/BlockEntityRendererProvider$Context;Ljava/util/stream/Stream;)V",
      at = @At("RETURN"),
      locals = LocalCapture.CAPTURE_FAILEXCEPTION)
  private void constructorInjector(
      BlockEntityRendererProvider.Context context,
      Stream<TFCSignBlockEntityRenderer.SignModelData> blocks,
      CallbackInfo ci,
      ImmutableMap.Builder<Block, Material> materialBuilder,
      ImmutableMap.Builder<Block, SignModel> modelBuilder) {
    String domain = TFCMConstants.MOD_ID;
    String name = TFCMWood.PLACEHOLDER.getSerializedName();
    Block sign = TFCMBlocks.PLACEHOLDER_WOODS.get(Wood.BlockType.SIGN).get();
    Block wallSign = TFCMBlocks.PLACEHOLDER_WOODS.get(Wood.BlockType.WALL_SIGN).get();
    final Material material = createSignMaterial(domain, name);
    final SignModel model =
        new SignModel(
            context.bakeLayer(
                new ModelLayerLocation(new ResourceLocation(domain, "sign/" + name), "main")));

    materialBuilder.put(sign, material);
    materialBuilder.put(wallSign, material);
    modelBuilder.put(sign, model);
    modelBuilder.put(wallSign, model);

    materials = materialBuilder.build();
    models = modelBuilder.build();
  }
}
