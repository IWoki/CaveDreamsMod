package com.imwoki.cavedreams.mixin;

import com.imwoki.cavedreams.util.DreamPlayer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {

    @Inject(method = "wakeUp(ZZ)V", at = @At("HEAD"), cancellable = true)
    private void cavedreams$preventWakeUp(boolean skipSleepTimer, boolean updateSleepingPlayers, CallbackInfo ci) {
        if ((Object) this instanceof DreamPlayer dream
                && dream.cavedreams_isDreaming()
                && !dream.cavedreams_isAllowWake()) {
            ci.cancel();
        }
    }
}