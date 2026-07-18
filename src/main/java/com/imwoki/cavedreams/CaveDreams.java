package com.imwoki.cavedreams;

import com.imwoki.cavedreams.event.LullabiteProximityHandler;
import com.imwoki.cavedreams.entity.LullabiteNaturalSpawner;
import com.imwoki.cavedreams.entity.ModEntities;
import com.imwoki.cavedreams.event.DreamSleepEvents;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import com.imwoki.cavedreams.item.ModItems;
import com.imwoki.cavedreams.sound.ModSounds;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CaveDreams implements ModInitializer {
	public static final String MOD_ID = "cavedreams";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModItems.register();
		ModSounds.register();
		DreamSleepEvents.register();
		ModEntities.register();
		LullabiteNaturalSpawner.register();
		LullabiteProximityHandler.register();
	}

	public static Identifier id(String path) {
		return new Identifier(MOD_ID, path);
	}

	public static boolean isTrueSingleplayer(MinecraftServer server) {
		return server != null
				&& !server.isDedicated()
				&& server.getPlayerManager().getPlayerList().size() <= 1;
	}
}
