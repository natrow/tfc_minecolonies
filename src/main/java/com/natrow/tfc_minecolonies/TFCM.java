package com.natrow.tfc_minecolonies;

import com.natrow.tfc_minecolonies.block.TFCMBlocks;
import com.natrow.tfc_minecolonies.item.TFCMItems;
import com.natrow.tfc_minecolonies.minecolonies.TFCMInteractionValidatorInitializer;
import com.natrow.tfc_minecolonies.structurize.TFCMPlacementHandlers;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;


@Mod(TFCMConstants.MOD_ID)
public class TFCM
{
    public TFCM()
    {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        TFCMBlocks.BLOCKS.register(bus);
        TFCMItems.ITEMS.register(bus);

        TFCMPlacementHandlers.registerHandlers();
        TFCMInteractionValidatorInitializer.registerValidators();
    }
}
