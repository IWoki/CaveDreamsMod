package com.imwoki.cavedreams.event;

import com.imwoki.cavedreams.util.DreamPlayer;
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.block.BlockState;

public final class DreamSleepEvents {
	private DreamSleepEvents() {
	}

	public static void register() {
		EntitySleepEvents.ALLOW_BED.register(DreamSleepEvents::allowBed);
		EntitySleepEvents.MODIFY_SLEEPING_DIRECTION.register(DreamSleepEvents::modifySleepDirection);
		EntitySleepEvents.MODIFY_WAKE_UP_POSITION.register(DreamSleepEvents::modifyWakeUpPosition);
		EntitySleepEvents.ALLOW_RESETTING_TIME.register(DreamSleepEvents::allowResettingTime);
	}

	private static ActionResult allowBed(LivingEntity entity, BlockPos sleepingPos, BlockState state, boolean vanillaResult) {
		if (entity instanceof DreamPlayer dream && dream.cavedreams_isDreaming()) {
			return ActionResult.SUCCESS;
		}
		return ActionResult.PASS;
	}

	private static Direction modifySleepDirection(LivingEntity entity, BlockPos sleepingPos, Direction sleepingDirection) {
		if (entity instanceof DreamPlayer dream && dream.cavedreams_isDreaming()) {
			return entity.getHorizontalFacing().getOpposite();
		}
		return sleepingDirection;
	}

	private static Vec3d modifyWakeUpPosition(LivingEntity entity, BlockPos sleepingPos, BlockState bedState, Vec3d wakeUpPos) {
		if (entity instanceof DreamPlayer dream && dream.cavedreams_isDreaming()) {
			return Vec3d.ofBottomCenter(sleepingPos);
		}
		return wakeUpPos;
	}

	private static boolean allowResettingTime(PlayerEntity player) {
		return !(player instanceof DreamPlayer dream && dream.cavedreams_isDreaming());
	}
}
