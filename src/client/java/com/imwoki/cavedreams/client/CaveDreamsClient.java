package com.imwoki.cavedreams.client;

import net.fabricmc.api.ClientModInitializer;
import com.imwoki.cavedreams.client.render.LullabiteRenderer;
import com.imwoki.cavedreams.entity.ModEntities;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import com.imwoki.cavedreams.client.model.LullabiteModel;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;

public class CaveDreamsClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		EntityRendererRegistry.register(ModEntities.LULLABITE, LullabiteRenderer::new);
		EntityModelLayerRegistry.registerModelLayer(ModModelLayer.LULLABITE_LAYER, LullabiteModel::getTexturedModelData);
	}
}