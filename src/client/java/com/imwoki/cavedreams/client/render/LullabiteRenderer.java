package com.imwoki.cavedreams.client.render;

import com.imwoki.cavedreams.entity.LullabiteEntity;
import com.imwoki.cavedreams.client.model.LullabiteModel;
import net.minecraft.client.render.entity.EntityRendererFactory;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class LullabiteRenderer extends GeoEntityRenderer<LullabiteEntity> {

    public LullabiteRenderer(EntityRendererFactory.Context context) {
        super(context, new LullabiteModel());
    }
}