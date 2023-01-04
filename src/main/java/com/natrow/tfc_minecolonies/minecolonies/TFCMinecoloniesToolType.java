package com.natrow.tfc_minecolonies.minecolonies;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import com.minecolonies.api.util.constant.IToolType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

public enum TFCMinecoloniesToolType implements IToolType
{
    SCYTHE("scythe", true, new TextComponent("Scythe"));

    private static final Map<String, IToolType> tools = new HashMap<>();

    static
    {
        for (final TFCMinecoloniesToolType type : values())
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

    TFCMinecoloniesToolType(final String name, final boolean hasVariableMaterials, final Component displayName)
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
