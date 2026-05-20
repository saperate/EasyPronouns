package dev.saperate.easypronouns;

import dev.saperate.easypronouns.data.EasyPronounsConfig;
import dev.saperate.easypronouns.data.Pronouns;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;

import java.util.function.Function;

public class EasyPronouns implements ModInitializer {
    public static final String MODID = "easypronouns";
    
    @Override
    public void onInitialize() {
        AutoConfig.register(EasyPronounsConfig.class, GsonConfigSerializer::new);
    }
    

    public static EasyPronounsConfig getConfig(){
        return AutoConfig.getConfigHolder(EasyPronounsConfig.class).getConfig();
    }     
    
    public static void updateNameplate(MinecraftServer server, ServerPlayer player){
        Scoreboard scoreboard = server.getScoreboard();
        String name = player.getPlainTextName();

        Pronouns.PronounsData pronounsData = Pronouns.getPlayerData(player);
        if (pronounsData.isEmpty(player)) {
            return;
        }

        PlayerTeam team;
        if (scoreboard.getPlayerTeam(name) == null) {
            team = scoreboard.addPlayerTeam(name);
            team.setDisplayName(player.getDisplayName());
        } else {
            team = scoreboard.getPlayerTeam(name);
        }
        if (team != null) {
            if(!getConfig().displaysOnNameplate()){ 
                // If the option is toggled off, we remove the team
                scoreboard.removePlayerTeam(team);
            }
            
            team.setPlayerSuffix(Component.literal
                    (" (" + pronounsData.getPronounsAsString() + ")"));
            scoreboard.addPlayerToTeam(player.getPlainTextName(), team);
        }
    }
}
