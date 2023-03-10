package com.natrow.tfc_minecolonies.minecolonies;

import net.minecraft.world.level.Level;

/**
 * @see com.natrow.tfc_minecolonies.mixin.TreeMixin
 */
public interface TFCMTreeExtension
{
    void recalcLeaves(Level level);

    int recalcWood(Level level);
}
