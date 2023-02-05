package com.natrow.tfc_minecolonies.minecolonies;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public interface IAbstractBlockHutExtension
{
    void onBlockPlacedByBuildTool(@NotNull final Level worldIn, @NotNull final BlockPos pos, final BlockState state, final LivingEntity placer, final ItemStack stack, final boolean mirror, final String style, final String woodType, final String stoneType, final String soilType);
}
