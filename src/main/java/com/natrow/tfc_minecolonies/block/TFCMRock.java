package com.natrow.tfc_minecolonies.block;

import java.util.Locale;
import java.util.function.Supplier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;

import net.dries007.tfc.common.blocks.rock.Rock;
import net.dries007.tfc.common.blocks.rock.RockCategory;
import net.dries007.tfc.common.blocks.soil.SandBlockType;
import net.dries007.tfc.util.registry.RegistryRock;

public enum TFCMRock implements RegistryRock
{
    PLACEHOLDER(RockCategory.IGNEOUS_INTRUSIVE, SandBlockType.YELLOW);
    private final String serializedName;
    private final RockCategory category;
    private final SandBlockType sandType;

    TFCMRock(RockCategory category, SandBlockType sandType)
    {
        this.serializedName = this.name().toLowerCase(Locale.ROOT);
        this.category = category;
        this.sandType = sandType;
    }

    public SandBlockType getSandType()
    {
        return this.sandType;
    }

    public RockCategory category()
    {
        return this.category;
    }

    @Override
    public Supplier<? extends Block> getBlock(Rock.BlockType blockType)
    {
        return TFCMBlocks.PLACEHOLDER_ROCK_BLOCKS.get(blockType);
    }

    @Override
    public Supplier<? extends Block> getAnvil()
    {
        return TFCMBlocks.PLACEHOLDER_ROCK_ANVIL;
    }

    @Override
    public Supplier<? extends SlabBlock> getSlab(Rock.BlockType blockType)
    {
        return TFCMBlocks.PLACEHOLDER_ROCK_DECORATIONS.get(blockType).slab();
    }

    @Override
    public Supplier<? extends StairBlock> getStair(Rock.BlockType blockType)
    {
        return TFCMBlocks.PLACEHOLDER_ROCK_DECORATIONS.get(blockType).stair();
    }

    @Override
    public Supplier<? extends WallBlock> getWall(Rock.BlockType blockType)
    {
        return TFCMBlocks.PLACEHOLDER_ROCK_DECORATIONS.get(blockType).wall();
    }

    @Override
    public String getSerializedName()
    {
        return this.serializedName;
    }
}
