package dev.saperate.easypronouns.server;

import dev.saperate.easypronouns.data.EasyPronounsConfig;
import dev.saperate.easypronouns.data.Pronouns;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;

public class EasyPronounsServer implements DedicatedServerModInitializer {
    @Override
    public void onInitializeServer() {
        ServerCommands.registerCommands();
        ServerEvents.registerEvents();
        Pronouns.InitialiseDataTypes();
    }
}
