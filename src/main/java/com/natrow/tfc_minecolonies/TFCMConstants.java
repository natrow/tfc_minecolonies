package com.natrow.tfc_minecolonies;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import com.natrow.tfc_minecolonies.block.TFCMBlocks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.util.Lazy;

import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.rock.Rock;
import net.dries007.tfc.common.blocks.soil.SoilBlockType;
import net.dries007.tfc.common.blocks.wood.Wood;

public class TFCMConstants
{
    public static final String MOD_ID = "tfc_minecolonies";

    // TFC conversion maps, lazy loaded
    public static final Lazy<Map<Block, ItemStack>> LEAVES_TO_SAPLINGS;
    public static final Lazy<Map<Block, ItemStack>> LOG_TO_SAPLINGS;
    public static final Lazy<Map<Block, Block>> ANVIL_TO_ROCK;
    public static final Lazy<Map<Block, Block>> SOIL_TO_FARMLAND;
    public static final Lazy<Map<Block, Map<String, Block>>> PLACEHOLDER_TO_WOOD;
    public static final Lazy<Map<Block, Map<String, Block>>> PLACEHOLDER_TO_STONE;
    public static final Lazy<Map<Block, Map<String, Block>>> PLACEHOLDER_TO_SOIL;
    public static final String FALLBACK_STONE = "gabbro";
    public static final String DROPDOWN_WOOD_ID = "woodType";
    public static final String BUTTON_NEXT_WOOD_ID = "nextWoodType";
    public static final String BUTTON_PREVIOUS_WOOD_ID = "previousWoodType";
    public static final String DROPDOWN_ROCK_ID = "rockType";
    public static final String BUTTON_NEXT_ROCK_ID = "nextRockType";
    public static final String BUTTON_PREVIOUS_ROCK_ID = "previousRockType";
    public static final String DROPDOWN_SOIL_ID = "soilType";
    public static final String BUTTON_NEXT_SOIL_ID = "nextSoilType";
    public static final String BUTTON_PREVIOUS_SOIL_ID = "previousSoilType";

    static
    {
        LEAVES_TO_SAPLINGS = Lazy.of(() -> TFCBlocks.WOODS.keySet()
            .stream()
            .collect(Collectors.toMap(e -> e.getBlock(Wood.BlockType.LEAVES)
                .get(), e -> new ItemStack(e.getBlock(Wood.BlockType.SAPLING)
                .get()
                .asItem(), 1))));
        LOG_TO_SAPLINGS = Lazy.of(() -> TFCBlocks.WOODS.keySet()
            .stream()
            .collect(Collectors.toMap(e -> e.getBlock(Wood.BlockType.LOG)
                .get(), e -> new ItemStack(e.getBlock(Wood.BlockType.SAPLING)
                .get()
                .asItem(), 1))));
        ANVIL_TO_ROCK = Lazy.of(() -> TFCBlocks.ROCK_ANVILS.keySet()
            .stream()
            .collect(Collectors.toMap(e -> e.getAnvil()
                .get(), e -> e.getBlock(Rock.BlockType.RAW)
                .get())));
        SOIL_TO_FARMLAND = Lazy.of(() -> TFCBlocks.SOIL.values()
            .stream()
            .flatMap(e -> e.entrySet()
                .stream())
            .collect(Collectors.toMap(e -> e.getValue()
                .get(), e -> TFCBlocks.SOIL.get(SoilBlockType.FARMLAND)
                .get(e.getKey())
                .get())));
        PLACEHOLDER_TO_WOOD = Lazy.of(() -> TFCMBlocks.PLACEHOLDER_WOODS.entrySet()
            .stream()
            .collect(Collectors.toMap(e -> e.getValue()
                .get(), e -> TFCBlocks.WOODS.entrySet()
                .stream()
                .collect(Collectors.toMap(e2 -> e2.getKey()
                    .getSerializedName(), e2 -> e2.getValue()
                    .get(e.getKey())
                    .get())))));
        PLACEHOLDER_TO_STONE = Lazy.of(TFCMConstants::createPlaceholderToStone);
        PLACEHOLDER_TO_SOIL = Lazy.of(() -> TFCMBlocks.PLACEHOLDER_SOIL.entrySet()
            .stream()
            .collect(Collectors.toMap(e -> e.getValue()
                .get(), e -> TFCBlocks.SOIL.get(e.getKey())
                .entrySet()
                .stream()
                .collect(Collectors.toMap(e2 -> e2.getKey()
                    .name()
                    .toLowerCase(Locale.ROOT), e2 -> e2.getValue()
                    .get())))));
    }

