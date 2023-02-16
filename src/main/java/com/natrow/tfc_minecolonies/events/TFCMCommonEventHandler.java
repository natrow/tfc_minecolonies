package com.natrow.tfc_minecolonies.events;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;
import com.mojang.logging.LogUtils;
import com.natrow.tfc_minecolonies.TFCMConstants;
import com.natrow.tfc_minecolonies.block.TFCMBlocks;
import com.natrow.tfc_minecolonies.mixin.BlockEntityTypeAccessor;
import javax.annotation.Nonnull;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.forgespi.locating.IModFile;
import net.minecraftforge.resource.PathResourcePack;
import org.slf4j.Logger;

import net.dries007.tfc.common.blockentities.TFCBlockEntities;
import net.dries007.tfc.common.blocks.wood.Wood;

public class TFCMCommonEventHandler
{
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void init(IEventBus bus)
    {
        bus.addListener(TFCMCommonEventHandler::onPackFinder);
        bus.addListener(TFCMCommonEventHandler::onCommonSetup);
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

    private static void onCommonSetup(FMLCommonSetupEvent event)
    {
        extendBlockEntityType(TFCBlockEntities.CHEST.get(), TFCMBlocks.WOODS.get(Wood.BlockType.CHEST).stream());
        extendBlockEntityType(TFCBlockEntities.TRAPPED_CHEST.get(), TFCMBlocks.WOODS.get(Wood.BlockType.TRAPPED_CHEST).stream());
        extendBlockEntityType(TFCBlockEntities.BARREL.get(), TFCMBlocks.WOODS.get(Wood.BlockType.BARREL).stream());
        extendBlockEntityType(TFCBlockEntities.LOOM.get(), TFCMBlocks.WOODS.get(Wood.BlockType.LOOM).stream());
        extendBlockEntityType(TFCBlockEntities.SLUICE.get(), TFCMBlocks.WOODS.get(Wood.BlockType.SLUICE).stream());
        extendBlockEntityType(TFCBlockEntities.TOOL_RACK.get(), TFCMBlocks.WOODS.get(Wood.BlockType.TOOL_RACK).stream());
        extendBlockEntityType(TFCBlockEntities.BOOKSHELF.get(), TFCMBlocks.WOODS.get(Wood.BlockType.BOOKSHELF).stream());

    }

    private static void extendBlockEntityType(BlockEntityType<?> type, Stream<Block> extraBlocks)
    {
        LOGGER.info("Modifying BlockEntityType: " + type.getRegistryName());
        Set<Block> blocks = ((BlockEntityTypeAccessor) type).accessor$getValidBlocks();
        blocks = new HashSet<>(blocks);
        blocks.addAll(extraBlocks.toList());
        ((BlockEntityTypeAccessor) type).accessor$setValidBlocks(blocks);
    }
}
