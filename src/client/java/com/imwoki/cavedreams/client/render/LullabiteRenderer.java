package com.imwoki.cavedreams.client.render;

import com.imwoki.cavedreams.CaveDreams;
import com.imwoki.cavedreams.entity.LullabiteEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.util.Identifier;
import com.imwoki.cavedreams.client.ModModelLayer;
import com.imwoki.cavedreams.client.model.LullabiteModel;

public class LullabiteRenderer extends MobEntityRenderer<LullabiteEntity, EntityModel<LullabiteEntity>> {

    private static final Identifier TEXTURE = new Identifier(CaveDreams.MOD_ID, "textures/entity/lullabite.png");

    public LullabiteRenderer(EntityRendererFactory.Context context) {
        super(context, new LullabiteModel(context.getPart(ModModelLayer.LULLABITE_LAYER)), 0.5f);
    }

    @Override
    public Identifier getTexture(LullabiteEntity entity) {
        return TEXTURE;
    }
}