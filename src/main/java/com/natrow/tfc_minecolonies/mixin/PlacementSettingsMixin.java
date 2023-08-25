package com.natrow.tfc_minecolonies.mixin;

import com.ldtteam.structurize.util.PlacementSettings;
import com.natrow.tfc_minecolonies.structurize.ISettingsExtension;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = PlacementSettings.class, remap = false)
public class PlacementSettingsMixin implements ISettingsExtension {
  private String woodType = "";
  private String rockType = "";
  private String soilType = "";

  public String getWoodType() {
    return woodType;
  }

  @Override
  public void setWoodType(String woodType) {
    this.woodType = woodType;
  }

  @Override
  public String getRockType() {
    return rockType;
  }

  @Override
  public void setRockType(String rockType) {
    this.rockType = rockType;
  }

  @Override
  public String getSoilType() {
    return soilType;
  }

  @Override
  public void setSoilType(String soilType) {
    this.soilType = soilType;
  }
}
