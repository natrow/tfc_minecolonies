package com.natrow.tfcm.datagen

import com.natrow.tfcm.*
import net.minecraft.data.PackOutput
import net.minecraftforge.common.data.LanguageProvider

/**
 * Generates the en_us lang file for the TFCM mod
 */
class TFCMLanguageProvider(output: PackOutput) : LanguageProvider(output, TFCM.ID, "en_us") {
    override fun addTranslations() {
        // Requests
        this.add(REQUEST_STICKS, "Sticks")
        // Interactions
        this.add(
            CROP_TOO_DRY,
            climateInteraction("the soil is too dry", "irrigating the field")
        )
        this.add(
            CROP_TOO_WET,
            climateInteraction("the soil is too damp", "removing irrigation")
        )
        this.add(
            CROP_TOO_COLD,
            climateInteraction("the weather is too cold", null)
        )
        this.add(
            CROP_TOO_HOT,
            climateInteraction("the weather is too hot", null)
        )
        this.add(
            CROP_TOO_TALL,
            climateInteraction("there isn't enough room", "removing blocks above the soil")
        )
        this.add(
            CROP_NO_WATER,
            climateInteraction("they only grow in water", "flooding the field")
        )
    }

    private fun climateInteraction(reason: String, suggestion: String?): String {
        return "I can't plant %s in the field at (%d, %d, %d) because $reason. Try " + if (suggestion != null) {
            "$suggestion or "
        } else {
            ""
        } + "growing something else."
    }
}