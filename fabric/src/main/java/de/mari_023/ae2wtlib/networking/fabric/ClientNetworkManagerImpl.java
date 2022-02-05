package de.mari_023.ae2wtlib.networking.fabric;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.resources.ResourceLocation;

import de.mari_023.ae2wtlib.AE2wtlib;
import de.mari_023.ae2wtlib.networking.AE2wtlibPacket;
import de.mari_023.ae2wtlib.networking.c2s.ServerNetworkManager;

public class ClientNetworkManagerImpl {

    public static void registerClientBoundPacket(String name, ServerNetworkManager.PacketDeserializer deserializer) {
        ClientPlayNetworking.registerGlobalReceiver(new ResourceLocation(AE2wtlib.MOD_NAME, name),
                (client, handler, buf, responseSender) -> {
                    if (client.player == null)
                        return;
                    buf.retain();
                    client.execute(() -> {
                        deserializer.create(buf).processPacketData(client.player);
                        buf.release();
                    });
                });
    }

    public static void sendToServer(AE2wtlibPacket packet) {
        ClientPlayNetworking.send(new ResourceLocation(AE2wtlib.MOD_NAME, packet.getPacketName()),
                packet.getPacketBuffer());
    }
}
