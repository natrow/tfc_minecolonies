package com.natrow.tfc_minecolonies;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

import net.dries007.tfc.common.items.TFCItems;
import net.dries007.tfc.util.Metal;

public class TFCMCreativeTab
{
    public static final CreativeModeTab TFC_MINECOLONIES = new CreativeModeTab(TFCMConstants.MOD_ID) {
        @Override
        public ItemStack makeIcon()
        {
            return new ItemStack(TFCItems.METAL_ITEMS.get(Metal.Default.STEEL).get(Metal.ItemType.HELMET).get());
        }
    };

    /**
     * Hidden constructor
     */
    private TFCMCreativeTab()
    {

    }
}
