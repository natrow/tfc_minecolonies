package com.natrow.tfc_minecolonies.mixin;

import com.ldtteam.structurize.client.gui.WindowBuildTool;
import com.ldtteam.structurize.helpers.Settings;
import com.ldtteam.structurize.management.StructureName;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.client.gui.WindowBuildDecoration;
import com.minecolonies.coremod.client.gui.WindowMinecoloniesBuildTool;
import com.minecolonies.coremod.network.messages.server.BuildToolPlaceMessage;
import com.natrow.tfc_minecolonies.minecolonies.TFCMBuildToolPasteMessage;
import com.natrow.tfc_minecolonies.minecolonies.TFCMBuildToolPlaceMessage;
import com.natrow.tfc_minecolonies.structurize.ISettingsExtension;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = WindowMinecoloniesBuildTool.class, remap = false)
public class WindowMinecoloniesBuildToolMixin extends WindowBuildTool {
  /** Dummy constructor */
  public WindowMinecoloniesBuildToolMixin(@Nullable BlockPos pos, int groundstyle) {
    super(pos, groundstyle);
  }

  /**
   * Override the "style" field to encode extended information
   *
   * @param structureName real structure name
   * @param ci callback info
   */
  @Inject(method = "place", at = @At("HEAD"), cancellable = true)
  private void placeInjector(StructureName structureName, CallbackInfo ci) {
    final BlockPos offset = Settings.instance.getActiveStructure().getPrimaryBlockOffset();
    final BlockState state = Settings.instance.getActiveStructure().getBlockState(offset);

    final ISettingsExtension settings = (ISettingsExtension) (Object) Settings.instance;

    final String woodType = settings.getWoodType();
    final String rockType = settings.getRockType();
    final String soilType = settings.getSoilType();

    if (structureName.isHut()) {
      Network.getNetwork()
          .sendToServer(
              new TFCMBuildToolPlaceMessage(
                  structureName.toString(),
                  structureName.getLocalizedName(),
                  Settings.instance.getPosition(),
                  Settings.instance.getRotation(),
                  structureName.isHut(),
                  Settings.instance.getMirror(),
                  state,
                  woodType,
                  rockType,
                  soilType));
    } else {
      Minecraft.getInstance()
          .tell(
              new WindowBuildDecoration(
                      new BuildToolPlaceMessage(
                          structureName.toString(),
                          structureName.getLocalizedName(),
                          Settings.instance.getPosition(),
                          Settings.instance.getRotation(),
                          structureName.isHut(),
                          Settings.instance.getMirror(),
                          state),
                      Settings.instance.getPosition(),
                      structureName)
                  ::open);
    }

    ci.cancel();
  }

  /**
   * Override the "style" field to encode extended information
   *
   * @param name real structure name
   * @param complete whether the structure is complete
   * @param ci callback info
   */
  @Inject(method = "paste", at = @At("HEAD"), cancellable = true)
  private void pasteInjector(StructureName name, boolean complete, CallbackInfo ci) {
    final BlockPos offset = Settings.instance.getActiveStructure().getPrimaryBlockOffset();
    final BlockState state = Settings.instance.getActiveStructure().getBlockState(offset);

    final ISettingsExtension settings = (ISettingsExtension) (Object) Settings.instance;

    final String woodType = settings.getWoodType();
    final String rockType = settings.getRockType();
    final String soilType = settings.getSoilType();

    Network.getNetwork()
        .sendToServer(
            new TFCMBuildToolPasteMessage(
                name.toString(),
                name.toString(),
                Settings.instance.getPosition(),
                Settings.instance.getRotation(),
                name.isHut(),
                Settings.instance.getMirror(),
                complete,
                state,
                woodType,
                rockType,
                soilType));

    ci.cancel();
  }
}
