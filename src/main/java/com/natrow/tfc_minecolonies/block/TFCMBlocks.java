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
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

import net.dries007.tfc.client.TFCSounds;
import net.dries007.tfc.common.blockentities.TFCBlockEntities;
import net.dries007.tfc.common.blocks.DecorationBlockRegistryObject;
import net.dries007.tfc.common.blocks.ExtendedBlock;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.TFCMagmaBlock;
import net.dries007.tfc.common.blocks.TFCMaterials;
import net.dries007.tfc.common.blocks.rock.Rock;
import net.dries007.tfc.common.blocks.rock.RockAnvilBlock;
import net.dries007.tfc.common.blocks.soil.SoilBlockType;
import net.dries007.tfc.common.blocks.wood.Wood;
import net.dries007.tfc.util.Helpers;

public class TFCMBlocks
{
    public static final DeferredRegister<Block> BLOCKS;
    public static final RegistryObject<Block> REINFORCED_THATCH;
    public static final Map<Wood.BlockType, RegistryObject<Block>> PLACEHOLDER_WOODS;
    public static final Map<Rock.BlockType, RegistryObject<Block>> PLACEHOLDER_ROCK_BLOCKS;
    public static final Map<Rock.BlockType, DecorationBlockRegistryObject> PLACEHOLDER_ROCK_DECORATIONS;
    public static final RegistryObject<Block> PLACEHOLDER_ROCK_ANVIL;
    public static final RegistryObject<Block> PLACEHOLDER_MAGMA_BLOCK;

    public static final Map<SoilBlockType, RegistryObject<Block>> PLACEHOLDER_SOIL;

    static
    {
        BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, TFCMConstants.MOD_ID);
        REINFORCED_THATCH = register("reinforced_thatch", () -> new ExtendedBlock(ExtendedProperties.of(TFCMaterials.THATCH_COLOR_LEAVES).strength(0.6F, 0.4F).sound(TFCSounds.THATCH).flammable(50, 100)));
        PLACEHOLDER_WOODS = Helpers.mapOfKeys(Wood.BlockType.class, (type) -> register(type.nameFor(TFCMWood.PLACEHOLDER), TFCMWood.create(type, TFCMWood.PLACEHOLDER), type.createBlockItem(new Item.Properties().tab(TFCMCreativeTab.TFC_MINECOLONIES))));
        PLACEHOLDER_ROCK_BLOCKS = Helpers.mapOfKeys(Rock.BlockType.class, (type) -> register("rock/" + type.name() + "/" + TFCMRock.PLACEHOLDER.name(), () -> type.create(TFCMRock.PLACEHOLDER)));
        PLACEHOLDER_ROCK_DECORATIONS = Helpers.mapOfKeys(Rock.BlockType.class, Rock.BlockType::hasVariants, (type) -> new DecorationBlockRegistryObject(register("rock/" + type.name() + "/" + TFCMRock.PLACEHOLDER.name() + "_slab", () -> type.createSlab(TFCMRock.PLACEHOLDER)), register("rock/" + type.name() + "/" + TFCMRock.PLACEHOLDER.name() + "_stairs", () -> type.createStairs(TFCMRock.PLACEHOLDER)), register("rock/" + type.name() + "/" + TFCMRock.PLACEHOLDER.name() + "_wall", () -> type.createWall(TFCMRock.PLACEHOLDER))));
        PLACEHOLDER_ROCK_ANVIL = register("rock/anvil/" + TFCMRock.PLACEHOLDER.name(), () -> new RockAnvilBlock(ExtendedProperties.of(Material.STONE).sound(SoundType.STONE).strength(2.0F, 10.0F).requiresCorrectToolForDrops().blockEntity(TFCBlockEntities.ANVIL), PLACEHOLDER_ROCK_BLOCKS.get(Rock.BlockType.RAW)));
        PLACEHOLDER_MAGMA_BLOCK = register("rock/magma/" + TFCMRock.PLACEHOLDER.name(), () -> new TFCMagmaBlock(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.NETHER).requiresCorrectToolForDrops().lightLevel((s) -> 6).randomTicks().strength(0.5F).isValidSpawn((state, level, pos, type) -> type.fireImmune()).hasPostProcess(TFCBlocks::always)));
        PLACEHOLDER_SOIL = Helpers.mapOfKeys(SoilBlockType.class, type -> register(type.name() + "/" + TFCMSoil.PLACEHOLDER.name(), () -> type.create(TFCMSoil.PLACEHOLDER)));
    }

    private static <T extends Block> RegistryObject<T> register(String name, Supplier<T> blockSupplier)
    {
        final String actualName = name.toLowerCase(Locale.ROOT);
        final RegistryObject<T> block = BLOCKS.register(actualName, blockSupplier);
        TFCMItems.ITEMS.register(actualName, () -> new BlockItem(block.get(), new Item.Properties().tab(TFCMCreativeTab.TFC_MINECOLONIES)));
        return block;
    }

    private static <T extends Block> RegistryObject<T> register(String name, Supplier<T> blockSupplier, @Nullable Function<T, ? extends BlockItem> blockItemFactory)
    {
        final String actualName = name.toLowerCase(Locale.ROOT);
        final RegistryObject<T> block = BLOCKS.register(actualName, blockSupplier);
        if (blockItemFactory != null)
        {
            TFCMItems.ITEMS.register(actualName, () -> blockItemFactory.apply(block.get()));
        }

        return block;
    }
}