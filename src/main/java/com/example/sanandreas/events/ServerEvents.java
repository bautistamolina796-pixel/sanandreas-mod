package com.example.sanandreas.events;

import com.example.sanandreas.ModItems;
import com.example.sanandreas.SanAndreasMod;
import com.example.sanandreas.item.SaWeaponItem;
import com.example.sanandreas.weapon.WeaponType;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SanAndreasMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerEvents {

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        CompoundTag data = player.getPersistentData();
        CompoundTag persisted = data.getCompound(Player.PERSISTED_NBT_TAG);
        if (!persisted.getBoolean("sa_kit")) {
            giveKit(player, false);
            persisted.putBoolean("sa_kit", true);
            data.put(Player.PERSISTED_NBT_TAG, persisted);
        }
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && !event.isEndConquered()) {
            giveKit(player, false);
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        SaWeaponItem.forget(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("sakit").executes(ctx -> {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            giveKit(player, true);
            ctx.getSource().sendSuccess(() -> Component.literal("Armas y municion de CJ entregadas."), false);
            return 1;
        }));
    }

    /** Entrega las 8 armas en los slots 2-9 de la hotbar (el slot 1 queda libre para los punos). */
    public static void giveKit(ServerPlayer player, boolean refill) {
        Inventory inv = player.getInventory();
        int slot = 1;
        for (WeaponType type : WeaponType.values()) {
            SaWeaponItem item = ModItems.WEAPONS.get(type).get();
            ItemStack existing = findStack(inv, item);
            if (existing != null) {
                if (refill) item.fill(existing);
            } else {
                ItemStack stack = item.getDefaultInstance();
                if (slot < 9 && inv.getItem(slot).isEmpty()) {
                    inv.setItem(slot, stack);
                } else if (!inv.add(stack)) {
                    player.drop(stack, false);
                }
            }
            slot++;
        }
        player.containerMenu.broadcastChanges();
    }

    private static ItemStack findStack(Inventory inv, SaWeaponItem item) {
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (s.is(item)) return s;
        }
        return null;
    }
}
