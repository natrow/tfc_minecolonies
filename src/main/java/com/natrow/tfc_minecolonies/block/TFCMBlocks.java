package com.natrow.tfc_minecolonies.block;

import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import com.natrow.tfc_minecolonies.TFCMConstants;
import com.natrow.tfc_minecolonies.TFCMCreativeTab;
import com.natrow.tfc_minecolonies.item.TFCMItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

import net.dries007.tfc.client.TFCSounds;
import net.dries007.tfc.common.blocks.ExtendedBlock;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.TFCMaterials;
import net.dries007.tfc.common.blocks.wood.Wood;
import net.dries007.tfc.util.Helpers;

public class TFCMBlocks
{
    public static final DeferredRegister<Block> BLOCKS;
    public static final RegistryObject<Block> REINFORCED_THATCH;
    public static final Map<Wood.BlockType, RegistryObject<Block>> WOODS;

    static {
        BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, TFCMConstants.MOD_ID);
        REINFORCED_THATCH = register("reinforced_thatch", () -> new ExtendedBlock(ExtendedProperties.of(TFCMaterials.THATCH_COLOR_LEAVES).strength(0.6F, 0.4F).sound(TFCSounds.THATCH).flammable(50, 100)));
        WOODS = Helpers.mapOfKeys(Wood.BlockType.class, (type) ->
            register(type.nameFor(TFCMWood.PLACEHOLDER), TFCMWood.create(type, TFCMWood.PLACEHOLDER), type.createBlockItem(new Item.Properties().tab(TFCMCreativeTab.TFC_MINECOLONIES)))
        );
    }

    private static <T extends Block> RegistryObject<T> register(String name, Supplier<T> blockSupplier)
    {
        final RegistryObject<T> block = BLOCKS.register(name, blockSupplier);
        TFCMItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().tab(TFCMCreativeTab.TFC_MINECOLONIES)));
        return block;
    }

    private static <T extends Block> RegistryObject<T> register(String name, Supplier<T> blockSupplier, @Nullable Function<T, ? extends BlockItem> blockItemFactory)
    {
        String actualName = name.toLowerCase(Locale.ROOT);
        RegistryObject<T> block = BLOCKS.register(actualName, blockSupplier);
        if (blockItemFactory != null)
        {
            TFCMItems.ITEMS.register(actualName, () -> blockItemFactory.apply(block.get()));
        }

        return block;
    }
}