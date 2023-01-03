package com.natrow.tfc_minecolonies.mixin;

import com.minecolonies.api.util.ItemStackUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to change the names of different tool grades for Minecolonies' worker requests to match TFC item tiers.
 */
@Mixin(value = ItemStackUtils.class, remap = false)
public abstract class TierNamesMixin
{
    /**
     * Get name from a given tool tier
     *
     * @param toolGrade item tier
     * @param cir       callback info
     */
    @Inject(method = "swapToolGrade", at = @At("HEAD"), cancellable = true)
    private static void swapToolGrade(int toolGrade, CallbackInfoReturnable<String> cir)
    {
        cir.setReturnValue(switch (toolGrade)
            {
                case 0 -> "Stone";
                case 1 -> "Copper";
                case 2 -> "Bronze";
                case 3 -> "Wrought Iron";
                case 4 -> "Steel";
                default -> "Black, Red, or Blue Steel";
            });
    }

    /**
     * Get name from a given armor tier
     *
     * @param toolGrade armor tier
     * @param cir       callback info
     */
    @Inject(method = "swapArmorGrade", at = @At("HEAD"), cancellable = true)
    private static void swapArmorGrade(int toolGrade, CallbackInfoReturnable<String> cir)
    {
        cir.setReturnValue(switch (toolGrade)
            {
                case 0 -> "Leather";
                case 1 -> "Copper";
                case 2 -> "Bronze";
                case 3 -> "Wrought Iron";
                case 4 -> "Steel";
                default -> "Black, Red, or Blue Steel";
            });
    }
}
