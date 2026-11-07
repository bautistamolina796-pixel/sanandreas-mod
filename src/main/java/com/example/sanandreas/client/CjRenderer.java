package com.example.sanandreas.client;

import com.example.sanandreas.SanAndreasMod;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.resources.ResourceLocation;

/** Renderer de jugador que siempre usa el skin de CJ (brazos clasicos de 4 px). */
public class CjRenderer extends PlayerRenderer {
    public static final ResourceLocation TEXTURE =
            new ResourceLocation(SanAndreasMod.MOD_ID, "textures/entity/cj.png");

    private static EntityRendererProvider.Context context;
    private static CjRenderer instance;

    private CjRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, false);
    }

    /** Se llama cada vez que Minecraft recrea los renderers (inicio y F3+T). */
    public static void setContext(EntityRendererProvider.Context ctx) {
        context = ctx;
        instance = null;
    }

    /** Devuelve null si todavia no se pudo crear (el juego usa entonces el skin normal). */
    public static CjRenderer get() {
        if (instance == null && context != null) {
            try {
                instance = new CjRenderer(context);
            } catch (Exception e) {
                context = null;
                return null;
            }
        }
        return instance;
    }

    @Override
    public ResourceLocation getTextureLocation(AbstractClientPlayer player) {
        return TEXTURE;
    }
}