    public static ResourceLocation getResourceLocation(String resource)
    {
        return new ResourceLocation(MOD_ID, resource);
    }

    private static Map<Block, Map<String, Block>> createPlaceholderToStone()
    {
        Map<Block, Map<String, Block>> normalBlocks = TFCMBlocks.PLACEHOLDER_ROCK_BLOCKS.entrySet()
            .stream()
            .collect(Collectors.toMap(e -> e.getValue()
                .get(), e -> TFCBlocks.ROCK_BLOCKS.entrySet()
                .stream()
                .collect(Collectors.toMap(e2 -> e2.getKey()
                    .getSerializedName(), e2 -> e2.getValue()
                    .get(e.getKey())
                    .get()))));
        Map<Block, Map<String, Block>> wallBlocks = TFCMBlocks.PLACEHOLDER_ROCK_DECORATIONS.entrySet()
            .stream()
            .collect(Collectors.toMap(e -> e.getValue()
                .wall()
                .get(), e -> TFCBlocks.ROCK_DECORATIONS.entrySet()
                .stream()
                .collect(Collectors.toMap(e2 -> e2.getKey()
                    .getSerializedName(), e2 -> e2.getValue()
                    .get(e.getKey())
                    .wall()
                    .get()))));
        Map<Block, Map<String, Block>> slabBlocks = TFCMBlocks.PLACEHOLDER_ROCK_DECORATIONS.entrySet()
            .stream()
            .collect(Collectors.toMap(e -> e.getValue()
                .slab()
                .get(), e -> TFCBlocks.ROCK_DECORATIONS.entrySet()
                .stream()
                .collect(Collectors.toMap(e2 -> e2.getKey()
                    .getSerializedName(), e2 -> e2.getValue()
                    .get(e.getKey())
                    .slab()
                    .get()))));
        Map<Block, Map<String, Block>> stairBlocks = TFCMBlocks.PLACEHOLDER_ROCK_DECORATIONS.entrySet()
            .stream()
            .collect(Collectors.toMap(e -> e.getValue()
                .stair()
                .get(), e -> TFCBlocks.ROCK_DECORATIONS.entrySet()
                .stream()
                .collect(Collectors.toMap(e2 -> e2.getKey()
                    .getSerializedName(), e2 -> e2.getValue()
                    .get(e.getKey())
                    .stair()
                    .get()))));
        Map<Block, Map<String, Block>> anvilBlocks = TFCMBlocks.PLACEHOLDER_ROCK_ANVIL.stream()
            .collect(Collectors.toMap(e -> e, e -> TFCBlocks.ROCK_ANVILS.entrySet()
                .stream()
                .collect(Collectors.toMap(e2 -> e2.getKey()
                    .getSerializedName(), e2 -> e2.getValue()
                    .get()))));
        Map<Block, Map<String, Block>> magmaBlocks = TFCMBlocks.PLACEHOLDER_MAGMA_BLOCK.stream()
            .collect(Collectors.toMap(e -> e, e -> TFCBlocks.MAGMA_BLOCKS.entrySet()
                .stream()
                .collect(Collectors.toMap(e2 -> e2.getKey()
                    .getSerializedName(), e2 -> e2.getValue()
                    .get()))));

        return mergeMaps(normalBlocks, wallBlocks, slabBlocks, stairBlocks, anvilBlocks, magmaBlocks);
    }

    @SafeVarargs
    private static <K, V> Map<K, V> mergeMaps(Map<K, V>... values)
    {
        return Arrays.stream(values)
            .flatMap(e -> e.entrySet()
                .stream())
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
