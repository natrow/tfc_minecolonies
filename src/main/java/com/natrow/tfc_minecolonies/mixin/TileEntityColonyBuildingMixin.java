package com.natrow.tfc_minecolonies.mixin;

import com.minecolonies.api.tileentities.AbstractTileEntityColonyBuilding;
import com.minecolonies.api.tileentities.TileEntityColonyBuilding;
import com.natrow.tfc_minecolonies.minecolonies.ITileEntityColonyBuildingExtension;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = TileEntityColonyBuilding.class, remap = false)
public abstract class TileEntityColonyBuildingMixin extends AbstractTileEntityColonyBuilding implements ITileEntityColonyBuildingExtension
{

    /**
     * Dummy constructor
     */
    public TileEntityColonyBuildingMixin(BlockEntityType<? extends AbstractTileEntityColonyBuilding> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    private static final String TAG_WOOD_TYPE = "wood_type";
    private static final String TAG_STONE_TYPE = "stone_type";
    private static final String TAG_SOIL_TYPE = "soil_type";

    private String woodType = "";
    private String stoneType = "";
    private String soilType = "";

    @Inject(method = "load", at = @At("TAIL"))
    private void loadInjector(CompoundTag compound, CallbackInfo ci)
    {
        woodType = compound.getString(TAG_WOOD_TYPE);
        stoneType = compound.getString(TAG_STONE_TYPE);
        soilType = compound.getString(TAG_SOIL_TYPE);
    }

    @Inject(method = "saveAdditional", at = @At("TAIL"))
    private void saveAdditionalInjector(CompoundTag compound, CallbackInfo ci)
    {
        compound.putString(TAG_WOOD_TYPE, woodType);
        compound.putString(TAG_STONE_TYPE, stoneType);
        compound.putString(TAG_SOIL_TYPE, soilType);
    }

    @Override
    public String getWoodType()
    {
        return woodType;
    }

    @Override
    public void setWoodType(String woodType)
    {
        this.woodType = woodType;
    }

    @Override
    public String getStoneType()
    {
        return stoneType;
    }

    @Override
    public void setStoneType(String stoneType)
    {
        this.stoneType = stoneType;
    }

    @Override
    public String getSoilType()
    {
        return soilType;
    }

    @Override
    public void setSoilType(String soilType)
    {
        this.soilType = soilType;
    }
}
