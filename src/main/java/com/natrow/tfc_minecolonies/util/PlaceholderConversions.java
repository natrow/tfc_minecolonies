package com.natrow.tfc_minecolonies.util;

import com.natrow.tfc_minecolonies.TFCMConstants;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.dries007.tfc.common.blocks.DecorationBlockRegistryObject;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.RegistryObject;

public abstract class PlaceholderConversions {
  public static Block convertPlaceholder(
      Block target, String woodType, String stoneType, String soilType) {
    if (TFCMConstants.PLACEHOLDER_TO_WOOD.get().containsKey(target)) {
      return TFCMConstants.PLACEHOLDER_TO_WOOD.get().get(target).get(woodType);
    } else if (TFCMConstants.PLACEHOLDER_TO_STONE.get().containsKey(target)) {
      return TFCMConstants.PLACEHOLDER_TO_STONE
          .get()
          .get(target)
          .getOrDefault(
              stoneType,
              TFCMConstants.PLACEHOLDER_TO_STONE
                  .get()
                  .get(target)
                  .get(TFCMConstants.FALLBACK_STONE));
    } else if (TFCMConstants.PLACEHOLDER_TO_SOIL.get().containsKey(target)) {
      return TFCMConstants.PLACEHOLDER_TO_SOIL.get().get(target).get(soilType);
    } else return target; // ignore non-placeholder blocks
  }

  public static <K1, K2 extends Enum<K2>> Map<Block, Map<String, Block>> createLUT(
      Map<K1, RegistryObject<Block>> placeholders,
      Map<K2, Map<K1, RegistryObject<Block>>> targets) {
    return placeholders.entrySet().stream()
        .collect(
            Collectors.toMap(
                e -> e.getValue().get(),
                e ->
                    createLUTLeaf(
                        targets,
                        (k) -> k.name().toLowerCase(Locale.ROOT),
                        (v) -> v.get(e.getKey()).get())));
  }

  public static <K1, K2 extends Enum<K2>> Map<Block, Map<String, Block>> createRevLUT(
      Map<K1, RegistryObject<Block>> placeholders,
      Map<K1, Map<K2, RegistryObject<Block>>> targets) {
    return createLUT(placeholders, reverseMap(targets));
  }

  public static <K extends Enum<K>> Map<Block, Map<String, Block>> createDecoLUT(
      DecorationBlockRegistryObject placeholders, Map<K, DecorationBlockRegistryObject> targets) {
    return Map.of(
        placeholders.slab().get(),
        createLUTLeaf(targets, (k) -> k.name().toLowerCase(Locale.ROOT), (v) -> v.slab().get()),
        placeholders.wall().get(),
        createLUTLeaf(targets, (k) -> k.name().toLowerCase(Locale.ROOT), (v) -> v.wall().get()),
        placeholders.stair().get(),
        createLUTLeaf(targets, (k) -> k.name().toLowerCase(Locale.ROOT), (v) -> v.stair().get()));
  }

  public static <K1, K2 extends Enum<K2>> Map<Block, Map<String, Block>> createDecoLUT(
      Map<K1, DecorationBlockRegistryObject> placeholders,
      Map<K2, Map<K1, DecorationBlockRegistryObject>> targets) {
    Map<K1, Map<K2, DecorationBlockRegistryObject>> revMap = reverseMap(targets);

    return placeholders.entrySet().stream()
        .flatMap(e -> createDecoLUT(e.getValue(), revMap.get(e.getKey())).entrySet().stream())
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  public static <K, V> Map<String, Block> createLUTLeaf(
      Map<K, V> target, Function<K, String> nameResolver, Function<V, Block> blockResolver) {
    return target.entrySet().stream()
        .collect(
            Collectors.toMap(
                e -> nameResolver.apply(e.getKey()), e -> blockResolver.apply(e.getValue())));
  }

  private static <K1, K2, V> Map<K1, Map<K2, V>> reverseMap(Map<K2, Map<K1, V>> map) {
    return map.entrySet().stream()
        .flatMap(
            e ->
                e.getValue().entrySet().stream()
                    .map(
                        e2 ->
                            new AbstractMap.SimpleEntry<>(
                                e2.getKey(),
                                new AbstractMap.SimpleEntry<>(e.getKey(), e2.getValue()))))
        .collect(
            HashMap::new,
            (result, entry) ->
                result
                    .computeIfAbsent(entry.getKey(), k -> new HashMap<>())
                    .put(entry.getValue().getKey(), entry.getValue().getValue()),
            Map::putAll);
  }
}
