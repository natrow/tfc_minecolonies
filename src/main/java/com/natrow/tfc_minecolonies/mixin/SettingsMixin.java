package com.natrow.tfc_minecolonies.mixin;

import com.ldtteam.structurize.helpers.Settings;
import com.natrow.tfc_minecolonies.structurize.ISettingsExtension;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(value = Settings.class, remap = false)
public abstract class SettingsMixin implements ISettingsExtension
{
    private String woodType = "";
    private String stoneType = "";
    private String soilType = "";

    @Override
    public String getWoodType()
    {
        return woodType;
    }

    @Override
    public String getStoneType() {
        return stoneType;
    }

    @Override
    public String getSoilType()
    {
        return soilType;
    }

    @Override
    public void setWoodType(String woodType)
    {
        this.woodType = woodType;
    }

    @Override
    public void setStoneType(String stoneType)
    {
        this.stoneType = stoneType;
    }

    @Override
    public void setSoilType(String soilType)
    {
        this.soilType = soilType;
    }

    @Inject(method = "deserializeNBT(Lnet/minecraft/nbt/CompoundTag;)V", at = @At("HEAD"))
    private void deserializeNBTInjector(CompoundTag nbt, CallbackInfo ci)
    {
        if (nbt.contains("wood_type"))
        {
            woodType = nbt.getString("wood_type");
        }
        else
        {
            woodType = null;
        }
        if (nbt.contains("stone_type"))
        {
            stoneType = nbt.getString("stone_type");
        }
        else
        {
            stoneType = null;
        }
        if (nbt.contains("soil_type"))
        {
            soilType = nbt.getString("soil_type");
        }
        else
        {
            soilType = null;
        }
    }

    @Inject(method = "serializeNBT()Lnet/minecraft/nbt/CompoundTag;", at = @At("RETURN"), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private void serializeNBTInjector(CallbackInfoReturnable<CompoundTag> cir, CompoundTag nbt)
    {
        if (woodType != null)
        {
            nbt.putString("wood_type", woodType);
        }
        if (stoneType != null)
        {
            nbt.putString("stone_type", stoneType);
        }
        if (soilType != null)
        {
            nbt.putString("soil_type", soilType);
        }
    }
}
