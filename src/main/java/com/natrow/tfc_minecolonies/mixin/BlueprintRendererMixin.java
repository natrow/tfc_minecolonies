package com.natrow.tfc_minecolonies.mixin;

import com.ldtteam.structurize.client.BlueprintRenderer;
import com.ldtteam.structurize.helpers.Settings;
import com.natrow.tfc_minecolonies.structurize.ISettingsExtension;
import com.natrow.tfc_minecolonies.util.PlaceholderConversions;
import java.util.Locale;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = BlueprintRenderer.class, remap = false)
public abstract class BlueprintRendererMixin implements AutoCloseable {
  // Override BlockState of placeholder blocks
  @ModifyVariable(method = "init", name = "state", at = @At("STORE"))
  private BlockState initInjector(BlockState value) {
    // Safe to access settings instances within this function
    if (Settings.instance.renderLightPlaceholders()) {
      final String woodType =
          ((ISettingsExtension) (Object) Settings.instance).getWoodType().toLowerCase(Locale.ROOT);
      final String stoneType =
          ((ISettingsExtension) (Object) Settings.instance).getRockType().toLowerCase(Locale.ROOT);
      final String soilType =
          ((ISettingsExtension) (Object) Settings.instance).getSoilType().toLowerCase(Locale.ROOT);

      final Block targetBlock =
          PlaceholderConversions.convertPlaceholder(
              value.getBlock(), woodType, stoneType, soilType);

      return targetBlock.withPropertiesOf(value);
    } else {
      return value;
    }
  }
}
