package com.natrow.tfc_minecolonies.minecolonies;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.TriPredicate;

import net.dries007.tfc.util.climate.ClimateRange;

public interface IFarmerExtension
{

    /**
     * Checks whether every block of a field has a certain property.
     *
     * @param fieldPos  location of field to check
     * @param predicate should return true if block passes
     * @return returns true if predicate passes for every block in field (or if field is invalid)
     */
    boolean checkField(BlockPos fieldPos, TriPredicate<BlockPos, ClimateRange, Level> predicate);
}
