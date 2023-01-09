package com.natrow.tfc_minecolonies.minecolonies;

import java.util.function.BiPredicate;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.interactionhandling.InteractionValidatorRegistry;
import com.minecolonies.coremod.colony.jobs.JobFarmer;
import com.natrow.tfc_minecolonies.TFCMTranslationConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.TriPredicate;

import net.dries007.tfc.common.blocks.soil.FarmlandBlock;
import net.dries007.tfc.util.climate.Climate;
import net.dries007.tfc.util.climate.ClimateRange;

/**
 * Registers interaction validators. These are used by minecolonies to determine whether a constraint has been satisfied.
 * <p>
 * NOTE: validators should return false when criteria is NOT MET. In other words, the message will stay for as long as the predicate returns TRUE.
 */
public class TFCMInteractionValidatorInitializer
{

    public static void registerValidators()
    {
        InteractionValidatorRegistry.registerPosBasedPredicate(new TranslatableComponent(TFCMTranslationConstants.CROP_TOO_DRY),
            climateValidator((pos, climate, world) -> climate.checkHydration(FarmlandBlock.getHydration(world, pos), false) != ClimateRange.Result.LOW));
        InteractionValidatorRegistry.registerPosBasedPredicate(new TranslatableComponent(TFCMTranslationConstants.CROP_TOO_WET),
            climateValidator((pos, climate, world) -> climate.checkHydration(FarmlandBlock.getHydration(world, pos), false) != ClimateRange.Result.HIGH));
        InteractionValidatorRegistry.registerPosBasedPredicate(new TranslatableComponent(TFCMTranslationConstants.CROP_TOO_COLD),
            climateValidator((pos, climate, world) -> climate.checkTemperature(Climate.getTemperature(world, pos), false) != ClimateRange.Result.LOW));
        InteractionValidatorRegistry.registerPosBasedPredicate(new TranslatableComponent(TFCMTranslationConstants.CROP_TOO_HOT),
            climateValidator((pos, climate, world) -> climate.checkTemperature(Climate.getTemperature(world, pos), false) != ClimateRange.Result.HIGH));
    }

    /**
     * Creates the real predicate by using a proxy one
     *
     * @param predicate checks whether a position in the world satisfies climate constraints
     * @return a predicate that accesses a job & block position then run the proxy predicate
     */
    private static BiPredicate<ICitizenData, BlockPos> climateValidator(TriPredicate<BlockPos, ClimateRange, Level> predicate)
    {
        return (citizen, pos) -> {
            // farmers have to check against the entire field
            if (citizen.getJob() instanceof JobFarmer)
            {
                return !((TFCMFarmerExtension) citizen.getJob().getWorkerAI()).checkField(pos, predicate);
            }
            // unknown job
            return false;
        };
    }
}
