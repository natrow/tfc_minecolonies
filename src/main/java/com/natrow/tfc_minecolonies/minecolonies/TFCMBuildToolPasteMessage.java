package com.natrow.tfc_minecolonies.minecolonies;

import java.util.function.Predicate;
import com.ldtteam.structurize.management.StructureName;
import com.ldtteam.structurize.management.Structures;
import com.ldtteam.structurize.placement.StructurePlacementUtils;
import com.minecolonies.api.advancements.AdvancementTriggers;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.IRSComponent;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.colony.workorders.WorkOrderType;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.CompatibilityUtils;
import com.minecolonies.api.util.CreativeBuildingStructureHandler;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.workorders.WorkOrderBuilding;
import com.minecolonies.coremod.entity.ai.citizen.builder.ConstructionTapeHelper;
import com.minecolonies.coremod.event.EventHandler;
import com.minecolonies.coremod.util.BuildingUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.minecolonies.api.util.constant.Constants.*;
import static com.minecolonies.api.util.constant.TranslationConstants.*;
import static com.minecolonies.api.util.constant.translation.ProgressTranslationConstants.*;

/**
 * Send build tool data to the server. Verify the data on the server side and then place the building.
 * This has been extended to include TFCM specific data about material types.
 */
public class TFCMBuildToolPasteMessage implements IMessage
{
    /**
     * The state at the offset position.
     */
    private BlockState state;

    private boolean complete;
    private String structureName;
    private String workOrderName;
    private int rotation;
    private BlockPos pos;
    private boolean isHut;
    private boolean mirror;
    private String woodType;
    private String stoneType;
    private String soilType;

    /**
     * Empty constructor used when registering the
     */
    public TFCMBuildToolPasteMessage()
    {
        super();
    }

    /**
     * Create the building that was made with the build tool. Item in inventory required
     *
     * @param structureName String representation of a structure
     * @param workOrderName String name of the work order
     * @param pos           BlockPos
     * @param rotation      int representation of the rotation
     * @param isHut         true if hut, false if decoration
     * @param mirror        the mirror of the building or decoration.
     * @param complete      paste it complete (with structure blocks) or without.
     * @param state         the state.
     * @param woodType      the type of wood to use
     * @param stoneType     the type of stone to use
     * @param soilType      the type of soil to use
     */
    public TFCMBuildToolPasteMessage(final String structureName, final String workOrderName, final BlockPos pos, final int rotation, final boolean isHut, final Mirror mirror, final boolean complete, final BlockState state, final String woodType, final String stoneType, final String soilType)
    {
        super();
        this.structureName = structureName;
        this.workOrderName = workOrderName;
        this.pos = pos;
        this.rotation = rotation;
        this.isHut = isHut;
        this.mirror = mirror == Mirror.FRONT_BACK;
        this.complete = complete;
        this.state = state;
        this.woodType = woodType;
        this.stoneType = stoneType;
        this.soilType = soilType;
    }

    /**
     * Reads this packet from a {@link FriendlyByteBuf}.
     *
     * @param buf The buffer begin read from.
     */
    @Override
    public void fromBytes(@NotNull final FriendlyByteBuf buf)
    {
        structureName = buf.readUtf(32767);
        workOrderName = buf.readUtf(32767);

        pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());

        rotation = buf.readInt();

        isHut = buf.readBoolean();

        mirror = buf.readBoolean();

        complete = buf.readBoolean();

        state = Block.stateById(buf.readInt());

