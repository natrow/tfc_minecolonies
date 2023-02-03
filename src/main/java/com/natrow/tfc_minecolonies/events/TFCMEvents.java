package com.natrow.tfc_minecolonies.events;

import java.io.IOException;
import java.nio.file.Path;
import com.mojang.logging.LogUtils;
import com.natrow.tfc_minecolonies.TFCMConstants;
import javax.annotation.Nonnull;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.locating.IModFile;
import net.minecraftforge.resource.PathResourcePack;
import org.slf4j.Logger;

public class TFCMEvents
{
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void init(IEventBus bus)
    {
        bus.addListener(TFCMEvents::onPackFinder);
    }

    private static void onPackFinder(AddPackFindersEvent event)
    {
        try
        {
            if (event.getPackType() == PackType.CLIENT_RESOURCES)
            {
                IModFile modFile = ModList.get().getModFileById(TFCMConstants.MOD_ID).getFile();
                Path filePath = modFile.getFilePath();
                PathResourcePack pack = new PathResourcePack(modFile.getFileName() + ":overload", filePath)
                {
                    @Nonnull
                    @Override
                    protected Path resolve(@Nonnull String... paths)
                    {
                        return modFile.findResource(paths);
                    }
                };

                PackMetadataSection metadata = pack.getMetadataSection(PackMetadataSection.SERIALIZER);
                if(metadata != null)
                {
                    LOGGER.info("Injecting TFCM override pack...");
                    event.addRepositorySource((consumer, constructor) ->
                        consumer.accept(constructor.create("builtin/tfcm_data", new TextComponent("TFCM Resources"), true, () -> pack, metadata, Pack.Position.TOP, PackSource.BUILT_IN, false)));
                }
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
}
