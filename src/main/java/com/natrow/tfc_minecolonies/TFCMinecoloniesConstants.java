package com.natrow.tfc_minecolonies;

import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.util.Lazy;

import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.rock.Rock;
import net.dries007.tfc.common.blocks.wood.Wood;

public class TFCMinecoloniesConstants
{
    public static final String MOD_ID = "tfc_minecolonies";

    // TFC conversion maps, lazy loaded
    public static final Lazy<Map<Block, ItemStack>> LEAVES_TO_SAPLINGS;
    public static final Lazy<Map<Block, Block>> ANVIL_TO_ROCK;

    static
    {
        LEAVES_TO_SAPLINGS = Lazy.of(() -> TFCBlocks.WOODS.keySet().stream().collect(Collectors.toMap(e -> e.getBlock(Wood.BlockType.LEAVES).get(), e -> new ItemStack(e.getBlock(Wood.BlockType.SAPLING).get().asItem(), 1))));
        ANVIL_TO_ROCK = Lazy.of(() -> TFCBlocks.ROCK_ANVILS.keySet().stream().collect(Collectors.toMap(e -> e.getAnvil().get(), e -> e.getBlock(Rock.BlockType.RAW).get())));
    }
}