        woodType = buf.readUtf(32767);
        stoneType = buf.readUtf(32767);
        soilType = buf.readUtf(32767);
    }

    /**
     * Writes this packet to a {@link FriendlyByteBuf}.
     *
     * @param buf The buffer being written to.
     */
    @Override
    public void toBytes(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeUtf(structureName);
        buf.writeUtf(workOrderName);

        buf.writeInt(pos.getX());
        buf.writeInt(pos.getY());
        buf.writeInt(pos.getZ());

        buf.writeInt(rotation);

        buf.writeBoolean(isHut);

        buf.writeBoolean(mirror);

        buf.writeBoolean(complete);

        buf.writeInt(Block.getId(state));

        buf.writeUtf(woodType);
        buf.writeUtf(stoneType);
        buf.writeUtf(soilType);
    }

    @Nullable
    @Override
    public LogicalSide getExecutionSide()
    {
        return LogicalSide.SERVER;
    }

    @Override
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer)
    {
        final StructureName sn = new StructureName(structureName);
        final ServerPlayer player = ctxIn.getSender();
        if (!Structures.hasMD5(sn))
        {
            MessageUtils.format(new TextComponent("Can not build " + workOrderName + ": schematic missing!")).sendTo(player);
            return;
        }

        if (player.isCreative())
        {
            if (isHut)
            {
                handleHut(CompatibilityUtils.getWorldFromEntity(player), player, sn, rotation, pos, mirror, state, complete, woodType, stoneType, soilType);
                CreativeBuildingStructureHandler.loadAndPlaceStructureWithRotation(player.level, structureName, pos, BlockPosUtil.getRotationFromRotations(rotation), mirror ? Mirror.FRONT_BACK : Mirror.NONE, !complete, player);

                @Nullable final IBuilding building = IColonyManager.getInstance().getBuilding(CompatibilityUtils.getWorldFromEntity(player), pos);
                if (building != null)
                {
                    final WorkOrderBuilding workOrder = WorkOrderBuilding.create(WorkOrderType.BUILD, building);
                    ConstructionTapeHelper.removeConstructionTape(workOrder, CompatibilityUtils.getWorldFromEntity(player));
                }
            }
            else
            {
                StructurePlacementUtils.loadAndPlaceStructureWithRotation(ctxIn.getSender().level, structureName, pos, BlockPosUtil.getRotationFromRotations(rotation), mirror ? Mirror.FRONT_BACK : Mirror.NONE, !complete, ctxIn.getSender());
            }
        }
        else if (structureName.contains("supply"))
        {
            if (player.getStats().getValue(Stats.ITEM_USED.get(ModItems.supplyChest)) > 0 && !MineColonies.getConfig().getServer().allowInfiniteSupplyChests.get() && !isFreeInstantPlacementMH(player))
            {
                MessageUtils.format(WARNING_SUPPLY_CHEST_ALREADY_PLACED).sendTo(player);
                return;
            }

            Predicate<ItemStack> searchPredicate = stack -> !stack.isEmpty();
            if (structureName.contains("supplyship"))
            {
                searchPredicate = searchPredicate.and(stack -> ItemStackUtils.compareItemStacksIgnoreStackSize(stack, new ItemStack(ModItems.supplyChest), true, false));
            }
            if (structureName.contains("supplycamp"))
            {
                searchPredicate = searchPredicate.and(stack -> ItemStackUtils.compareItemStacksIgnoreStackSize(stack, new ItemStack(ModItems.supplyCamp), true, false));
            }

            if (isFreeInstantPlacementMH(player))
            {
                searchPredicate = searchPredicate.and(stack -> stack.hasTag() && stack.getTag().get(PLACEMENT_NBT) != null && stack.getTag().getString(PLACEMENT_NBT).equals(INSTANT_PLACEMENT));
            }

            final int slot = InventoryUtils.findFirstSlotInItemHandlerNotEmptyWith(new InvWrapper(player.getInventory()), searchPredicate);

            if (slot != -1 && !ItemStackUtils.isEmpty(player.getInventory().removeItemNoUpdate(slot)))
            {
                if (player.getStats().getValue(Stats.ITEM_USED.get(ModItems.supplyChest)) < 1)
                {
                    MessageUtils.format(PROGRESS_SUPPLY_CHEST_PLACED).sendTo(player);
                    player.awardStat(Stats.ITEM_USED.get(ModItems.supplyChest), 1);
                    AdvancementTriggers.PLACE_SUPPLY.trigger(player);
                }

                CreativeBuildingStructureHandler.loadAndPlaceStructureWithRotation(player.level, structureName, pos, BlockPosUtil.getRotationFromRotations(rotation), mirror ? Mirror.FRONT_BACK : Mirror.NONE, !complete, player);
            }
            else
            {
                MessageUtils.format(WARNING_REMOVING_SUPPLY_CHEST).sendTo(player);
            }
        }
    }

    /**
     * Whether the itemstack used allows a free placement.
     *
     * @param playerEntity the player to check
     * @return whether the itemstack used allows a free placement.
     */
    private boolean isFreeInstantPlacementMH(ServerPlayer playerEntity)
    {
        final ItemStack mhItem = playerEntity.getMainHandItem();
        return !ItemStackUtils.isEmpty(mhItem) && mhItem.getTag() != null && mhItem.getTag().getString(PLACEMENT_NBT).equals(INSTANT_PLACEMENT);
    }

    /**
     * Handles the placement of huts.
     *
     * @param world     World the hut is being placed into.
     * @param player    Who placed the hut.
     * @param sn        The name of the structure.
     * @param rotation  The number of times the structure should be rotated.
     * @param buildPos  The location the hut is being placed.
     * @param mirror    Whether or not the strcture is mirrored.
     * @param state     The state of the hut.
     * @param complete  If complete or not.
     * @param woodType  The type of wood to use
     * @param stoneType The type of stone to use
     * @param soilType  The type of soil to use
     */
    private static void handleHut(@NotNull final Level world, @NotNull final Player player, final StructureName sn, final int rotation, @NotNull final BlockPos buildPos, final boolean mirror, final BlockState state, final boolean complete, final String woodType, final String stoneType, final String soilType)
    {
        final IColony tempColony = IColonyManager.getInstance().getClosestColony(world, buildPos);
        if (!complete && tempColony != null && !tempColony.getPermissions().hasPermission(player, Action.MANAGE_HUTS) && IColonyManager.getInstance().isFarEnoughFromColonies(world, buildPos))
        {
            return;
        }

        final String hut = sn.getSection();

        final ItemStack stack = BuildingUtils.getItemStackForHutFromInventory(player.getInventory(), hut);
        final Block block = stack.getItem() instanceof BlockItem ? ((BlockItem) stack.getItem()).getBlock() : null;
        if (block != null && EventHandler.onBlockHutPlaced(world, player, block, buildPos))
        {
            world.destroyBlock(buildPos, true);
            world.setBlockAndUpdate(buildPos, state);
            if (!complete)
            {
                ((IAbstractBlockHutExtension) block).onBlockPlacedByBuildTool(world, buildPos, world.getBlockState(buildPos), player, null, mirror, sn.getStyle(), woodType, stoneType, soilType);
                setupBuilding(world, player, sn, rotation, buildPos, mirror);
            }
        }
    }

    /**
     * setup the building once it has been placed.
     *
     * @param world    World the hut is being placed into.
     * @param player   Who placed the hut.
     * @param sn       The name of the structure.
     * @param rotation The number of times the structure should be rotated.
     * @param buildPos The location the hut is being placed.
     * @param mirror   Whether or not the strcture is mirrored.
     */
    private static void setupBuilding(@NotNull final Level world, @NotNull final Player player, final StructureName sn, final int rotation, @NotNull final BlockPos buildPos, final boolean mirror)
    {
        @Nullable final IBuilding building = IColonyManager.getInstance().getBuilding(world, buildPos);

        if (building == null)
        {
            Log.getLogger().error("BuildTool: building is null!", new Exception());
        }
        else
        {
            if (building.getTileEntity() != null)
            {
                final IColony colony = IColonyManager.getInstance().getColonyByPosFromWorld(world, buildPos);
                if (colony == null)
                {
                    Log.getLogger().info("No colony for " + player.getName().getString());
                }
                else
                {
                    building.getTileEntity().setColony(colony);
                }
            }

            // Don't set the building level here; that will be set later in
            // readSchematicDataFromNBT (provided that the schematic has
            // TAG_BLUEPRINTDATA, but buildings always should).  This allows
            // level 0 -> N upgrade events to properly be triggered on paste.

            building.setStyle(sn.getStyle());

            if (!(building instanceof IRSComponent))
            {
                ConstructionTapeHelper.removeConstructionTape(building.getCorners(), world);
            }

            building.setIsMirrored(mirror);
        }
    }
}