package com.imwoki.cavedreams.item;

import com.imwoki.cavedreams.entity.ModEntities;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModItems {

    public static final Item UNTAMED_LULLADUST = new UntamedLulladustItem(
            new FabricItemSettings().food(new FoodComponent.Builder()
                    .hunger(2)
                    .saturationModifier(0.25f)
                    .alwaysEdible()
                    .build())
    );

    public static final Item STABILIZED_LULLADUST = new StabilizedLulladustItem(
            new FabricItemSettings().food(new FoodComponent.Builder()
                    .hunger(4)
                    .saturationModifier(1.0f)
                    .alwaysEdible()
                    .build())
    );

    // Яйцо спавна Lullabite - пастельные "сонные" цвета (основной/крапинки), как у ванильных яиц
    public static final Item LULLABITE_SPAWN_EGG = new SpawnEggItem(
            ModEntities.LULLABITE,
            0xA9C8FF,
            0xFFE9A8,
            new FabricItemSettings()
    );

    public static void register() {
        Registry.register(Registries.ITEM, new Identifier("cavedreams", "untamed_lulladust"), UNTAMED_LULLADUST);
        Registry.register(Registries.ITEM, new Identifier("cavedreams", "stabilized_lulladust"), STABILIZED_LULLADUST);
        Registry.register(Registries.ITEM, new Identifier("cavedreams", "lullabite_spawn_egg"), LULLABITE_SPAWN_EGG);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FOOD_AND_DRINK).register(entries -> {
            entries.add(UNTAMED_LULLADUST);
            entries.add(STABILIZED_LULLADUST);
        });

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS).register(entries -> {
            entries.add(LULLABITE_SPAWN_EGG);
        });
    }
}