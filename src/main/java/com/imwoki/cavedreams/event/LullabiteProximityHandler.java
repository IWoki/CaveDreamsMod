package com.imwoki.cavedreams.event;

import com.imwoki.cavedreams.entity.LullabiteEntity;
import com.imwoki.cavedreams.util.DreamPlayer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import java.util.*;

public class LullabiteProximityHandler {
    private static final int RADIUS = 16;
    private static final Map<UUID, Integer> playerTickCounter = new HashMap<>();
    // 1, 1.5, 2, 2.5 минуты
    private static final int[] EFFECT_TICKS = {1200, 1800, 2400, 3000};
    private static final int SLEEP_TICKS = 3600; // 3 мин

    public static void register() {
        ServerTickEvents.START_WORLD_TICK.register(LullabiteProximityHandler::onWorldTick);
    }

    private static void onWorldTick(ServerWorld world) {
        for (PlayerEntity player : world.getPlayers()) {
            if (player.isSpectator() || player.isCreative()) {
                playerTickCounter.remove(player.getUuid());
                continue;
            }
            boolean nearMob = !world.getEntitiesByClass(LullabiteEntity.class,
                    player.getBoundingBox().expand(RADIUS), LivingEntity::isAlive).isEmpty();
            UUID id = player.getUuid();
            if (nearMob) {
                int ticks = playerTickCounter.getOrDefault(id, 0) + 1;
                playerTickCounter.put(id, ticks);
                updateEffects(player, ticks);
                if (ticks >= SLEEP_TICKS && player instanceof ServerPlayerEntity sp) {
                    forceSleep(sp, world);
                    playerTickCounter.remove(id);
                }
            } else {
                playerTickCounter.remove(id);
            }
        }
    }

    private static void updateEffects(PlayerEntity player, int ticks) {
        if (ticks >= EFFECT_TICKS[0]) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 40, 0, true, true));
        }
        if (ticks >= EFFECT_TICKS[1]) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 40, 0, true, true));
        }
        if (ticks >= EFFECT_TICKS[2]) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 40, 0, true, true));
        }
        if (ticks >= EFFECT_TICKS[3]) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 40, 1, true, true));
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 40, 1, true, true));
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 40, 1, true, true));
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 800, 0, true, true));
        }
    }

    private static void forceSleep(ServerPlayerEntity player, ServerWorld world) {
        if (!(player instanceof DreamPlayer dreamPlayer)) return;
        long wakeTick = world.getTime() + 200; // 10 сек
        if (!world.getServer().isDedicated()) {
            long timeOfDay = world.getTimeOfDay();
            long days = timeOfDay / 24000;
            long nextTime = (timeOfDay % 24000 < 13000) ? days * 24000 + 13000 : (days + 1) * 24000;
            world.setTimeOfDay(nextTime);
        }
        dreamPlayer.cavedreams_startLullabiteDream(wakeTick);
    }

    /** Распространяет страх, только если игрок не креатив/спектатор */
    public static void spreadFear(World world, BlockPos center, PlayerEntity player, long durationTicks) {
        if (world.isClient || player.isCreative() || player.isSpectator()) return;
        List<LullabiteEntity> list = world.getEntitiesByClass(LullabiteEntity.class,
                new net.minecraft.util.math.Box(center).expand(20), LivingEntity::isAlive);
        for (LullabiteEntity mob : list) {
            mob.setFearedPlayer(player, durationTicks);
        }
    }

    /** Переводит ближайших взрослых особей в режим "влюблённости" через игрока, как при кормлении */
    public static void breedNearbyLullabites(ServerWorld world, PlayerEntity player) {
        List<LullabiteEntity> adults = world.getEntitiesByClass(LullabiteEntity.class,
                player.getBoundingBox().expand(20),
                e -> !e.isBaby() && e.isAlive() && e.getBreedingAge() == 0);
        Collections.shuffle(adults);
        for (int i = 0; i < adults.size() - 1; i += 2) {
            LullabiteEntity a = adults.get(i);
            LullabiteEntity b = adults.get(i + 1);
            a.lovePlayer(player);
            b.lovePlayer(player);
        }
    }

    /** Вызывается после любого пробуждения (кровать, пыль, принудительный сон) */
    public static void onPlayerWakeUp(ServerPlayerEntity player) {
        ServerWorld world = player.getServerWorld();
        if (world.isClient) return;
        if (!world.getEntitiesByClass(LullabiteEntity.class, player.getBoundingBox().expand(20), LivingEntity::isAlive).isEmpty()) {
            // Сначала размножаются ("благодарность" игроку), затем пугаются и отступают
            breedNearbyLullabites(world, player);
            spreadFear(world, player.getBlockPos(), player, 20*60*5); // 5 мин страха
        }
    }
}