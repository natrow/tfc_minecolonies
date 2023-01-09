package com.natrow.tfc_minecolonies.util;

import java.util.List;
import java.util.function.Predicate;
import com.minecolonies.coremod.tileentities.ScarecrowTileEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import net.dries007.tfc.common.blockentities.FarmlandBlockEntity;
import net.dries007.tfc.common.blocks.crop.CropBlock;
import net.dries007.tfc.util.Fertilizer;

public class TFCMCropUtil
{
    /**
     * Determines whether an item stack is valid fertilizer for a given field.
     */
    @Nullable
    public static Predicate<ItemStack> isFertilizer(ScarecrowTileEntity field)
    {
        CropBlock crop = getCrop(field);
        if (crop == null) return null;
        FarmlandBlockEntity.NutrientType nutrient = crop.getPrimaryNutrient();
        return itemStack -> {
            Fertilizer fertilizer = Fertilizer.get(itemStack);
            if (fertilizer == null) return false;
            else return getNutrient(fertilizer, nutrient) > 0.0F;
        };
    }

    /**
     * Finds all valid fertilizer items for a given field
     */
    @Nullable
    public static List<ItemStack> getFertilizers(ScarecrowTileEntity field)
    {
        CropBlock crop = getCrop(field);
        if (crop == null) return null;
        else return getFertilizers(crop, null).stream().map(ItemStack::new).toList();
    }

    /**
     * Retrieves all valid fertilizer items for the given crop, without waste
     */
    public static List<Item> getFertilizers(CropBlock crop, @Nullable FarmlandBlockEntity farmland)
    {
        FarmlandBlockEntity.NutrientType nutrient = crop.getPrimaryNutrient();
        float currentNutrient = farmland == null ? 0.0F : farmland.getNutrient(nutrient);

        return Fertilizer.MANAGER
            .getValues()
            .stream()
            .filter(fertilizer -> {
                float newNutrient = getNutrient(fertilizer, nutrient);
                return newNutrient > 0.0F && newNutrient + currentNutrient <= 1.0F;
            })
            .flatMap(fertilizer -> fertilizer.getValidItems().stream())
            .sorted((item1, item2) -> Float.compare(getNutrient(item1, nutrient), getNutrient(item2, nutrient)))
            .toList();
    }


    /**
     * Helper method to get requested nutrient type from an item
     */
    public static float getNutrient(Item item, FarmlandBlockEntity.NutrientType nutrient)
    {
        Fertilizer fertilizer = Fertilizer.get(new ItemStack(item, 1));
        return fertilizer == null ? 0.0F : getNutrient(fertilizer, nutrient);
    }

    /**
     * Helper method to get nutrient type from a fertilizer definition
     */
    public static float getNutrient(Fertilizer fertilizer, FarmlandBlockEntity.NutrientType nutrient)
    {
        return switch (nutrient)
            {
                case NITROGEN -> fertilizer.getNitrogen();
                case PHOSPHOROUS -> fertilizer.getPhosphorus();
                case POTASSIUM -> fertilizer.getPotassium();
            };
    }

    /**
     * Attempt to get a crop using a field's seed slot
     */
    @Nullable
    public static CropBlock getCrop(ScarecrowTileEntity field)
    {
        ItemStack seed = field.getSeed();
        if (seed != null && !seed.isEmpty())
        {
            Block cropBlock = ((BlockItem) seed.getItem()).getBlock();
            if (cropBlock instanceof CropBlock crop) return crop;
        }
        return null;
    }
}
