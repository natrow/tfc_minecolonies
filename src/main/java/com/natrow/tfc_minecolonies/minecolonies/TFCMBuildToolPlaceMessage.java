package com.natrow.tfc_minecolonies.minecolonies;

import com.ldtteam.structurize.management.StructureName;
import com.ldtteam.structurize.management.Structures;
import com.minecolonies.api.advancements.AdvancementTriggers;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.IRSComponent;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.colony.workorders.WorkOrderType;
import com.minecolonies.api.entity.ai.citizen.builder.IBuilderUndestroyable;
import com.minecolonies.api.items.ModTags;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.CompatibilityUtils;
import com.minecolonies.api.util.CreativeBuildingStructureHandler;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.api.util.SoundUtils;
import com.minecolonies.coremod.blocks.huts.BlockHutTownHall;
import com.minecolonies.coremod.colony.workorders.WorkOrderDecoration;
import com.minecolonies.coremod.entity.ai.citizen.builder.ConstructionTapeHelper;
import com.minecolonies.coremod.event.EventHandler;
import com.minecolonies.coremod.util.AdvancementUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.network.NetworkEvent;
import org.apache.commons.lang3.text.WordUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_COLONY_ID;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_OTHER_LEVEL;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_PASTEABLE;
import static com.minecolonies.api.util.constant.TranslationConstants.INDESTRUCTIBLE_BLOCK_AT_POS;
import static com.minecolonies.api.util.constant.TranslationConstants.NO_HUT_IN_INVENTORY;
import static com.minecolonies.api.util.constant.TranslationConstants.WRONG_COLONY;

/**
 * Send build tool data to the server. Verify the data on the server side and then place the building.
 * Expanded to include TFCM-specific information about material types.
 */
public class TFCMBuildToolPlaceMessage implements IMessage
{

    /**
     * Handles the placement of huts.
     *
     * @param world    World the hut is being placed into.
     * @param player   Who placed the hut.
     * @param sn       The name of the structure.
     * @param rotation The number of times the structure should be rotated.
     * @param buildPos The location the hut is being placed.
     * @param mirror   Whether or not the strcture is mirrored.
     * @param state    the state.
     * @param woodType The type of wood to use
     * @param rockType The type of rock to use
     * @param soilType The type of soil to use
     */
    private static void handleHut(@NotNull final Level world, @NotNull final Player player, final StructureName sn, final int rotation, @NotNull final BlockPos buildPos, final boolean mirror, final BlockState state, final String woodType, final String rockType, final String soilType)
    {
        final BlockState stateAtPos = world.getBlockState(buildPos);
        final Block blockAtPos = stateAtPos.getBlock();
        if (blockAtPos instanceof IBuilderUndestroyable || stateAtPos.is(ModTags.indestructible))
        {
            MessageUtils.format(INDESTRUCTIBLE_BLOCK_AT_POS).sendTo(player);
            SoundUtils.playErrorSound(player, buildPos);
            return;
        }

        final Block block = state.getBlock();
        final ItemStack tempStack = new ItemStack(block, 1);
        final int slot = InventoryUtils.findFirstSlotInItemHandlerWith(new InvWrapper(player.getInventory()), s -> ItemStackUtils.compareItemStacksIgnoreStackSize(tempStack, s, false, false));
        if (slot < 0)
        {
            return;
        }

        final ItemStack stack = player.getInventory().getItem(slot);

        final IColony tempColony = IColonyManager.getInstance().getClosestColony(world, buildPos);
        if (tempColony != null && (!tempColony.getPermissions().hasPermission(player, Action.MANAGE_HUTS) && !(block instanceof BlockHutTownHall && IColonyManager.getInstance().isFarEnoughFromColonies(world, buildPos))))
        {
            return;
        }

        final CompoundTag compound = stack.getTag();
        if (tempColony != null && compound != null && compound.contains(TAG_COLONY_ID) && tempColony.getID() != compound.getInt(TAG_COLONY_ID))
        {
            MessageUtils.format(WRONG_COLONY, compound.getInt(TAG_COLONY_ID)).sendTo(player);
            return;
        }

        if (block != null && player.getInventory().contains(new ItemStack(block)))
        {
            if (EventHandler.onBlockHutPlaced(world, player, block, buildPos))
            {
                if (tempColony != null)
                {
                    AdvancementUtils.TriggerAdvancementPlayersForColony(tempColony, playerMP -> AdvancementTriggers.PLACE_STRUCTURE.trigger(playerMP, sn));
                }
                else
                {
                    AdvancementTriggers.PLACE_STRUCTURE.trigger((ServerPlayer) player, sn);
                }

                world.destroyBlock(buildPos, true);
                world.setBlockAndUpdate(buildPos, state);
                ((IAbstractBlockHutExtension) block).onBlockPlacedByBuildTool(world, buildPos, world.getBlockState(buildPos), player, null, mirror, sn.getStyle(), woodType, rockType, soilType);

                boolean complete = false;
                int level = 0;

                if (compound != null)
                {
                    if (compound.getAllKeys().contains(TAG_OTHER_LEVEL))
                    {
                        level = compound.getInt(TAG_OTHER_LEVEL);
                    }
                    if (compound.getAllKeys().contains(TAG_PASTEABLE))
                    {
                        String schematic = sn.toString();
                        schematic = schematic.substring(0, schematic.length() - 1);
                        schematic += level;
                        CreativeBuildingStructureHandler.loadAndPlaceStructureWithRotation(player.level, schematic, buildPos, BlockPosUtil.getRotationFromRotations(rotation), mirror ? Mirror.FRONT_BACK : Mirror.NONE, true, (ServerPlayer) player);
                        complete = true;
                    }
                }

                InventoryUtils.reduceStackInItemHandler(new InvWrapper(player.getInventory()), stack, 1);
                setupBuilding(world, player, sn, rotation, buildPos, mirror, level, complete);
            }
        }
        else
        {
            MessageUtils.format(NO_HUT_IN_INVENTORY).sendTo(player);
        }
    }

