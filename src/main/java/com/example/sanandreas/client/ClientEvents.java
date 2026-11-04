package com.example.sanandreas.client;

import com.example.sanandreas.SanAndreasMod;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

public class ClientEvents {

    /** Eventos del bus del MOD: registrar nuestro overlay. */
    @Mod.EventBusSubscriber(modid = SanAndreasMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ModBus {
        @SubscribeEvent
        public static void registerOverlays(RegisterGuiOverlaysEvent event) {
            event.registerAboveAll("gta_hud", SanAndreasHud.HUD);
        }
    }

    /** Eventos del bus de FORGE: ocultar corazones y armadura de vainilla. */
    @Mod.EventBusSubscriber(modid = SanAndreasMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class ForgeBus {
        @SubscribeEvent
        public static void onOverlayPre(RenderGuiOverlayEvent.Pre event) {
            if (event.getOverlay() == VanillaGuiOverlay.PLAYER_HEALTH.type()
                    || event.getOverlay() == VanillaGuiOverlay.ARMOR_LEVEL.type()) {
                event.setCanceled(true);
            }
        }
    }
}
