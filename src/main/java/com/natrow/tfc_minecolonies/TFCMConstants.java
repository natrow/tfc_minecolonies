package com.natrow.tfc_minecolonies;

import com.natrow.tfc_minecolonies.block.TFCMBlocks;
import com.natrow.tfc_minecolonies.util.PlaceholderConversions;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.rock.Rock;
import net.dries007.tfc.common.blocks.soil.SoilBlockType;
import net.dries007.tfc.common.blocks.wood.Wood;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.registries.RegistryObject;

public class TFCMConstants {
  public static final String MOD_ID = "tfc_minecolonies";

  // TFC conversion maps, lazy loaded
  public static final Lazy<Map<Block, ItemStack>> LEAVES_TO_SAPLINGS;
  public static final Lazy<Map<Block, ItemStack>> LOG_TO_SAPLINGS;
  public static final Lazy<Map<Block, Block>> ANVIL_TO_ROCK;
  public static final Lazy<Map<Block, Block>> SOIL_TO_FARMLAND;
  public static final Lazy<Map<Block, Map<String, Block>>> PLACEHOLDER_TO_WOOD;
  public static final Lazy<Map<Block, Map<String, Block>>> PLACEHOLDER_TO_STONE;
  public static final Lazy<Map<Block, Map<String, Block>>> PLACEHOLDER_TO_SOIL;
  public static final String FALLBACK_STONE = "gabbro";
  public static final String DROPDOWN_WOOD_ID = "woodType";
  public static final String BUTTON_NEXT_WOOD_ID = "nextWoodType";
  public static final String BUTTON_PREVIOUS_WOOD_ID = "previousWoodType";
  public static final String DROPDOWN_ROCK_ID = "rockType";
  public static final String BUTTON_NEXT_ROCK_ID = "nextRockType";
  public static final String BUTTON_PREVIOUS_ROCK_ID = "previousRockType";
  public static final String DROPDOWN_SOIL_ID = "soilType";
  public static final String BUTTON_NEXT_SOIL_ID = "nextSoilType";
  public static final String BUTTON_PREVIOUS_SOIL_ID = "previousSoilType";

  static {
    LEAVES_TO_SAPLINGS =
        Lazy.of(
            () ->
                TFCBlocks.WOODS.keySet().stream()
                    .collect(
                        Collectors.toMap(
                            e -> e.getBlock(Wood.BlockType.LEAVES).get(),
                            e ->
                                new ItemStack(
                                    e.getBlock(Wood.BlockType.SAPLING).get().asItem(), 1))));
    LOG_TO_SAPLINGS =
        Lazy.of(
            () ->
                TFCBlocks.WOODS.keySet().stream()
                    .collect(
                        Collectors.toMap(
                            e -> e.getBlock(Wood.BlockType.LOG).get(),
                            e ->
                                new ItemStack(
                                    e.getBlock(Wood.BlockType.SAPLING).get().asItem(), 1))));
    ANVIL_TO_ROCK =
        Lazy.of(
            () ->
                TFCBlocks.ROCK_ANVILS.keySet().stream()
                    .collect(
                        Collectors.toMap(
                            e -> e.getAnvil().get(), e -> e.getBlock(Rock.BlockType.RAW).get())));
    SOIL_TO_FARMLAND =
        Lazy.of(
            () ->
                TFCBlocks.SOIL.values().stream()
                    .flatMap(e -> e.entrySet().stream())
                    .collect(
                        Collectors.toMap(
                            e -> e.getValue().get(),
                            e ->
                                TFCBlocks.SOIL.get(SoilBlockType.FARMLAND).get(e.getKey()).get())));
    PLACEHOLDER_TO_WOOD =
        Lazy.of(
            () -> PlaceholderConversions.createLUT(TFCMBlocks.PLACEHOLDER_WOODS, TFCBlocks.WOODS));
    PLACEHOLDER_TO_STONE = Lazy.of(TFCMConstants::createPlaceholderToStone);
    PLACEHOLDER_TO_SOIL = Lazy.of(TFCMConstants::createPlaceholderToSoil);
  }

  public static ResourceLocation getResourceLocation(String resource) {
    return new ResourceLocation(MOD_ID, resource);
  }

  private static Map<Block, Map<String, Block>> createPlaceholderToSoil() {
    Map<Block, Map<String, Block>> normalBlocks =
        PlaceholderConversions.createRevLUT(TFCMBlocks.PLACEHOLDER_SOIL, TFCBlocks.SOIL);
    Map<Block, Map<String, Block>> decoBlocks =
        PlaceholderConversions.createDecoLUT(
            TFCMBlocks.PLACEHOLDER_MUD_BRICK_DECORATIONS, TFCBlocks.MUD_BRICK_DECORATIONS);

    return mergeMaps(normalBlocks, decoBlocks);
  }

  private static Map<Block, Map<String, Block>> createPlaceholderToStone() {
    Map<Block, Map<String, Block>> normalBlocks =
        PlaceholderConversions.createLUT(TFCMBlocks.PLACEHOLDER_ROCK_BLOCKS, TFCBlocks.ROCK_BLOCKS);
    Map<Block, Map<String, Block>> decoBlocks =
        PlaceholderConversions.createDecoLUT(
            TFCMBlocks.PLACEHOLDER_ROCK_DECORATIONS, TFCBlocks.ROCK_DECORATIONS);
    Map<Block, Map<String, Block>> auxBlocks =
        Map.of(
            TFCMBlocks.PLACEHOLDER_ROCK_ANVIL.get(),
            PlaceholderConversions.createLUTLeaf(
                TFCBlocks.ROCK_ANVILS, Rock::getSerializedName, RegistryObject::get),
            TFCMBlocks.PLACEHOLDER_MAGMA_BLOCK.get(),
            PlaceholderConversions.createLUTLeaf(
                TFCBlocks.MAGMA_BLOCKS, Rock::getSerializedName, RegistryObject::get));

    return mergeMaps(normalBlocks, decoBlocks, auxBlocks);
  }

  @SafeVarargs
  private static <K, V> Map<K, V> mergeMaps(Map<K, V>... values) {
    return Arrays.stream(values)
        .flatMap(e -> e.entrySet().stream())
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }
}
