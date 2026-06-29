package com.imwoki.cavedreams.mixin;

import com.imwoki.cavedreams.util.DreamPlayer;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Inject(method = "isSleepingInBed", at = @At("HEAD"), cancellable = true)
    private void cavedreams$isSleepingInBed(CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof DreamPlayer dream && dream.cavedreams_isDreaming()) {
            cir.setReturnValue(true);
        }
    }
}