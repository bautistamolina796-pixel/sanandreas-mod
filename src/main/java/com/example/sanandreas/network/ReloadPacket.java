package com.example.sanandreas.network;

import com.example.sanandreas.item.SaWeaponItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Cliente -> servidor: tecla R. */
public class ReloadPacket {
    public ReloadPacket() {}

    public static void encode(ReloadPacket msg, FriendlyByteBuf buf) {}

    public static ReloadPacket decode(FriendlyByteBuf buf) {
        return new ReloadPacket();
    }

    public static void handle(ReloadPacket msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context c = ctx.get();
        c.enqueueWork(() -> {
            ServerPlayer player = c.getSender();
            if (player == null) return;
            ItemStack stack = player.getMainHandItem();
            if (stack.getItem() instanceof SaWeaponItem weapon) {
                weapon.reload(player, stack);
            }
        });
        c.setPacketHandled(true);
    }
}