    /**
     * Creates the {@link WorkOrderDecoration} to start building the decoration.
     *
     * @param world         The world the decoration is being built in.
     * @param player        The player who placed the decoration.
     * @param sn            The name of the structure.
     * @param workOrderName The style of the decoration.
     * @param rotation      The number of times the decoration is rotated.
     * @param buildPos      The location the decoration will be built.
     * @param mirror        Whether or not the strcture is mirrored.
     */
    private static void handleDecoration(@NotNull final Level world, @NotNull final Player player, final StructureName sn, final String workOrderName, final int rotation, @NotNull final BlockPos buildPos, final boolean mirror, BlockPos builder)
    {
        @Nullable final IColony colony = IColonyManager.getInstance().getColonyByPosFromWorld(world, buildPos);
        if (colony != null && colony.getPermissions().hasPermission(player, Action.PLACE_HUTS))
        {
            String schem = sn.toString();
            String woName = workOrderName;

            if (!schem.contains("cache"))
            {
                if (schem.matches("^.*[a-zA-Z_-]\\d$"))
                {

                    schem = schem.replaceAll("\\d$", "");
                    schem += '1';
                }

                if (woName.matches("^.*[a-zA-Z_-]\\d$"))
                {
                    woName = woName.replaceAll("\\d$", "");
                    woName += '1';
                }
            }

            WorkOrderDecoration woDeco = WorkOrderDecoration.create(WorkOrderType.BUILD, schem, WordUtils.capitalizeFully(woName), buildPos, rotation, mirror, 0);
            if (!builder.equals(BlockPos.ZERO))
            {
                woDeco.setClaimedBy(builder);
            }

            colony.getWorkManager().addWorkOrder(woDeco, false);
        }
        else
        {
            SoundUtils.playErrorSound(player, player.blockPosition());
            Log.getLogger().error("handleDecoration: Could not build " + sn, new Exception());
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
     * @param mirror   Whether or not the structure is mirrored.
     * @param level    the future initial building level.
     * @param complete if pasted.
     */
    private static void setupBuilding(@NotNull final Level world, @NotNull final Player player, final StructureName sn, final int rotation, @NotNull final BlockPos buildPos, final boolean mirror, final int level, final boolean complete)
    {
        @Nullable final IBuilding building = IColonyManager.getInstance().getBuilding(world, buildPos);

        if (building == null)
        {
            if (!sn.getHutName().equals(ModBuildings.TOWNHALL_ID))
            {
                SoundUtils.playErrorSound(player, player.blockPosition());
                Log.getLogger().error("BuildTool: building is null!", new Exception());
            }
        }
        else
        {
            SoundUtils.playSuccessSound(player, player.blockPosition());
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


            building.setStyle(sn.getStyle());
            building.setBuildingLevel(level);
            if (level > 0)
            {
                building.setDeconstructed();
            }

            if (!(building instanceof IRSComponent))
            {
                ConstructionTapeHelper.placeConstructionTape(building.getCorners(), world);
            }

            building.setIsMirrored(mirror);

            if (complete)
            {
                building.onUpgradeComplete(building.getBuildingLevel());
            }
        }
    }
    public BlockPos builder = BlockPos.ZERO;
    /**
     * The state at the offset position.
     */
    private BlockState state;
    private String structureName;
    private String workOrderName;
    private int rotation;
    private BlockPos pos;
    private boolean isHut;
    private boolean mirror;
    private String woodType;
    private String rockType;
    private String soilType;

    /**
     * Empty constructor used when registering the
     */
    public TFCMBuildToolPlaceMessage()
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
     * @param state         the state.
     */
    public TFCMBuildToolPlaceMessage(final String structureName, final String workOrderName, final BlockPos pos, final int rotation, final boolean isHut, final Mirror mirror, final BlockState state, final String woodType, final String rockType, final String soilType)
    {
        super();
        this.structureName = structureName;
        this.workOrderName = workOrderName;
        this.pos = pos;
        this.rotation = rotation;
        this.isHut = isHut;
        this.mirror = mirror == Mirror.FRONT_BACK;
        this.state = state;
        this.woodType = woodType;
        this.rockType = rockType;
        this.soilType = soilType;
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

        buf.writeInt(Block.getId(state));

        buf.writeBlockPos(builder);

        buf.writeUtf(woodType);
        buf.writeUtf(rockType);
        buf.writeUtf(soilType);
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

        state = Block.stateById(buf.readInt());

        builder = buf.readBlockPos();

        woodType = buf.readUtf(32767);
        rockType = buf.readUtf(32767);
        soilType = buf.readUtf(32767);
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
        final Player player = ctxIn.getSender();
        final StructureName sn = new StructureName(structureName);
        if (!Structures.hasMD5(sn))
        {
            MessageUtils.format(new TextComponent("Can not build " + workOrderName + ": schematic missing!")).sendTo(player);
            return;
        }
        if (isHut)
        {
            handleHut(CompatibilityUtils.getWorldFromEntity(player), player, sn, rotation, pos, mirror, state, woodType, rockType, soilType);
        }
        else
        {
            handleDecoration(CompatibilityUtils.getWorldFromEntity(player), player, sn, workOrderName, rotation, pos, mirror, builder);
        }
    }
}
