package com.natrow.tfc_minecolonies;

import com.natrow.tfc_minecolonies.block.TFCMBlocks;
import com.natrow.tfc_minecolonies.client.TFCMClientEventHandler;
import com.natrow.tfc_minecolonies.events.TFCMCommonEventHandler;
import com.natrow.tfc_minecolonies.item.TFCMItems;
import com.natrow.tfc_minecolonies.minecolonies.TFCMInteractionValidatorInitializer;
import com.natrow.tfc_minecolonies.structurize.TFCMPlacementHandlers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod(TFCMConstants.MOD_ID)
public class TFCM {
  public TFCM() {
    IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

    TFCMBlocks.BLOCKS.register(bus);
    TFCMItems.ITEMS.register(bus);

    TFCMPlacementHandlers.registerHandlers();
    TFCMInteractionValidatorInitializer.registerValidators();

    TFCMCommonEventHandler.init(bus);

    if (FMLEnvironment.dist == Dist.CLIENT) {
      TFCMClientEventHandler.init(bus);
    }
  }
}
