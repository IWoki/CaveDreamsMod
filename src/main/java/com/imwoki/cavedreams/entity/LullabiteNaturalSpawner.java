package com.imwoki.cavedreams.entity;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.SpawnReason;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeKeys;

public final class LullabiteNaturalSpawner {
    private static final int SPAWN_INTERVAL = 400;
    private static final int MIN_GROUP = 4;
    private static final int MAX_GROUP = 6;
    private static final int AREA_CAP = 12;
    private static final int SPAWN_RADIUS = 48;
    private static final int MAX_ATTEMPTS = 80;

    private LullabiteNaturalSpawner() {
    }

    public static void register() {
        ServerTickEvents.END_WORLD_TICK.register(world -> {
            if (!(world instanceof ServerWorld)) {
                return;
            }
            ServerWorld serverWorld = (ServerWorld) world;
            if (serverWorld.getRegistryKey() != World.OVERWORLD) {
                return;
            }
            if (!serverWorld.getGameRules().getBoolean(GameRules.DO_MOB_SPAWNING)) {
                return;
            }
            if (serverWorld.getTime() % SPAWN_INTERVAL != 0) {
                return;
            }

            for (ServerPlayerEntity player : serverWorld.getPlayers()) {
                if (player.isSpectator()) {
                    continue;
                }
                BlockPos playerPos = player.getBlockPos();
                if (!serverWorld.getBiome(playerPos).matchesKey(BiomeKeys.LUSH_CAVES)) {
                    continue;
                }
                trySpawnGroup(serverWorld, playerPos, serverWorld.getRandom());
            }
        });
    }

    private static void trySpawnGroup(ServerWorld world, BlockPos center, Random random) {
        Box area = new Box(center).expand(SPAWN_RADIUS);
        long existing = world.getEntitiesByType(ModEntities.LULLABITE, area, entity -> entity.isAlive()).size();
        if (existing >= AREA_CAP) {
            return;
        }

        int groupSize = random.nextBetween(MIN_GROUP, MAX_GROUP);
        if (existing + groupSize > AREA_CAP) {
            groupSize = (int) (AREA_CAP - existing);
        }
        if (groupSize < MIN_GROUP) {
            return;
        }

        BlockPos anchor = findSpawnPos(world, center, random);
        if (anchor == null) {
            return;
        }

        int spawned = 0;
        for (int i = 0; i < groupSize; i++) {
            BlockPos pos = i == 0 ? anchor : findSpawnPosNear(world, anchor, random);
            if (pos == null) {
                continue;
            }
            spawnOne(world, pos, random);
            spawned++;
        }

        if (spawned == 0) {
            return;
        }
    }

    private static BlockPos findSpawnPosNear(World world, BlockPos anchor, Random random) {
        for (int attempt = 0; attempt < 16; attempt++) {
            BlockPos pos = anchor.add(
                    random.nextBetween(-6, 6),
                    random.nextBetween(-3, 3),
                    random.nextBetween(-6, 6)
            );
            if (LullabiteEntity.canSpawn(ModEntities.LULLABITE, world, SpawnReason.NATURAL, pos, random)) {
                return pos;
            }
        }
        return null;
    }

    private static BlockPos findSpawnPos(World world, BlockPos center, Random random) {
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            BlockPos pos = center.add(
                    random.nextBetween(-SPAWN_RADIUS, SPAWN_RADIUS),
                    random.nextBetween(-24, 24),
                    random.nextBetween(-SPAWN_RADIUS, SPAWN_RADIUS)
            );
            if (LullabiteEntity.canSpawn(ModEntities.LULLABITE, world, SpawnReason.NATURAL, pos, random)) {
                return pos;
            }
        }
        return null;
    }

    private static void spawnOne(ServerWorld world, BlockPos pos, Random random) {
        LullabiteEntity lullabite = ModEntities.LULLABITE.create(world);
        lullabite.refreshPositionAndAngles(
                pos.getX() + 0.5,
                pos.getY(),
                pos.getZ() + 0.5,
                random.nextFloat() * 360.0F,
                0.0F
        );
        lullabite.initialize(world, world.getLocalDifficulty(pos), SpawnReason.NATURAL, null, null);
        world.spawnEntity(lullabite);
    }
}
