package com.natrow.tfc_minecolonies.mixin;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.tileentities.AbstractTileEntityColonyBuilding;
import com.natrow.tfc_minecolonies.minecolonies.IAbstractBlockHutExtension;
import com.natrow.tfc_minecolonies.minecolonies.ITileEntityColonyBuildingExtension;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/***
 * The purpose of this mixin is to force building metadata to include the building materials
 */
@Mixin(value = AbstractBlockHut.class, remap = false)
public abstract class AbstractBlockHutMixin implements IAbstractBlockHutExtension
{
    @Shadow
    public abstract void setPlacedBy(@NotNull Level worldIn, @NotNull BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack);

    @Override
    public void onBlockPlacedByBuildTool(@NotNull final Level worldIn, @NotNull final BlockPos pos, final BlockState state, final LivingEntity placer, final ItemStack stack, final boolean mirror, final String style, final String woodType, final String rockType, final String soilType)
    {
        final BlockEntity tileEntity = worldIn.getBlockEntity(pos);

        if (tileEntity instanceof AbstractTileEntityColonyBuilding)
        {
            ((AbstractTileEntityColonyBuilding) tileEntity).setMirror(mirror);
            ((AbstractTileEntityColonyBuilding) tileEntity).setStyle(style);
            ((ITileEntityColonyBuildingExtension) tileEntity).setWoodType(woodType);
            ((ITileEntityColonyBuildingExtension) tileEntity).setRockType(rockType);
            ((ITileEntityColonyBuildingExtension) tileEntity).setSoilType(soilType);
        }

        setPlacedBy(worldIn, pos, state, placer, stack);
    }

    @Inject(method = "onBlockPlacedByBuildTool", at = @At("HEAD"))
    private void onBlockPlacedByBuildToolInjector(Level worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack, boolean mirror, String style, CallbackInfo ci)
    {
        throw new RuntimeException("Attempted to use onBlockPlacedByBuildTool without TFCM properties");
    }
}
