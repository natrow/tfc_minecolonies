package com.natrow.tfc_minecolonies.minecolonies;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import com.minecolonies.api.entity.citizen.citizenhandlers.ICitizenItemHandler;
import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.common.util.FakePlayer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import net.dries007.tfc.common.items.ScytheItem;
import net.dries007.tfc.util.AxeLoggingHelper;

public class TFCMinecoloniesBreakActions
{
    private static final Map<ResourceKey<Level>, FakePlayer> fakePlayers = new HashMap<>();
    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Fakes mining whilst doing correct tool handling & drops for TFC.
     * Note: incompatible with Minecolonies drops & item damage handling.
     *
     * @param world              level accessor
     * @param blockPos           position of block to break
     * @param itemStack          item to break block with
     * @param workerPos          position of worker
     * @param citizenItemHandler produces fake sounds/animations/particle effects
     * @return A callback function for breaking blocks
     */
    public static Runnable getToolBreakAction(@NotNull final Level world, @NotNull final BlockPos blockPos, @NotNull final ItemStack itemStack, @NotNull final BlockPos workerPos, @NotNull final ICitizenItemHandler citizenItemHandler)
    {
        return () -> {
            final BlockState curBlockState = world.getBlockState(blockPos);
            final Item item = itemStack.getItem();

            if (world.getServer() == null)
            {
                LOGGER.error("TFCMinecolonies: unexpected null while trying to get World");
                return;
            }

            final ResourceKey<Level> dim = world.dimension();
            FakePlayer fake = fakePlayers.get(dim);

            if (fake == null) // generate new fake player
            {
                fakePlayers.put(dim, new FakePlayer((ServerLevel) world,
                    new GameProfile(UUID.randomUUID(), "TFC Minecolonies FakePlayer")));
                fake = fakePlayers.get(dim);
            }

            fake.setPos(workerPos.getX(), workerPos.getY(), workerPos.getZ());
            fake.setItemInHand(InteractionHand.MAIN_HAND, itemStack);

            FluidState fluidState = world.getFluidState(blockPos);

            if (itemStack.getItem() instanceof ScytheItem)
            {
                // this method seems to work for the scythe ¯\_(ツ)_/¯
                item.mineBlock(itemStack, world, curBlockState, blockPos, fake);
            }
            else if (AxeLoggingHelper.isLoggingAxe(itemStack) && AxeLoggingHelper.isLoggingBlock(curBlockState))
            {
                // meanwhile this one works for axe logging
                AxeLoggingHelper.doLogging(world, blockPos, fake, itemStack);
            }
            else
            {
                // and this one works for... everything else?
                curBlockState.onDestroyedByPlayer(world, blockPos, fake, true, fluidState);
            }

            // play animation
            citizenItemHandler.hitBlockWithToolInHand(blockPos);
        };
    }
}
