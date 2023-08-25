package com.natrow.tfc_minecolonies.mixin;

import com.minecolonies.api.colony.requestsystem.requestable.Tool;
import java.util.Set;
import net.dries007.tfc.common.items.ScytheItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(value = Tool.class, remap = false)
public abstract class ToolMixin {
  /** Add tool class to scythe items */
  @Inject(method = "getToolClasses", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
  private void getToolClassesInjector(
      ItemStack stack, CallbackInfoReturnable<Set<String>> cir, Set<String> set) {
    if (stack.getItem() instanceof ScytheItem) {
      set.add("scythe");
    }
  }
}
