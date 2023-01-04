package com.natrow.tfc_minecolonies.item;

import com.natrow.tfc_minecolonies.TFCMinecoloniesConstants;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import net.dries007.tfc.common.TFCItemGroup;

public class TFCMinecoloniesItems
{
    public static final DeferredRegister<Item> ITEMS;
    public static final RegistryObject<Item> FIREWOOD;

    static
    {
        ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, TFCMinecoloniesConstants.MOD_ID);
        FIREWOOD = register("firewood", TFCItemGroup.WOOD);
    }

    private static RegistryObject<Item> register(String name, CreativeModeTab group)
    {
        return ITEMS.register(name, () -> new Item(new Item.Properties().tab(group)));
    }
}