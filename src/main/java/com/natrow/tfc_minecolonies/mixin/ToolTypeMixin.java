package com.natrow.tfc_minecolonies.mixin;

import com.minecolonies.api.util.constant.IToolType;
import com.minecolonies.api.util.constant.ToolType;
import com.natrow.tfc_minecolonies.minecolonies.TFCMToolType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ToolType.class, remap = false)
public abstract class ToolTypeMixin {
  /** Attempts to get TFC tool types before looking for minecolonies ones. */
  @Inject(method = "getToolType", at = @At("HEAD"), cancellable = true)
  private static void getToolTypeInjector(String tool, CallbackInfoReturnable<IToolType> cir) {
    TFCMToolType.getToolType(tool).ifPresent(cir::setReturnValue);
  }
}
