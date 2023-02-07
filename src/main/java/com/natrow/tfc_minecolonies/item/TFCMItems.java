package com.natrow.tfc_minecolonies.item;

import com.natrow.tfc_minecolonies.TFCMConstants;
import com.natrow.tfc_minecolonies.TFCMCreativeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class TFCMItems
{
    public static final DeferredRegister<Item> ITEMS;
    public static final RegistryObject<Item> FIREWOOD;

    static
    {
        ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, TFCMConstants.MOD_ID);
        FIREWOOD = register("firewood");
    }

    private static RegistryObject<Item> register(String name)
    {
        return ITEMS.register(name, () -> new Item(new Item.Properties().tab(TFCMCreativeTab.TFC_MINECOLONIES)));
    }
}