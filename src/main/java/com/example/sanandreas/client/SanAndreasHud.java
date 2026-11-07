package com.example.sanandreas.client;

import com.example.sanandreas.item.SaWeaponItem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

/** HUD estilo GTA San Andreas: arma + municion, reloj, vida y dinero (arriba a la derecha). */
public class SanAndreasHud {

    /** Placeholder: el dinero real llega con la fase 2. */
    public static int money = 0;

    private static final int GTA_GREEN = 0xFF3F8F3F;
    private static final int GTA_RED = 0xFFC8101E;
    private static final int GTA_GRAY = 0xFFD8D8D8;

    public static final IGuiOverlay HUD = (gui, g, partialTick, width, height) -> {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null || mc.options.hideGui) return;
        Font font = mc.font;

        int right = width - 10;
        int top = 10;

        // Reloj: el dia de Minecraft (24000 ticks) mapeado a 24 h. Tick 0 = 06:00
        long t = mc.level.getDayTime() % 24000L;
        int hours = (int) ((t / 1000L + 6) % 24);
        int minutes = (int) ((t % 1000L) * 60 / 1000L);
        String clock = String.format("%02d:%02d", hours, minutes);
        float clockScale = 2f;
        int clockW = (int) (font.width(clock) * clockScale);
        int clockX = right - clockW;
        drawScaled(g, font, clock, clockX, top, clockScale, GTA_GRAY);

        // Barra de vida
        float frac = Math.max(0f, Math.min(1f, player.getHealth() / player.getMaxHealth()));
        int barY = top + 20;
        g.fill(clockX, barY, clockX + clockW, barY + 6, 0xFF000000);
        g.fill(clockX + 1, barY + 1, clockX + 1 + (int) ((clockW - 2) * frac), barY + 5, GTA_RED);

        // Dinero
        String moneyText = String.format("$%08d", money);
        float moneyScale = 2f;
        int moneyW = (int) (font.width(moneyText) * moneyScale);
        drawScaled(g, font, moneyText, right - moneyW, barY + 10, moneyScale, GTA_GREEN);

        // Icono del arma
        int boxSize = 36;
        int boxX = clockX - boxSize - 8;
        int boxY = top;
        g.fill(boxX - 1, boxY - 1, boxX + boxSize + 1, boxY + boxSize + 1, 0xFF000000);
        g.fill(boxX, boxY, boxX + boxSize, boxY + boxSize, 0x88202020);

        ItemStack held = player.getMainHandItem();
        if (held.isEmpty()) {
            g.drawCenteredString(font, "PU\u00D1OS", boxX + boxSize / 2, boxY + 14, 0xFFFFFFFF);
        } else {
            PoseStack pose = g.pose();
            pose.pushPose();
            pose.translate(boxX + 6, boxY + 6, 0);
            pose.scale(1.5f, 1.5f, 1f);
            g.renderItem(held, 0, 0);
            pose.popPose();

            if (held.getItem() instanceof SaWeaponItem weapon) {
                // Formato GTA: reserva - cargador (ej. 450-34)
                String ammo = weapon.getReserve(held) + "-" + weapon.getClip(held);
                g.drawCenteredString(font, ammo, boxX + boxSize / 2, boxY + boxSize + 3, 0xFFFFFFFF);
            }
        }
    };

    private static void drawScaled(GuiGraphics g, Font font, String text, int x, int y, float scale, int color) {
        PoseStack pose = g.pose();
        pose.pushPose();
        pose.translate(x, y, 0);
        pose.scale(scale, scale, 1f);
        g.drawString(font, text, 0, 0, color, true);
        pose.popPose();
    }
}
