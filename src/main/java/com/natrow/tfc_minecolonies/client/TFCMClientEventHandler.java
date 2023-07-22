package com.natrow.tfc_minecolonies.client;

import com.natrow.tfc_minecolonies.TFCMConstants;
import com.natrow.tfc_minecolonies.block.TFCMBlocks;
import com.natrow.tfc_minecolonies.block.TFCMWood;
import java.util.Arrays;
import java.util.stream.Stream;
import net.dries007.tfc.client.TFCColors;
import net.dries007.tfc.common.blocks.soil.SoilBlockType;
import net.dries007.tfc.common.blocks.wood.Wood;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public final class TFCMClientEventHandler {
  public static void init(IEventBus bus) {
    bus.addListener(TFCMClientEventHandler::clientSetup);
    bus.addListener(TFCMClientEventHandler::colorHandlerBlocks);
    bus.addListener(TFCMClientEventHandler::colorHandlerItems);
    bus.addListener(TFCMClientEventHandler::onTextureSwitch);
    bus.addListener(TFCMClientEventHandler::registerLayerDefinitions);
  }

  public static void clientSetup(FMLClientSetupEvent event) {
    event.enqueueWork(
        () -> {
          Stream.of(
                  Wood.BlockType.SAPLING,
                  Wood.BlockType.DOOR,
                  Wood.BlockType.TRAPDOOR,
                  Wood.BlockType.FENCE,
                  Wood.BlockType.FENCE_GATE,
                  Wood.BlockType.BUTTON,
                  Wood.BlockType.PRESSURE_PLATE,
                  Wood.BlockType.SLAB,
                  Wood.BlockType.STAIRS,
                  Wood.BlockType.TWIG,
                  Wood.BlockType.BARREL,
                  Wood.BlockType.SCRIBING_TABLE,
                  Wood.BlockType.POTTED_SAPLING)
              .forEach(
                  type ->
                      ItemBlockRenderTypes.setRenderLayer(
                          TFCMBlocks.PLACEHOLDER_WOODS.get(type).get(), RenderType.cutout()));
          Stream.of(Wood.BlockType.LEAVES, Wood.BlockType.FALLEN_LEAVES)
              .forEach(
                  type ->
                      ItemBlockRenderTypes.setRenderLayer(
                          TFCMBlocks.PLACEHOLDER_WOODS.get(type).get(),
                          layer ->
                              Minecraft.useFancyGraphics()
                                  ? layer == RenderType.cutoutMipped()
                                  : layer == RenderType.solid()));
          Stream.of(SoilBlockType.GRASS, SoilBlockType.CLAY_GRASS)
              .forEach(
                  type ->
                      ItemBlockRenderTypes.setRenderLayer(
                          TFCMBlocks.PLACEHOLDER_SOIL.get(type).get(), RenderType.cutoutMipped()));
        });
  }

  public static void colorHandlerBlocks(ColorHandlerEvent.Block event) {
    final BlockColors registry = event.getBlockColors();
    final BlockColor foliageColor =
        (state, level, pos, tintIndex) -> TFCColors.getFoliageColor(pos, tintIndex);
    final BlockColor grassColor =
        (state, level, pos, tintIndex) -> TFCColors.getGrassColor(pos, tintIndex);

    registry.register(
        foliageColor,
        TFCMBlocks.PLACEHOLDER_WOODS.get(Wood.BlockType.LEAVES).get(),
        TFCMBlocks.PLACEHOLDER_WOODS.get(Wood.BlockType.FALLEN_LEAVES).get());

    registry.register(
        grassColor,
        TFCMBlocks.PLACEHOLDER_SOIL.get(SoilBlockType.GRASS).get(),
        TFCMBlocks.PLACEHOLDER_SOIL.get(SoilBlockType.CLAY_GRASS).get());
  }

  public static void colorHandlerItems(ColorHandlerEvent.Item event) {
    final ItemColors registry = event.getItemColors();
    final ItemColor foliageColor = (stack, tintIndex) -> TFCColors.getFoliageColor(null, tintIndex);

    registry.register(
        foliageColor,
        TFCMBlocks.PLACEHOLDER_WOODS.get(Wood.BlockType.FALLEN_LEAVES).get(),
        TFCMBlocks.PLACEHOLDER_WOODS.get(Wood.BlockType.LEAVES).get());
  }

  public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
    LayerDefinition signLayer = SignRenderer.createSignLayer();
    event.registerLayerDefinition(
        new ModelLayerLocation(
            TFCMConstants.getResourceLocation("sign/" + TFCMWood.PLACEHOLDER.getSerializedName()),
            "main"),
        () -> signLayer);
  }

  public static void onTextureSwitch(TextureStitchEvent.Pre event) {
    final ResourceLocation sheet = event.getAtlas().location();
    if (sheet.equals(Sheets.CHEST_SHEET)) {
      Arrays.stream(TFCMWood.VALUES)
          .map(TFCMWood::getSerializedName)
          .forEach(
              name -> {
                event.addSprite(TFCMConstants.getResourceLocation("entity/chest/normal/" + name));
                event.addSprite(
                    TFCMConstants.getResourceLocation("entity/chest/normal_left/" + name));
                event.addSprite(
                    TFCMConstants.getResourceLocation("entity/chest/normal_right/" + name));
                event.addSprite(TFCMConstants.getResourceLocation("entity/chest/trapped/" + name));
                event.addSprite(
                    TFCMConstants.getResourceLocation("entity/chest/trapped_left/" + name));
                event.addSprite(
                    TFCMConstants.getResourceLocation("entity/chest/trapped_right/" + name));
              });
    } else if (sheet.equals(Sheets.SIGN_SHEET)) {
      Arrays.stream(TFCMWood.VALUES)
          .map(TFCMWood::getSerializedName)
          .forEach(
              name -> event.addSprite(TFCMConstants.getResourceLocation("entity/signs/" + name)));
    }
  }
}
