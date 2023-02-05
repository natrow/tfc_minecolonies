package com.natrow.tfc_minecolonies.mixin;

import java.util.function.Supplier;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.coremod.network.NetworkChannel;
import com.natrow.tfc_minecolonies.minecolonies.TFCMBuildToolPasteMessage;
import com.natrow.tfc_minecolonies.minecolonies.TFCMBuildToolPlaceMessage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(value = NetworkChannel.class, remap = false)
public abstract class NetworkChannelMixin
{
    @Shadow protected abstract <MSG extends IMessage> void registerMessage(int id, Class<MSG> msgClazz, Supplier<MSG> msgCreator);

    @Inject(method = "registerCommonMessages", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private void registerCommonMessagesInjector(CallbackInfo ci, int idx)
    {
        registerMessage(++idx, TFCMBuildToolPasteMessage.class, TFCMBuildToolPasteMessage::new);
        registerMessage(++idx, TFCMBuildToolPlaceMessage.class, TFCMBuildToolPlaceMessage::new);
    }
}
