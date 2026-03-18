package dev.saperate.easypronouns.server;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;

public class ServerEvents {
    public static void registerEvents(){
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            server.getPlayerList().broadcastAll(
                    new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME, handler.player)
            );
        });
    }
}
