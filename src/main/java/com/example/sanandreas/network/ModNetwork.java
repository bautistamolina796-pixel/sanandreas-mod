package com.example.sanandreas.network;

import com.example.sanandreas.SanAndreasMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModNetwork {
    private static final String PROTOCOL = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(SanAndreasMod.MOD_ID, "main"),
            () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);

    public static void register() {
        CHANNEL.registerMessage(0, FirePacket.class, FirePacket::encode, FirePacket::decode, FirePacket::handle);
        CHANNEL.registerMessage(1, ReloadPacket.class, ReloadPacket::encode, ReloadPacket::decode, ReloadPacket::handle);
    }
}
