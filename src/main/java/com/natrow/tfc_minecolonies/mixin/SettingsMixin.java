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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(value = Settings.class, remap = false)
public abstract class SettingsMixin implements ISettingsExtension {
  private String woodType = "";
  private String rockType = "";
  private String soilType = "";
  @Shadow private Blueprint blueprint;
  @Shadow private String structureName;
  @Shadow private BlockPos pos;

  @Shadow
  public abstract void scheduleRefresh();

  @Override
  public String getWoodType() {
    return woodType;
  }

  @Override
  public void setWoodType(String woodType) {
    this.woodType = woodType;
    scheduleRefresh();
  }

  @Override
  public String getRockType() {
    return rockType;
  }

  @Override
  public void setRockType(String rockType) {
    this.rockType = rockType;
    scheduleRefresh();
  }

  @Override
  public String getSoilType() {
    return soilType;
  }

  @Override
  public void setSoilType(String soilType) {
    this.soilType = soilType;
    scheduleRefresh();
  }

  @Inject(method = "deserializeNBT(Lnet/minecraft/nbt/CompoundTag;)V", at = @At("HEAD"))
  private void deserializeNBTInjector(CompoundTag nbt, CallbackInfo ci) {
    if (nbt.contains("wood_type")) {
      woodType = nbt.getString("wood_type");
    } else {
      woodType = null;
    }
    if (nbt.contains("rock_type")) {
      rockType = nbt.getString("rock_type");
    } else {
      rockType = null;
    }
    if (nbt.contains("soil_type")) {
      soilType = nbt.getString("soil_type");
    } else {
      soilType = null;
    }
  }

  @Inject(
      method = "serializeNBT()Lnet/minecraft/nbt/CompoundTag;",
      at = @At("RETURN"),
      locals = LocalCapture.CAPTURE_FAILEXCEPTION)
  private void serializeNBTInjector(CallbackInfoReturnable<CompoundTag> cir, CompoundTag nbt) {
    if (woodType != null) {
      nbt.putString("wood_type", woodType);
    }
    if (rockType != null) {
      nbt.putString("rock_type", rockType);
    }
    if (soilType != null) {
      nbt.putString("soil_type", soilType);
    }
  }

  @Inject(method = "getActiveStructure", at = @At("HEAD"), cancellable = true)
  private void getActiveStructureInjector(CallbackInfoReturnable<Blueprint> cir) {
    if (this.blueprint == null
        && this.structureName != null
        && !this.structureName.isEmpty()
        && pos != null) {
      PlacementSettings settings =
          new PlacementSettings(
              Settings.instance.getMirror(),
              BlockUtils.getRotation(Settings.instance.getRotation()));
      ((ISettingsExtension) settings)
          .setWoodType(((ISettingsExtension) (Object) Settings.instance).getWoodType());
      ((ISettingsExtension) settings)
          .setRockType(((ISettingsExtension) (Object) Settings.instance).getRockType());
      ((ISettingsExtension) settings)
          .setSoilType(((ISettingsExtension) (Object) Settings.instance).getSoilType());

      final IStructureHandler structure =
          new CreativeStructureHandler(
              Minecraft.getInstance().level, new BlockPos(0, 0, 0), structureName, settings, true);
      if (structure.hasBluePrint()) {
        this.blueprint = structure.getBluePrint();
      }
    }
    cir.setReturnValue(this.blueprint);
  }
}
