package com.natrow.tfc_minecolonies.mixin;

import com.ldtteam.domumornamentum.entity.block.IMateriallyTexturedBlockEntity;
import com.ldtteam.structurize.blueprints.v1.BlueprintUtils;
import com.ldtteam.structurize.client.BlueprintBlockAccess;
import com.ldtteam.structurize.helpers.Settings;
import com.ldtteam.structurize.util.BlockInfo;
import com.natrow.tfc_minecolonies.structurize.ISettingsExtension;
import com.natrow.tfc_minecolonies.util.PlaceholderConversions;
import java.util.Locale;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(value = BlueprintUtils.class, remap = false)
public abstract class BlueprintUtilsMixin {
  @Inject(
      method = "constructTileEntity",
      at = @At(value = "RETURN", ordinal = 1),
      locals = LocalCapture.CAPTURE_FAILEXCEPTION)
  private static void constructTileEntityInjector(
      BlockInfo info,
      BlueprintBlockAccess blockAccess,
      CallbackInfoReturnable<BlockEntity> cir,
      String entityId,
      CompoundTag compound,
      BlockEntity entity) {
    final BlockState targetState = entity.getBlockState();
    if (Settings.instance.renderLightPlaceholders()) {
      final String woodType =
          ((ISettingsExtension) (Object) Settings.instance).getWoodType().toLowerCase(Locale.ROOT);
      final String stoneType =
          ((ISettingsExtension) (Object) Settings.instance).getRockType().toLowerCase(Locale.ROOT);
      final String soilType =
          ((ISettingsExtension) (Object) Settings.instance).getSoilType().toLowerCase(Locale.ROOT);

      if (entity instanceof IMateriallyTexturedBlockEntity doEntity) {
        Map<ResourceLocation, Block> data = doEntity.getTextureData().getTexturedComponents();
        data.entrySet()
            .forEach(
                e ->
                    e.setValue(
                        PlaceholderConversions.convertPlaceholder(
                            e.getValue(), woodType, stoneType, soilType)));
      } else {
        final Block targetBlock =
            PlaceholderConversions.convertPlaceholder(
                targetState.getBlock(), woodType, stoneType, soilType);
        entity.setBlockState(targetBlock.withPropertiesOf(targetState));
      }
    }
  }
}
