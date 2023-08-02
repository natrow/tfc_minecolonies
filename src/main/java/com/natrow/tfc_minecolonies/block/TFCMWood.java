package com.natrow.tfc_minecolonies.block;

import com.natrow.tfc_minecolonies.TFCMConstants;
import java.util.Locale;
import java.util.function.Supplier;
import net.dries007.tfc.common.blockentities.LoomBlockEntity;
import net.dries007.tfc.common.blockentities.TFCBlockEntities;
import net.dries007.tfc.common.blocks.ExtendedBlock;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.wood.TFCLoomBlock;
import net.dries007.tfc.common.blocks.wood.Wood;
import net.dries007.tfc.util.registry.RegistryWood;
import net.dries007.tfc.world.feature.tree.TFCTreeGrower;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;

public enum TFCMWood implements RegistryWood {
  PLACEHOLDER(MaterialColor.WOOD, MaterialColor.COLOR_BROWN, 8);

  public static final TFCMWood[] VALUES = values();

  public static Supplier<Block> create(Wood.BlockType type, RegistryWood wood) {
    if (type == Wood.BlockType.SAPLING) {
      return () ->
          new ExtendedBlock(
              ExtendedProperties.of(Material.PLANT)
                  .noCollission()
                  .randomTicks()
                  .strength(0.0F)
                  .sound(SoundType.GRASS)
                  .flammableLikeLeaves());
    } else if (type == Wood.BlockType.LOOM) {
      return () ->
          new TFCLoomBlock(
              ExtendedProperties.of(Material.WOOD, wood.woodColor())
                  .sound(SoundType.WOOD)
                  .strength(2.5F)
                  .noOcclusion()
                  .flammableLikePlanks()
                  .blockEntity(TFCBlockEntities.LOOM)
                  .ticks(LoomBlockEntity::tick),
              new ResourceLocation("tfc:block/wood/planks/oak"));
    }

    return type.create(wood);
  }

  private final String serializedName;
  private final MaterialColor woodColor;
  private final MaterialColor barkColor;
  private final TFCTreeGrower tree;
  private final int maxDecayDistance;

  TFCMWood(MaterialColor woodColor, MaterialColor barkColor, int maxDecayDistance) {
    this.serializedName = name().toLowerCase(Locale.ROOT);
    this.woodColor = woodColor;
    this.barkColor = barkColor;
    this.tree =
        new TFCTreeGrower(
            TFCMConstants.getResourceLocation("tree/" + serializedName),
            TFCMConstants.getResourceLocation("tree/" + serializedName + "_large"));
    this.maxDecayDistance = maxDecayDistance;
  }

  @Override
  public String getSerializedName() {
    return serializedName;
  }

  @Override
  public MaterialColor woodColor() {
    return woodColor;
  }

  @Override
  public MaterialColor barkColor() {
    return barkColor;
  }

  @Override
  public TFCTreeGrower tree() {
    return tree;
  }

  @Override
  public int maxDecayDistance() {
    return maxDecayDistance;
  }

  @Override
  public int daysToGrow() {
    return 10;
  }

  @Override
  public Supplier<Block> getBlock(Wood.BlockType type) {
    return TFCMBlocks.PLACEHOLDER_WOODS.get(type);
  }
}
