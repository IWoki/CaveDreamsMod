package com.imwoki.cavedreams.client;

import com.imwoki.cavedreams.CaveDreams;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;

public class ModModelLayer {
    public static final EntityModelLayer LULLABITE_LAYER =
            new EntityModelLayer(new Identifier(CaveDreams.MOD_ID, "lullabite"), "main");
}