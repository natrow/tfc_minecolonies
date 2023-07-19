package com.natrow.tfc_minecolonies.mixin;

import java.util.Locale;
import com.ldtteam.structurize.client.BlueprintRenderer;
import com.ldtteam.structurize.helpers.Settings;
import com.natrow.tfc_minecolonies.TFCMConstants;
import com.natrow.tfc_minecolonies.structurize.ISettingsExtension;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = BlueprintRenderer.class, remap = false)
public abstract class BlueprintRendererMixin implements AutoCloseable
{
    // Override BlockState of placeholder blocks
    @ModifyVariable(method = "init", name = "state", at = @At("STORE"))
    private BlockState initInjector(BlockState value)
    {
        // Safe to access settings instances within this function
        if (Settings.instance.renderLightPlaceholders() && TFCMConstants.PLACEHOLDER_TO_WOOD.get().containsKey(value.getBlock()))
        {
            final String woodType = ((ISettingsExtension) (Object) Settings.instance).getWoodType().toLowerCase(Locale.ROOT);
            final Block targetBlock = TFCMConstants.PLACEHOLDER_TO_WOOD.get().get(value.getBlock()).get(woodType);
            return targetBlock.withPropertiesOf(value);
        }
        else
        {
            return value;
        }
    }
}
