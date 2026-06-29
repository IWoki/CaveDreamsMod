package com.imwoki.cavedreams.client.mixin;

import com.imwoki.cavedreams.util.DreamPlayer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.SleepingChatScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SleepingChatScreen.class)
public abstract class SleepingChatScreenMixin {

	@Shadow
	@Final
	private ButtonWidget stopSleepingButton;

	@Inject(method = "init", at = @At("TAIL"))
	private void cavedreams$hideLeaveBedButton(CallbackInfo ci) {
		if (cavedreams$isDreaming()) {
			this.stopSleepingButton.visible = false;
			this.stopSleepingButton.active = false;
		}
	}

	@Inject(method = "stopSleeping", at = @At("HEAD"), cancellable = true)
	private void cavedreams$preventStopSleeping(CallbackInfo ci) {
		if (cavedreams$isDreaming()) {
			ci.cancel();
		}
	}

	private static boolean cavedreams$isDreaming() {
		MinecraftClient client = MinecraftClient.getInstance();
		return client.player instanceof DreamPlayer dream && dream.cavedreams_isDreaming();
	}
}
