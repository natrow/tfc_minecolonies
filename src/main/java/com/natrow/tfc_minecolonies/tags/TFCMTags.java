package com.natrow.tfc_minecolonies.tags;

import com.natrow.tfc_minecolonies.TFCMConstants;
import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class TFCMTags {
  public static class Blocks {
    public static final TagKey<Block> HOEABLE = create("hoeable");
    public static final TagKey<Block> ALABASTER = create("alabaster");
    public static final TagKey<Block> SANDSTONE = create("sandstone");
    public static final TagKey<Block> PLACEHOLDER_LOGS = create("placeholder_logs");

    private static TagKey<Block> create(final String id) {
      return TagKey.create(Registry.BLOCK_REGISTRY, TFCMConstants.getResourceLocation(id));
    }
  }

  public static class Items {
    public static final TagKey<Item> PLACEHOLDER_LOGS = create("placeholder_logs");

    private static TagKey<Item> create(final String id) {
      return TagKey.create(Registry.ITEM_REGISTRY, TFCMConstants.getResourceLocation(id));
    }
  }
}
