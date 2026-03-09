package dev.saperate.easypronouns.server;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;

public class ServerEvents {
    public static void registerEvents(){
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            server.getPlayerManager().sendToAll(
                    new PlayerListS2CPacket(PlayerListS2CPacket.Action.UPDATE_DISPLAY_NAME, handler.player)
            );
        });
    }
}
