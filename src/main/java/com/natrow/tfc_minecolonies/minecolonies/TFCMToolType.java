package com.natrow.tfc_minecolonies.minecolonies;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import com.minecolonies.api.util.constant.IToolType;
import com.natrow.tfc_minecolonies.TFCMTranslationConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

/**
 * Extend minecolonies tool types with TFC-specific tools
 */
public enum TFCMToolType implements IToolType
{
    SCYTHE("scythe", true, new TranslatableComponent(TFCMTranslationConstants.SCYTHE));

    private static final Map<String, IToolType> tools = new HashMap<>();

    static
    {
        for (final TFCMToolType type : values())
        {
            tools.put(type.getName(), type);
        }
    }

    /**
     * Returns a TFC tool type if one exists.
     */
    public static Optional<IToolType> getToolType(final String tool)
    {
        if (tools.containsKey(tool))
        {
            return Optional.of(tools.get(tool));
        }
        else
        {
            return Optional.empty();
        }
    }

    private final String name;
    private final boolean hasVariableMaterials;
    private final Component displayName;

    TFCMToolType(final String name, final boolean hasVariableMaterials, final Component displayName)
    {
        this.name = name;
        this.hasVariableMaterials = hasVariableMaterials;
        this.displayName = displayName;
    }

    @Override
    public String getName() {return name;}

    @Override
    public boolean hasVariableMaterials() {return hasVariableMaterials;}

    @Override
    public Component getDisplayName() {return displayName;}
}
