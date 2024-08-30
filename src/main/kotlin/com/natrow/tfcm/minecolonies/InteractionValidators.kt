package com.natrow.tfcm.minecolonies

import com.minecolonies.api.colony.ICitizenData
import com.minecolonies.api.colony.interactionhandling.InteractionValidatorRegistry
import com.minecolonies.core.colony.buildings.modules.FieldsModule
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingFarmer
import com.minecolonies.core.colony.fields.FarmField
import com.minecolonies.core.colony.jobs.JobFarmer
import com.natrow.tfcm.TFCM
import com.natrow.tfcm.mixinimpl.minecolonies.ai.ClimateCheckError
import com.natrow.tfcm.mixinimpl.minecolonies.ai.EntityAIWorkFarmerExt
import com.natrow.tfcm.mixinimpl.minecolonies.ai.checkClimate
import com.natrow.tfcm.mixinimpl.minecolonies.ai.getCrop
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import java.util.function.BiPredicate

/**
 * Registers the interaction validators. These are used by Minecolonies to determine whether an
 * interaction has been completed.
 *
 * Note: Validators return false when they have been completed.
 */
object InteractionValidators {
    init {
        // create a validator for each error type
        for (error in ClimateCheckError.entries) {
            InteractionValidatorRegistry.registerPosBasedPredicate(
                Component.translatable(error.toTranslationConstant()),
                climateValidator(error)
            )
        }

    }

    /**
     * Implementation of the actual validator
     */
    private fun climateValidator(error: ClimateCheckError): BiPredicate<ICitizenData, BlockPos> {
        return BiPredicate p@{ citizen, fieldPos ->
            // get the farmer
            val farmer = citizen.job as? JobFarmer
            if (farmer == null) {
                TFCM.LOGGER.error("Job ${citizen.job} is not a farmer")
                return@p false
            }

            // get the world and AI
            val world = citizen.colony.world
            val ai = (farmer.workerAI as EntityAIWorkFarmerExt)

            // find the field
            val field = (farmer.workBuilding as BuildingFarmer).getModulesByType(FieldsModule::class.java)
                .flatMap { it.ownedFields }.find { it.position == fieldPos && it is FarmField }

            if (field == null) {
                TFCM.LOGGER.error("Couldn't find field at $fieldPos")
                return@p false
            } else {
                field as FarmField
            }

            // find the crop
            val crop = getCrop(field)
            if (crop == null) {
                TFCM.LOGGER.error("Couldn't find crop in field")
                return@p false
            }

            // finally, check the environment
            !ai.checkField(field) { checkClimate(world, it, crop).contains(error) }
        }
    }
}
