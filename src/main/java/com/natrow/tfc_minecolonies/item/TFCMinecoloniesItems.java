package com.natrow.tfc_minecolonies.item;

import com.natrow.tfc_minecolonies.Constants;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class TFCMinecoloniesItems
{
    public static final DeferredRegister<Item> ITEMS;

    static
    {
        ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Constants.MOD_ID);
    }
}