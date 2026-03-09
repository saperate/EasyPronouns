package dev.saperate.easypronouns;

import dev.saperate.easypronouns.data.EasyPronounsConfig;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;

public class EasyPronouns implements ModInitializer {
    public static final String MODID = "easypronouns";
    
    @Override
    public void onInitialize() {
        AutoConfig.register(EasyPronounsConfig.class, GsonConfigSerializer::new);
    }

    public static EasyPronounsConfig getConfig(){
        return AutoConfig.getConfigHolder(EasyPronounsConfig.class).getConfig();
    }
}
