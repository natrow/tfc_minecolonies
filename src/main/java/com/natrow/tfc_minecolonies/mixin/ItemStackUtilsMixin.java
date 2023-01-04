package com.natrow.tfc_minecolonies.mixin;

import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.IToolType;
import com.natrow.tfc_minecolonies.minecolonies.TFCMinecoloniesToolType;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.dries007.tfc.common.items.ScytheItem;

@Mixin(value = ItemStackUtils.class, remap = false)
public abstract class ItemStackUtilsMixin
{
    /**
     * Determine whether a scythe is a valid tool.
     */
    @Inject(method = "isTool", at = @At("TAIL"), cancellable = true)
    private static void isToolInjector(ItemStack itemStack, IToolType toolType, CallbackInfoReturnable<Boolean> cir)
    {
        if (TFCMinecoloniesToolType.SCYTHE.equals(toolType) && itemStack.getItem() instanceof ScytheItem)
        {
            cir.setReturnValue(true);
        }
    }

    /**
     * Get name for a given tool tier
     */
    @Inject(method = "swapToolGrade", at = @At("HEAD"), cancellable = true)
    private static void swapToolGradeInjector(int toolGrade, CallbackInfoReturnable<String> cir)
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
     */
    @Inject(method = "swapArmorGrade", at = @At("HEAD"), cancellable = true)
    private static void swapArmorGradeInjector(int toolGrade, CallbackInfoReturnable<String> cir)
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
