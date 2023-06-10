package com.natrow.tfc_minecolonies.block;

import java.util.function.Supplier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import net.dries007.tfc.common.blocks.soil.SoilBlockType;
import net.dries007.tfc.common.items.TFCItems;
import net.dries007.tfc.util.registry.RegistrySoilVariant;

public enum TFCMSoil implements RegistrySoilVariant
{
    PLACEHOLDER;

    @Override
    public Supplier<? extends Block> getBlock(SoilBlockType soilBlockType)
    {
        return TFCMBlocks.PLACEHOLDER_SOIL.get(soilBlockType);
    }

    @Override
    public Supplier<? extends Item> getDriedMudBrick()
    {
        return switch (this)
            {
                case PLACEHOLDER -> TFCItems.SILTY_LOAM_MUD_BRICK;
            };
    }
}
