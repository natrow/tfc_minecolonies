package com.natrow.tfc_minecolonies.block;

import java.util.function.Supplier;
import com.natrow.tfc_minecolonies.Constants;
import com.natrow.tfc_minecolonies.item.TFCMinecoloniesItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import net.dries007.tfc.client.TFCSounds;
import net.dries007.tfc.common.TFCItemGroup;
import net.dries007.tfc.common.blocks.ExtendedBlock;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.TFCMaterials;

public class TFCMinecoloniesBlocks
{
    public static final DeferredRegister<Block> BLOCKS;
    public static final RegistryObject<Block> REINFORCED_THATCH;

    static
    {
        BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Constants.MOD_ID);
        REINFORCED_THATCH = register("reinforced_thatch", () -> new ExtendedBlock(ExtendedProperties.of(TFCMaterials.THATCH_COLOR_LEAVES).strength(0.6F, 0.4F).sound(TFCSounds.THATCH).flammable(50, 100)), TFCItemGroup.MISC);
    }

    /*
     * Register BlockItems with a Creative Mode tab
     */
    private static <T extends Block> RegistryObject<T> register(String name, Supplier<T> blockSupplier, CreativeModeTab group)
    {
        final RegistryObject<T> block = BLOCKS.register(name, blockSupplier);
        TFCMinecoloniesItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().tab(group)));
        return block;
    }
}