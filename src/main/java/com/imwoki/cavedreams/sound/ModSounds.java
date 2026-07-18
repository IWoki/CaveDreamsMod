package com.imwoki.cavedreams.sound;

import com.imwoki.cavedreams.CaveDreams;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class ModSounds {

    public static final SoundEvent LULLABITE_IDLE_1 = register("lullabite_idle_1");
    public static final SoundEvent LULLABITE_IDLE_2 = register("lullabite_idle_2");
    public static final SoundEvent LULLABITE_IDLE_3 = register("lullabite_idle_3");

    public static final SoundEvent LULLABITE_HURT_1 = register("lullabite_hurt_1");
    public static final SoundEvent LULLABITE_HURT_2 = register("lullabite_hurt_2");

    public static final SoundEvent LULLABITE_DEATH = register("lullabite_death");

    private static SoundEvent register(String name) {
        Identifier id = CaveDreams.id(name);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }

    public static void register() {
    }
}