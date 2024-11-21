package com.example.examplemod
import com.mojang.logging.LogUtils
import net.minecraft.client.Minecraft
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.world.food.FoodProperties
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.CreativeModeTab.ItemDisplayParameters
import net.minecraft.world.item.CreativeModeTabs
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.material.MapColor
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.IEventBus
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.ModContainer
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.fml.common.Mod
import net.neoforged.fml.config.ModConfig
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent
import net.neoforged.neoforge.event.server.ServerStartingEvent
import net.neoforged.neoforge.registries.DeferredBlock
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredItem
import net.neoforged.neoforge.registries.DeferredRegister
import java.util.function.Consumer
import java.util.function.Supplier

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(ExampleMod.MODID)
class ExampleMod {
    companion object  {
        // Define mod id in a common place for everything to reference
        const val MODID = "examplemod"
        // Directly reference a slf4j logger
        private val LOGGER = LogUtils.getLogger()

        // Create a Deferred Register to hold Blocks which will all be registered under the "examplemod" namespace
        val BLOCKS: DeferredRegister.Blocks = DeferredRegister.createBlocks(MODID)
        // Create a Deferred Register to hold Items which will all be registered under the "examplemod" namespace
        val ITEMS: DeferredRegister.Items = DeferredRegister.createItems(MODID)
        // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "examplemod" namespace
        val CREATIVE_MODE_TABS: DeferredRegister<CreativeModeTab> = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID)

        // Creates a new Block with the id "examplemod:example_block", combining the namespace and path
        val EXAMPLE_BLOCK: DeferredBlock<Block> = BLOCKS.registerSimpleBlock("example_block", BlockBehaviour.Properties.of().mapColor(MapColor.STONE))
        // Creates a new BlockItem with the id "examplemod:example_block", combining the namespace and path
        val EXAMPLE_BLOCK_ITEM: DeferredItem<BlockItem> = ITEMS.registerSimpleBlockItem("example_block", EXAMPLE_BLOCK)

        // Creates a new food item with the id "examplemod:example_id", nutrition 1 and saturation 2
        val EXAMPLE_ITEM: DeferredItem<Item> = ITEMS.registerSimpleItem("example_item", Item.Properties().food(FoodProperties.Builder().alwaysEdible().nutrition(1).saturationModifier(2f).build()))

        // Creates a creative tab with the id "examplemod:example_tab" for the example item, that is placed after the combat tab
        val EXAMPLE_TAB: DeferredHolder<CreativeModeTab, CreativeModeTab> = CREATIVE_MODE_TABS.register("example_tab", Supplier {
            CreativeModeTab.builder().title(Component.translatable("itemGroup.examplemod")).withTabsBefore(CreativeModeTabs.COMBAT).icon { //The language key for the title of your CreativeModeTab
                EXAMPLE_ITEM.get().defaultInstance
            }.displayItems { parameters: ItemDisplayParameters?, output: CreativeModeTab.Output ->
                output.accept(EXAMPLE_ITEM.get()) // Add the example item to the tab. For your own tabs, this method is preferred over the event
            }.build()
        })

        // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
        @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD, value = [Dist.CLIENT])
        object ClientModEvents {
            @SubscribeEvent
            fun onClientSetup(event: FMLClientSetupEvent?) {
                // Some client setup code
                LOGGER.info("HELLO FROM CLIENT SETUP")
                LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().user.name)
            }
        }
    }

    constructor(modEventBus: IEventBus, modContainer: ModContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(::commonSetup)

        // Register the Deferred Register to the mod event bus so blocks get registered
        BLOCKS.register(modEventBus)
        // Register the Deferred Register to the mod event bus so items get registered
        ITEMS.register(modEventBus)
        // Register the Deferred Register to the mod event bus so tabs get registered
        CREATIVE_MODE_TABS.register(modEventBus)

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (ExampleMod) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this)

        // Register the item to a creative tab
        modEventBus.addListener(::addCreative)

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC)
    }

    private fun commonSetup(event: FMLCommonSetupEvent) {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP")

        if (Config.logDirtBlock) LOGGER.info("DIRT BLOCK >> {}", BuiltInRegistries.BLOCK.getKey(Blocks.DIRT))

        LOGGER.info(Config.magicNumberIntroduction + Config.magicNumber)

        Config.items.forEach(Consumer { item: Item ->
            LOGGER.info("ITEM >> {}", item.toString())
        })
    }

    // Add the example block item to the building blocks tab
    private fun addCreative(event: BuildCreativeModeTabContentsEvent) {
        if (event.tabKey === CreativeModeTabs.BUILDING_BLOCKS) event.accept(EXAMPLE_BLOCK_ITEM)
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    fun onServerStarting(event: ServerStartingEvent) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting")
    }
}

