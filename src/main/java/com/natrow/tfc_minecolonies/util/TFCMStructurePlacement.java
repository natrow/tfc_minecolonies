package com.natrow.tfc_minecolonies.util;

import com.ldtteam.structurize.api.util.Log;
import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.management.Manager;
import com.ldtteam.structurize.placement.StructurePlacer;
import com.ldtteam.structurize.placement.structure.CreativeStructureHandler;
import com.ldtteam.structurize.placement.structure.IStructureHandler;
import com.ldtteam.structurize.util.PlacementSettings;
import com.ldtteam.structurize.util.TickedWorldOperation;
import com.minecolonies.api.util.CreativeBuildingStructureHandler;
import com.natrow.tfc_minecolonies.structurize.ISettingsExtension;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class TFCMStructurePlacement {

  public static Blueprint loadAndPlaceBuildingWithRotation(
      final Level worldObj,
      @NotNull final String name,
      @NotNull final BlockPos pos,
      final Rotation rotation,
      @NotNull final Mirror mirror,
      final boolean fancyPlacement,
      @Nullable final ServerPlayer player,
      @NotNull final String woodType,
      @NotNull final String stoneType,
      @NotNull final String soilType) {
    try {
      PlacementSettings settings = new PlacementSettings(mirror, rotation);
      ((ISettingsExtension) settings).setWoodType(woodType);
      ((ISettingsExtension) settings).setRockType(stoneType);
      ((ISettingsExtension) settings).setSoilType(soilType);
      @NotNull
      final IStructureHandler structure =
          new CreativeBuildingStructureHandler(
              worldObj, pos, name, new PlacementSettings(mirror, rotation), fancyPlacement);
      if (structure.hasBluePrint()) {
        structure.getBluePrint().rotateWithMirror(rotation, mirror, worldObj);

        @NotNull final StructurePlacer instantPlacer = new StructurePlacer(structure);
        Manager.addToQueue(new TickedWorldOperation(instantPlacer, player));
      }
      return structure.getBluePrint();
    } catch (final IllegalStateException e) {
      Log.getLogger().warn("Could not load structure!", e);
    }
    return null;
  }

  public static void loadAndPlaceStructureWithRotation(
      final Level worldObj,
      final String name,
      final BlockPos pos,
      final Rotation rotation,
      final Mirror mirror,
      final boolean fancyPlacement,
      final ServerPlayer player,
      @NotNull final String woodType,
      @NotNull final String stoneType,
      @NotNull final String soilType) {
    try {
      PlacementSettings settings = new PlacementSettings(mirror, rotation);
      ((ISettingsExtension) settings).setWoodType(woodType);
      ((ISettingsExtension) settings).setRockType(stoneType);
      ((ISettingsExtension) settings).setSoilType(soilType);

      final IStructureHandler structure =
          new CreativeStructureHandler(worldObj, pos, name, settings, fancyPlacement);
      structure.getBluePrint().rotateWithMirror(rotation, mirror, worldObj);

      final StructurePlacer instantPlacer = new StructurePlacer(structure);
      Manager.addToQueue(new TickedWorldOperation(instantPlacer, player));
    } catch (final IllegalStateException e) {
      Log.getLogger().warn("Could not load structure!", e);
    }
  }
}
