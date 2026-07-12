package com.imwoki.cavedreams.client.model;

import com.imwoki.cavedreams.entity.LullabiteEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class LullabiteModel extends GeoModel<LullabiteEntity> {

    // Пути к файлам которые вы экспортируете из Blockbench:
    //
    // Геометрия (модель):
    //   src/main/resources/assets/cavedreams/geo/lullabite.geo.json
    //
    // Анимации:
    //   src/main/resources/assets/cavedreams/animations/lullabite.animation.json
    //
    // Текстура:
    //   src/main/resources/assets/cavedreams/textures/entity/lullabite.png

    @Override
    public Identifier getModelResource(LullabiteEntity entity) {
        return new Identifier("cavedreams", "geo/lullabite.geo.json");
    }

    @Override
    public Identifier getTextureResource(LullabiteEntity entity) {
        return new Identifier("cavedreams", "textures/entity/lullabite.png");
    }

    @Override
    public Identifier getAnimationResource(LullabiteEntity entity) {
        return new Identifier("cavedreams", "animations/lullabite.animation.json");
    }
}