package com.example.sanandreas.client;

import com.example.sanandreas.ModEntities;
import com.example.sanandreas.ModItems;
import com.example.sanandreas.SanAndreasMod;
import com.example.sanandreas.item.SaWeaponItem;
import com.example.sanandreas.network.FirePacket;
import com.example.sanandreas.network.ModNetwork;
import com.example.sanandreas.network.ReloadPacket;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.IItemDecorator;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterItemDecorationsEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderArmEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryObject;

public class ClientEvents {

    public static final KeyMapping RELOAD =
            new KeyMapping("key.sanandreas.reload", InputConstants.KEY_R, "key.categories.sanandreas");

    /** Numero de municion total (reserva + cargador) abajo a la derecha del slot, como en el video. */
    private static final IItemDecorator AMMO_DECORATOR = (g, font, stack, x, y) -> {
        if (!(stack.getItem() instanceof SaWeaponItem weapon)) return false;
        String s = String.valueOf(weapon.getReserve(stack) + weapon.getClip(stack));
        float scale = 0.6f;
        PoseStack pose = g.pose();
        pose.pushPose();
        pose.translate(x + 17 - font.width(s) * scale, y + 17 - 8 * scale, 200f);
        pose.scale(scale, scale, 1f);
        g.drawString(font, s, 0, 0, 0xFFFFFFFF, true);
        pose.popPose();
        return false;
    };

    // ====================================================================== bus del MOD
    @Mod.EventBusSubscriber(modid = SanAndreasMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ModBus {
        @SubscribeEvent
        public static void registerOverlays(RegisterGuiOverlaysEvent event) {
            event.registerAboveAll("gta_hud", SanAndreasHud.HUD);
        }

        @SubscribeEvent
        public static void registerKeys(RegisterKeyMappingsEvent event) {
            event.register(RELOAD);
        }

        @SubscribeEvent
        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(ModEntities.ROCKET.get(), ThrownItemRenderer::new);
        }

        @SubscribeEvent
        public static void addLayers(EntityRenderersEvent.AddLayers event) {
            CjRenderer.setContext(event.getContext());
        }

        @SubscribeEvent
        public static void registerDecorations(RegisterItemDecorationsEvent event) {
            for (RegistryObject<SaWeaponItem> weapon : ModItems.WEAPONS.values()) {
                event.register(weapon.get(), AMMO_DECORATOR);
            }
        }
    }

    // ====================================================================== bus de FORGE
    @Mod.EventBusSubscriber(modid = SanAndreasMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class ForgeBus {
        private static boolean useWasDown = false;
        private static int nextClientShot = 0;
        private static boolean renderingCj = false;

        /** Oculta corazones y armadura de vainilla (la vida va en el HUD de GTA). */
        @SubscribeEvent
        public static void onOverlayPre(RenderGuiOverlayEvent.Pre event) {
            if (event.getOverlay() == VanillaGuiOverlay.PLAYER_HEALTH.type()
                    || event.getOverlay() == VanillaGuiOverlay.ARMOR_LEVEL.type()) {
                event.setCanceled(true);
            }
        }

        /** Con un arma en la mano, el clic derecho dispara en vez de usar/colocar. */
        @SubscribeEvent
        public static void onInteraction(InputEvent.InteractionKeyMappingTriggered event) {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null && event.isUseItem() && player.getMainHandItem().getItem() instanceof SaWeaponItem) {
                event.setCanceled(true);
                event.setSwingHand(false);
            }
        }

        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;
            Minecraft mc = Minecraft.getInstance();
            LocalPlayer player = mc.player;
            if (player == null) return;

            ItemStack held = player.getMainHandItem();
            boolean holdingWeapon = held.getItem() instanceof SaWeaponItem;

            // Se consume siempre para que no se acumulen pulsaciones de R
            while (RELOAD.consumeClick()) {
                if (holdingWeapon && mc.screen == null) {
                    ModNetwork.CHANNEL.sendToServer(new ReloadPacket());
                }
            }

            if (!holdingWeapon || mc.screen != null) {
                useWasDown = false;
                return;
            }

            SaWeaponItem weapon = (SaWeaponItem) held.getItem();
            boolean down = mc.options.keyUse.isDown();
            boolean wantsShot = down && (weapon.type.automatic || !useWasDown);
            if (wantsShot && !player.getCooldowns().isOnCooldown(weapon) && player.tickCount >= nextClientShot) {
                nextClientShot = player.tickCount + weapon.type.fireDelay;
                ModNetwork.CHANNEL.sendToServer(new FirePacket());
                if (weapon.getClip(held) > 0 && weapon.type.recoil > 0f) {
                    player.setXRot(Mth.clamp(player.getXRot() - weapon.type.recoil, -90f, 90f));
                }
            }
            useWasDown = down;
        }

        /** Cambia el modelo de TODOS los jugadores por el de CJ (skin propio). */
        @SubscribeEvent
        public static void onRenderPlayer(RenderPlayerEvent.Pre event) {
            if (renderingCj) return;
            CjRenderer cj = CjRenderer.get();
            if (cj == null) return;
            if (!(event.getEntity() instanceof AbstractClientPlayer player)) return;

            event.setCanceled(true);
            renderingCj = true;
            try {
                cj.render(player, player.getYRot(), event.getPartialTick(),
                        event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight());
            } finally {
                renderingCj = false;
            }
        }

        /** Brazo en primera persona con el skin de CJ. */
        @SubscribeEvent
        public static void onRenderArm(RenderArmEvent event) {
            CjRenderer cj = CjRenderer.get();
            if (cj == null) return;
            event.setCanceled(true);
            if (event.getArm() == HumanoidArm.RIGHT) {
                cj.renderRightHand(event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight(), event.getPlayer());
            } else {
                cj.renderLeftHand(event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight(), event.getPlayer());
            }
        }
    }
}
