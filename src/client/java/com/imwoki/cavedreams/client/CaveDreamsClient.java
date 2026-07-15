package com.imwoki.cavedreams.client;

import com.imwoki.cavedreams.client.render.LullabiteRenderer;
import com.imwoki.cavedreams.entity.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class CaveDreamsClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		EntityRendererRegistry.register(ModEntities.LULLABITE, LullabiteRenderer::new);
	}
}