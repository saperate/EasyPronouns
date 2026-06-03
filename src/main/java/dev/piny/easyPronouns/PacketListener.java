package dev.piny.easyPronouns;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoUpdate;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

public class PacketListener extends PacketListenerAbstract {

    public PacketListener() {
        PacketEvents.getAPI().getEventManager().registerListeners(this);
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() != PacketType.Play.Server.PLAYER_INFO_UPDATE) return;

        if (!EasyPronouns.getInstance().getConfig().getBoolean("display.tab.enabled", false)) return;

        WrapperPlayServerPlayerInfoUpdate packet = new WrapperPlayServerPlayerInfoUpdate(event);
        EnumSet<WrapperPlayServerPlayerInfoUpdate.Action> actions = packet.getActions();

        if (!actions.contains(WrapperPlayServerPlayerInfoUpdate.Action.ADD_PLAYER)
                && !actions.contains(WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_DISPLAY_NAME)) {
            return;
        }

        String tabFormat = EasyPronouns.getInstance().getConfig().getString(
                "display.tab.format", "<grey>[<pronouns>]<white> <player>");

        List<WrapperPlayServerPlayerInfoUpdate.PlayerInfo> entries = packet.getEntries();
        List<WrapperPlayServerPlayerInfoUpdate.PlayerInfo> modified = new ArrayList<>();
        boolean changed = false;

        for (WrapperPlayServerPlayerInfoUpdate.PlayerInfo info : entries) {
            UUID profileId = info.getGameProfile().getUUID();
            String pronouns = Data.getPronouns(profileId);

            if (pronouns.isEmpty()) {
                modified.add(info);
                continue;
            }

            Player onlinePlayer = Bukkit.getPlayer(profileId);
            String playerName = onlinePlayer != null ? onlinePlayer.getName() : profileId.toString();

            Component displayComponent = MiniMessage.miniMessage().deserialize(
                    tabFormat,
                    Placeholder.component("pronouns", Component.text(pronouns)),
                    Placeholder.component("player", Component.text(playerName))
            );

            WrapperPlayServerPlayerInfoUpdate.PlayerInfo newInfo = new WrapperPlayServerPlayerInfoUpdate.PlayerInfo(
                    info.getGameProfile(),
                    info.isListed(),
                    info.getLatency(),
                    info.getGameMode(),
                    displayComponent,
                    info.getChatSession()
            );
            modified.add(newInfo);
            changed = true;
        }

        if (changed) {
            packet.setEntries(modified);
        }
    }

    /**
     * Sends an UPDATE_DISPLAY_NAME player info packet to all online players causing the tab list to reflect the target player's current pronouns.
     */
    public static void updateTabDisplay(Player target) {
        if (!EasyPronouns.getInstance().packetEventsEnabled) return;
        if (!EasyPronouns.getInstance().getConfig().getBoolean("display.tab.enabled", false)) return;

        String pronouns = Data.getPronouns(target.getUniqueId());
        String tabFormat = EasyPronouns.getInstance().getConfig().getString(
                "display.tab.format", "<grey>[<pronouns>]<white> <player>");

        Component displayName = null;
        if (!pronouns.isEmpty()) {
            displayName = MiniMessage.miniMessage().deserialize(
                    tabFormat,
                    Placeholder.component("pronouns", Component.text(pronouns)),
                    Placeholder.component("player", Component.text(target.getName()))
            );
        }

        UserProfile profile = new UserProfile(target.getUniqueId(), target.getName());
        GameMode gameMode = GameMode.valueOf(target.getGameMode().name());

        WrapperPlayServerPlayerInfoUpdate.PlayerInfo playerInfo = new WrapperPlayServerPlayerInfoUpdate.PlayerInfo(
                profile,
                true,
                target.getPing(),
                gameMode,
                displayName,
                null
        );

        WrapperPlayServerPlayerInfoUpdate wrapper = new WrapperPlayServerPlayerInfoUpdate(
                EnumSet.of(WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_DISPLAY_NAME),
                List.of(playerInfo)
        );

        for (Player viewer : Bukkit.getOnlinePlayers()) {
            PacketEvents.getAPI().getPlayerManager().sendPacket(viewer, wrapper);
        }
    }
}
