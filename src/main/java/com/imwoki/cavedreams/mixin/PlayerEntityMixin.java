package com.imwoki.cavedreams.mixin;

import com.imwoki.cavedreams.event.LullabiteProximityHandler;
import com.imwoki.cavedreams.util.DreamPlayer;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin implements DreamPlayer {

	@Unique private static final TrackedData<Boolean> CAVEDREAMS_DREAMING =
			DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

	@Unique private long dreamWakeTick = -1;
	@Unique private boolean dreamUntamed;
	@Unique private boolean dreamStabilized;
	@Unique private boolean dreamLullabite;
	@Unique boolean dreamAllowWake = false;

	@Override
	public boolean cavedreams_isAllowWake() { return dreamAllowWake; }

	@Inject(method = "<init>", at = @At("TAIL"))
	private void cavedreams$initDreamTracker(CallbackInfo ci) {
		((PlayerEntity) (Object) this).getDataTracker().startTracking(CAVEDREAMS_DREAMING, false);
	}

	@Override
	public void cavedreams_startDream(long wakeTick, boolean untamed, boolean stabilized) {
		PlayerEntity self = (PlayerEntity) (Object) this;
		this.dreamWakeTick = wakeTick;
		this.dreamUntamed = untamed;
		this.dreamStabilized = stabilized;
		self.getDataTracker().set(CAVEDREAMS_DREAMING, true);
		self.sleep(self.getBlockPos());
	}

	@Override
	public void cavedreams_startLullabiteDream(long wakeTick) {
		this.dreamLullabite = true;
		cavedreams_startDream(wakeTick, false, false);
	}

	@Override
	public boolean cavedreams_isDreaming() {
		return ((PlayerEntity) (Object) this).getDataTracker().get(CAVEDREAMS_DREAMING);
	}

	@Inject(method = "tick", at = @At("TAIL"))
	private void cavedreams$checkDreamWakeUp(CallbackInfo ci) {
		PlayerEntity self = (PlayerEntity) (Object) this;
		if (self.getWorld().isClient || dreamWakeTick == -1) return;
		if (self.getWorld().getTime() >= dreamWakeTick && self instanceof ServerPlayerEntity sp) {
			dreamAllowWake = true;
			sp.wakeUp(false, true);
			dreamAllowWake = false;
			applyDreamEffects(self);
			LullabiteProximityHandler.onPlayerWakeUp(sp); // реакция мобов
			resetDreamState(self);
		}
	}

	@Unique
	private void applyDreamEffects(PlayerEntity player) {
		if (dreamUntamed) {
			player.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 200, 0));
			player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 400, 1));
		} else if (dreamStabilized) {
			player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 200, 0));
			player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 400, 0));
		} else if (dreamLullabite) {
			player.addStatusEffect(new StatusEffectInstance(StatusEffects.HUNGER, 200, 0));
			player.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 200, 0));
		}
	}

	@Unique
	private void resetDreamState(PlayerEntity self) {
		dreamWakeTick = -1;
		dreamUntamed = false;
		dreamStabilized = false;
		dreamLullabite = false;
		self.getDataTracker().set(CAVEDREAMS_DREAMING, false);
	}
}