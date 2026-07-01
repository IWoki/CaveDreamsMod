package com.imwoki.cavedreams.entity;

import com.imwoki.cavedreams.CaveDreams;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.util.Identifier;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.BiomeKeys;

public class ModEntities {
    public static final EntityType<LullabiteEntity> LULLABITE = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier(CaveDreams.MOD_ID, "lullabite"),
            FabricEntityTypeBuilder.create(SpawnGroup.AXOLOTLS, LullabiteEntity::new)
                    .dimensions(EntityDimensions.fixed(0.6f, 1.0f))
                    .spawnableFarFromPlayer()
                    .build()
    );

    public static void register() {
        FabricDefaultAttributeRegistry.register(LULLABITE, LullabiteEntity.createLullabiteAttributes());
        registerSpawnRestriction();
        registerSpawns();
    }

    private static void registerSpawnRestriction() {
        SpawnRestriction.register(
                LULLABITE,
                SpawnRestriction.Location.NO_RESTRICTIONS,
                Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
                LullabiteEntity::canSpawn
        );
    }

    // Спавн в мшистых пещерах, стайками по 4-6 особей (как аксолотли)
    private static void registerSpawns() {
        BiomeModifications.addSpawn(
                BiomeSelectors.includeByKey(BiomeKeys.LUSH_CAVES),
                SpawnGroup.AXOLOTLS,
                LULLABITE,
                10,
                4,
                6
        );
    }
}