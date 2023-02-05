package com.natrow.tfc_minecolonies.mixin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.ldtteam.blockui.views.DropDownList;
import com.ldtteam.structurize.client.gui.AbstractWindowSkeleton;
import com.ldtteam.structurize.client.gui.WindowBuildTool;
import com.ldtteam.structurize.helpers.Settings;
import com.natrow.tfc_minecolonies.TFCMConstants;
import com.natrow.tfc_minecolonies.structurize.ISettingsExtension;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.dries007.tfc.common.blocks.rock.Rock;
import net.dries007.tfc.common.blocks.soil.SoilBlockType;
import net.dries007.tfc.common.blocks.wood.Wood;

@Mixin(value = WindowBuildTool.class, remap = false)
public abstract class WindowBuildToolMixin extends AbstractWindowSkeleton
{
    private List<String> woodTypes = new ArrayList<>();
    private List<String> stoneTypes = new ArrayList<>();
    private List<String> soilTypes = new ArrayList<>();

    private DropDownList woodTypesDropDownList;
    private DropDownList stoneTypesDropDownList;
    private DropDownList soilTypesDropDownList;

    /**
     * Dummy constructor
     */
    private WindowBuildToolMixin(String resource)
    {
        super(resource);
    }

    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lcom/ldtteam/structurize/client/gui/WindowBuildTool;initBuildingTypeNavigation()V"))
    private void initInjector(BlockPos pos, int rot, int groundstyle, CallbackInfo ci)
    {
        initWoodTypesNavigation();
        initStoneTypesNavigation();
        initSoilTypesNavigation();
        updateMaterialTypes();
    }

    private void initWoodTypesNavigation()
    {
        registerButton(TFCMConstants.BUTTON_PREVIOUS_WOOD_ID, this::previousWoodType);
        registerButton(TFCMConstants.BUTTON_NEXT_WOOD_ID, this::nextWoodType);
        woodTypesDropDownList = findPaneOfTypeByID(TFCMConstants.DROPDOWN_WOOD_ID, DropDownList.class);
        woodTypesDropDownList.setHandler(this::onDropDownSelect);
        woodTypesDropDownList.setDataProvider(new DropDownList.DataProvider()
        {
            @Override
            public int getElementCount() {return woodTypes.size();}

            @Override
            public String getLabel(int index)
            {
                if (index >= 0 && index < woodTypes.size())
                {
                    return woodTypes.get(index);
                }
                return "";
            }
        });
    }

    private void initStoneTypesNavigation()
    {
        registerButton(TFCMConstants.BUTTON_PREVIOUS_STONE_ID, this::previousStoneType);
        registerButton(TFCMConstants.BUTTON_NEXT_STONE_ID, this::nextStoneType);
        stoneTypesDropDownList = findPaneOfTypeByID(TFCMConstants.DROPDOWN_STONE_ID, DropDownList.class);
        stoneTypesDropDownList.setHandler(this::onDropDownSelect);
        stoneTypesDropDownList.setDataProvider(new DropDownList.DataProvider()
        {
            @Override
            public int getElementCount() {return stoneTypes.size();}

            @Override
            public String getLabel(int index)
            {
                if (index >= 0 && index < stoneTypes.size())
                {
                    return stoneTypes.get(index);
                }
                return "";
            }
        });
    }

    private void initSoilTypesNavigation()
    {
        registerButton(TFCMConstants.BUTTON_PREVIOUS_SOIL_ID, this::previousSoilType);
        registerButton(TFCMConstants.BUTTON_NEXT_SOIL_ID, this::nextSoilType);
        soilTypesDropDownList = findPaneOfTypeByID(TFCMConstants.DROPDOWN_SOIL_ID, DropDownList.class);
        soilTypesDropDownList.setHandler(this::onDropDownSelect);
        soilTypesDropDownList.setDataProvider(new DropDownList.DataProvider()
        {
            @Override
            public int getElementCount() {return soilTypes.size();}

            @Override
            public String getLabel(int index)
            {
                if (index >= 0 && index < soilTypes.size())
                {
                    return soilTypes.get(index);
                }
                return "";
            }
        });
    }

    /**
     * Change to the next wood type
     */
    private void nextWoodType()
    {
        woodTypesDropDownList.selectNext();
    }

    /**
     * Change to the previous wood type
     */
    private void previousWoodType()
    {
        woodTypesDropDownList.selectPrevious();
    }

    /**
     * Change to the next stone type
     */
    private void nextStoneType()
    {
        stoneTypesDropDownList.selectNext();
    }

    /**
     * Change to the previous stone type
     */
    private void previousStoneType()
    {
        stoneTypesDropDownList.selectPrevious();
    }

    /**
     * Change to the next soil type
     */
    private void nextSoilType()
    {
        soilTypesDropDownList.selectNext();
    }

    /**
     * Change to the previous soil type
     */
    private void previousSoilType()
    {
        soilTypesDropDownList.selectPrevious();
    }

    /**
     * Called whenever the material dropdown selections are changed.
     */
    private void onDropDownSelect(final DropDownList list)
    {
        ISettingsExtension settings = ((ISettingsExtension) (Object) Settings.instance);
        if (list == woodTypesDropDownList)
        {
            settings.setWoodType(woodTypes.get(list.getSelectedIndex()));
        }
        else if (list == stoneTypesDropDownList)
        {
            settings.setStoneType(stoneTypes.get(list.getSelectedIndex()));
        }
        else if (list == soilTypesDropDownList)
        {
            settings.setSoilType(soilTypes.get(list.getSelectedIndex()));
        }
    }

    private void updateMaterialTypes()
    {
        woodTypes = Arrays.stream(Wood.values()).map(Enum::toString).toList();
        stoneTypes = Arrays.stream(Rock.values()).map(Enum::toString).toList();
        soilTypes = Arrays.stream(SoilBlockType.Variant.values()).map(Enum::toString).toList();

        ISettingsExtension settings = ((ISettingsExtension) (Object) Settings.instance);

        if(settings.getWoodType() != null && woodTypes.contains(settings.getWoodType()))
        {
            woodTypesDropDownList.setSelectedIndex(woodTypes.indexOf(settings.getWoodType()));
        }
        else
        {
            woodTypesDropDownList.setSelectedIndex(0);
        }

        if(settings.getStoneType() != null && stoneTypes.contains(settings.getStoneType()))
        {
            stoneTypesDropDownList.setSelectedIndex(stoneTypes.indexOf(settings.getStoneType()));
        }
        else
        {
            stoneTypesDropDownList.setSelectedIndex(0);
        }

        if(settings.getSoilType() != null && soilTypes.contains(settings.getSoilType()))
        {
            soilTypesDropDownList.setSelectedIndex(soilTypes.indexOf(settings.getSoilType()));
        }
        else
        {
            soilTypesDropDownList.setSelectedIndex(0);
        }
    }
}
