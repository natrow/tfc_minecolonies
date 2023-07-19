package com.natrow.tfc_minecolonies.mixin;

import java.util.Locale;
import com.ldtteam.structurize.blueprints.v1.BlueprintUtils;
import com.ldtteam.structurize.client.BlueprintBlockAccess;
import com.ldtteam.structurize.helpers.Settings;
import com.ldtteam.structurize.util.BlockInfo;
import com.natrow.tfc_minecolonies.TFCMConstants;
import com.natrow.tfc_minecolonies.structurize.ISettingsExtension;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(value = BlueprintUtils.class, remap = false)
public abstract class BlueprintUtilsMixin
{
    @Inject(method = "constructTileEntity", at = @At(value = "RETURN", ordinal = 1), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private static void constructTileEntityInjector(BlockInfo info, BlueprintBlockAccess blockAccess, CallbackInfoReturnable<BlockEntity> cir, String entityId, CompoundTag compound, BlockEntity entity)
    {
        final BlockState value = entity.getBlockState();
        if (Settings.instance.renderLightPlaceholders() && TFCMConstants.PLACEHOLDER_TO_WOOD.get().containsKey(value.getBlock()))
        {
            final String woodType = ((ISettingsExtension) (Object) Settings.instance).getWoodType().toLowerCase(Locale.ROOT);
            final Block targetBlock = TFCMConstants.PLACEHOLDER_TO_WOOD.get().get(value.getBlock()).get(woodType);
            entity.setBlockState(targetBlock.withPropertiesOf(value));
        }
    }
}
