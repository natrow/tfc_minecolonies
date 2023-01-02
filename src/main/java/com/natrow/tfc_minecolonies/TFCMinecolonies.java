package com.natrow.tfc_minecolonies;

import com.natrow.tfc_minecolonies.block.TFCMinecoloniesBlocks;
import com.natrow.tfc_minecolonies.item.TFCMinecoloniesItems;
import com.natrow.tfc_minecolonies.structurize.TFCMinecoloniesPlacementHandlers;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;


@Mod(Constants.MOD_ID)
public class TFCMinecolonies
{
    public TFCMinecolonies()
    {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        TFCMinecoloniesBlocks.BLOCKS.register(bus);
        TFCMinecoloniesItems.ITEMS.register(bus);

        TFCMinecoloniesPlacementHandlers.registerHandlers();
    }
}
