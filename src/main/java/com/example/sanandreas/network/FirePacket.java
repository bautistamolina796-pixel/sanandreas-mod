package com.example.sanandreas.network;

import com.example.sanandreas.item.SaWeaponItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Cliente -> servidor: "estoy manteniendo el boton de disparo". */
public class FirePacket {
    public FirePacket() {}

    public static void encode(FirePacket msg, FriendlyByteBuf buf) {}

    public static FirePacket decode(FriendlyByteBuf buf) {
        return new FirePacket();
    }

    public static void handle(FirePacket msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context c = ctx.get();
        c.enqueueWork(() -> {
            ServerPlayer player = c.getSender();
            if (player == null) return;
            ItemStack stack = player.getMainHandItem();
            if (stack.getItem() instanceof SaWeaponItem weapon) {
                weapon.tryFire(player, stack);
            }
        });
        c.setPacketHandled(true);
    }
}
