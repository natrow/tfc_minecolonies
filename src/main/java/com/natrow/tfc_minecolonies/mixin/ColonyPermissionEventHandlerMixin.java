package com.natrow.tfc_minecolonies.mixin;

import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.coremod.permissions.ColonyPermissionEventHandler;
import com.natrow.tfc_minecolonies.minecolonies.TFCMFakePlayerManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.eventbus.api.Event;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ColonyPermissionEventHandler.class, remap = false)
public abstract class ColonyPermissionEventHandlerMixin {
  /**
   * Prevent block interactions from being cancelled for fake players that were created by this mod
   */
  @Inject(method = "checkEventCancelation", at = @At("HEAD"), cancellable = true)
  private void checkEventCancellationInjector(
      Action action,
      Player playerIn,
      Level world,
      Event event,
      BlockPos pos,
      CallbackInfoReturnable<Boolean> cir) {
    if (playerIn instanceof FakePlayer fakePlayer) {
      if (TFCMFakePlayerManager.isFakePlayer(fakePlayer)) cir.setReturnValue(false);
    }
  }
}
