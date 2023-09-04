package com.natrow.tfc_minecolonies.mixin;

import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.helpers.Settings;
import com.ldtteam.structurize.placement.structure.CreativeStructureHandler;
import com.ldtteam.structurize.placement.structure.IStructureHandler;
import com.ldtteam.structurize.util.BlockUtils;
import com.ldtteam.structurize.util.PlacementSettings;
import com.natrow.tfc_minecolonies.structurize.ISettingsExtension;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(value = Settings.class, remap = false)
public abstract class SettingsMixin implements ISettingsExtension {
  @Unique
  private String tfc_minecolonies$woodType = "";
  @Unique
  private String tfc_minecolonies$rockType = "";
  @Unique
  private String tfc_minecolonies$soilType = "";
  @Shadow private Blueprint blueprint;
  @Shadow private String structureName;
  @Shadow private BlockPos pos;

  @Shadow
  public abstract void scheduleRefresh();

  @Override
  public String getWoodType() {
    return tfc_minecolonies$woodType;
  }

  @Override
  public void setWoodType(String woodType) {
    this.tfc_minecolonies$woodType = woodType;
    scheduleRefresh();
  }

  @Override
  public String getRockType() {
    return tfc_minecolonies$rockType;
  }

  @Override
  public void setRockType(String rockType) {
    this.tfc_minecolonies$rockType = rockType;
    scheduleRefresh();
  }

  @Override
  public String getSoilType() {
    return tfc_minecolonies$soilType;
  }

  @Override
  public void setSoilType(String soilType) {
    this.tfc_minecolonies$soilType = soilType;
    scheduleRefresh();
  }

  @Inject(method = "deserializeNBT(Lnet/minecraft/nbt/CompoundTag;)V", at = @At("HEAD"))
  private void deserializeNBTInjector(CompoundTag nbt, CallbackInfo ci) {
    if (nbt.contains("wood_type")) {
      tfc_minecolonies$woodType = nbt.getString("wood_type");
    } else {
      tfc_minecolonies$woodType = null;
    }
    if (nbt.contains("rock_type")) {
      tfc_minecolonies$rockType = nbt.getString("rock_type");
    } else {
      tfc_minecolonies$rockType = null;
    }
    if (nbt.contains("soil_type")) {
      tfc_minecolonies$soilType = nbt.getString("soil_type");
    } else {
      tfc_minecolonies$soilType = null;
    }
  }

  @Inject(
      method = "serializeNBT()Lnet/minecraft/nbt/CompoundTag;",
      at = @At("RETURN"),
      locals = LocalCapture.CAPTURE_FAILEXCEPTION)
  private void serializeNBTInjector(CallbackInfoReturnable<CompoundTag> cir, CompoundTag nbt) {
    if (tfc_minecolonies$woodType != null) {
      nbt.putString("wood_type", tfc_minecolonies$woodType);
    }
    if (tfc_minecolonies$rockType != null) {
      nbt.putString("rock_type", tfc_minecolonies$rockType);
    }
    if (tfc_minecolonies$soilType != null) {
      nbt.putString("soil_type", tfc_minecolonies$soilType);
    }
  }
}
