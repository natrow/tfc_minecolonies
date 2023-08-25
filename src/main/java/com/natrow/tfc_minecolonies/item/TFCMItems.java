package com.natrow.tfc_minecolonies.item;

import com.natrow.tfc_minecolonies.TFCMConstants;
import com.natrow.tfc_minecolonies.TFCMCreativeTab;
import com.natrow.tfc_minecolonies.block.TFCMBlocks;
import com.natrow.tfc_minecolonies.block.TFCMWood;
import java.util.Locale;
import java.util.function.Supplier;
import net.dries007.tfc.common.blocks.wood.Wood;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SignItem;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class TFCMItems {
  public static final DeferredRegister<Item> ITEMS;
  public static final RegistryObject<Item> FIREWOOD;
  public static final RegistryObject<Item> PLACEHOLDER_SIGN;
  public static final RegistryObject<Item> PLACEHOLDER_SUPPORT;

  static {
    ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, TFCMConstants.MOD_ID);
    FIREWOOD = register("firewood");
    PLACEHOLDER_SIGN =
        register(
            "wood/sign/" + TFCMWood.PLACEHOLDER.name(),
            () ->
                new SignItem(
                    new Item.Properties().tab(TFCMCreativeTab.TFC_MINECOLONIES),
                    TFCMBlocks.PLACEHOLDER_WOODS.get(Wood.BlockType.SIGN).get(),
                    TFCMBlocks.PLACEHOLDER_WOODS.get(Wood.BlockType.WALL_SIGN).get()));
    PLACEHOLDER_SUPPORT =
        register(
            "wood/support/" + TFCMWood.PLACEHOLDER.name(),
            () ->
                new StandingAndWallBlockItem(
                    TFCMBlocks.PLACEHOLDER_WOODS.get(Wood.BlockType.VERTICAL_SUPPORT).get(),
                    TFCMBlocks.PLACEHOLDER_WOODS.get(Wood.BlockType.HORIZONTAL_SUPPORT).get(),
                    new Item.Properties().tab(TFCMCreativeTab.TFC_MINECOLONIES)));
  }

  private static RegistryObject<Item> register(String name) {
    return ITEMS.register(
        name, () -> new Item(new Item.Properties().tab(TFCMCreativeTab.TFC_MINECOLONIES)));
  }

  private static <T extends Item> RegistryObject<Item> register(String name, Supplier<T> item) {
    return ITEMS.register(name.toLowerCase(Locale.ROOT), item);
  }
}
