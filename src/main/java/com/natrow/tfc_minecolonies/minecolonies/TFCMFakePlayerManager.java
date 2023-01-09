package com.natrow.tfc_minecolonies.minecolonies;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import com.minecolonies.api.entity.citizen.citizenhandlers.ICitizenItemHandler;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.FakePlayer;
import org.jetbrains.annotations.NotNull;

public class TFCMFakePlayerManager
{
    private static final Map<ResourceKey<Level>, FakePlayer> fakePlayers = new HashMap<>();

    /**
     * Sets up a fake player for using tools
     *
     * @param world      world to make FakePlayer in
     * @param workerPos  position to put FakePlayer in
     * @param toolInHand tool to put in FakePlayer's main hand
     * @return a new (or reused) fake player
     */
    public static FakePlayer setupFakePlayer(@NotNull final Level world, @NotNull final BlockPos workerPos, @NotNull final ItemStack toolInHand)
    {
        if (world.getServer() == null)
        {
            throw new NullPointerException("TFC Minecolonies: unexpected null while trying to get world");
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
        fake.setItemInHand(InteractionHand.MAIN_HAND, toolInHand);

        return fake;
    }

    /**
     * Fakes mining whilst doing correct tool handling & drops for TFC.
     * Note: incompatible with Minecolonies drops & item damage handling.
     *
     * @param world              level accessor
     * @param blockPos           position of block to break
     * @param tool               item to break block with
     * @param workerPos          position of worker
     * @param citizenItemHandler produces fake sounds/animations/particle effects
     * @return A callback function for breaking blocks
     */
    public static Runnable getToolBreakAction(@NotNull final Level world, @NotNull final BlockPos blockPos, @NotNull final ItemStack tool, @NotNull final BlockPos workerPos, @NotNull final ICitizenItemHandler citizenItemHandler)
    {
        return () -> {
            FakePlayer fake = setupFakePlayer(world, workerPos, tool);

            // Thanks, but I prefer it my way ;)
            fake.gameMode.destroyBlock(blockPos);

            // play animation
            citizenItemHandler.hitBlockWithToolInHand(blockPos);
        };
    }

    /**
     * Detects whether a player is actually a fake player created by this mod.
     */
    public static boolean isFakePlayer(FakePlayer player)
    {
        return fakePlayers.containsValue(player);
    }
}
