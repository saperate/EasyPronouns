package dev.piny.easyPronouns;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class PacketListener implements Listener {
    private final ProtocolManager protocolManager = EasyPronouns.getInstance().protocolManager;

    public PacketListener() {
        Bukkit.getServer().getPluginManager().registerEvents(this, EasyPronouns.getInstance());

        if (protocolManager == null) return;

        protocolManager.addPacketListener(new PacketAdapter(
                EasyPronouns.getInstance(),
                PacketType.Play.Server.PLAYER_INFO
        ) {
            @Override
            public void onPacketSending(PacketEvent event) {
                // Only act on packets that include display name information
                Set<EnumWrappers.PlayerInfoAction> actions = event.getPacket().getPlayerInfoActions().read(0);
                if (!actions.contains(EnumWrappers.PlayerInfoAction.ADD_PLAYER)
                        && !actions.contains(EnumWrappers.PlayerInfoAction.UPDATE_DISPLAY_NAME)) {
                    return;
                }

                if (!EasyPronouns.getInstance().getConfig().getBoolean("display.tab.enabled", false)) {
                    return;
                }

                String tabFormat = EasyPronouns.getInstance().getConfig().getString(
                        "display.tab.format", "<grey>[<pronouns>]<white> <player>");

                List<PlayerInfoData> dataList = event.getPacket().getPlayerInfoDataLists().read(1);
                List<PlayerInfoData> modified = new ArrayList<>();

                for (PlayerInfoData data : dataList) {
                    if (data == null) {
                        modified.add(null);
                        continue;
                    }
                    UUID profileId = data.getProfileId();
                    String pronouns = Data.getPronouns(profileId);

                    if (pronouns.isEmpty()) {
                        modified.add(data);
                        continue;
                    }

                    org.bukkit.entity.Player onlinePlayer = org.bukkit.Bukkit.getPlayer(profileId);
                    String playerName = onlinePlayer != null ? onlinePlayer.getName() : profileId.toString();

                    Component displayComponent = MiniMessage.miniMessage().deserialize(
                            tabFormat,
                            Placeholder.component("pronouns", Component.text(pronouns)),
                            Placeholder.component("player", Component.text(playerName))
                    );

                    String json = JSONComponentSerializer.json().serialize(displayComponent);
                    WrappedChatComponent wrappedDisplay = WrappedChatComponent.fromJson(json);

                    PlayerInfoData newData = new PlayerInfoData(
                            profileId,
                            data.getLatency(),
                            data.isListed(),
                            data.getGameMode(),
                            data.getProfile(),
                            wrappedDisplay,
                            data.isShowHat(),
                            data.getListOrder(),
                            data.getRemoteChatSessionData()
                    );
                    modified.add(newData);
                }

                event.getPacket().getPlayerInfoDataLists().write(1, modified);
            }
        });
    }

    /**
     * Sends an UPDATE_DISPLAY_NAME player info packet to all online players causing the tab list to reflect the target player's current pronouns.
     */
    public static void updateTabDisplay(Player target) {
        if (EasyPronouns.getInstance().protocolManager == null) return;
        if (!EasyPronouns.getInstance().getConfig().getBoolean("display.tab.enabled", false)) return;

        String pronouns = Data.getPronouns(target.getUniqueId());
        String tabFormat = EasyPronouns.getInstance().getConfig().getString(
                "display.tab.format", "<grey>[<pronouns>]<white> <player>");

        WrappedChatComponent displayName;
        if (pronouns.isEmpty()) {
            displayName = null;
        } else {
            Component displayComponent = MiniMessage.miniMessage().deserialize(
                    tabFormat,
                    Placeholder.component("pronouns", Component.text(pronouns)),
                    Placeholder.component("player", Component.text(target.getName()))
            );
            displayName = WrappedChatComponent.fromJson(JSONComponentSerializer.json().serialize(displayComponent));
        }

        WrappedGameProfile profile = WrappedGameProfile.fromPlayer(target);
        PlayerInfoData infoData = new PlayerInfoData(
                target.getUniqueId(),
                target.getPing(),
                true,
                EnumWrappers.NativeGameMode.fromBukkit(target.getGameMode()),
                profile,
                displayName,
                false,
                0,
                null
        );

        PacketContainer packet = new PacketContainer(PacketType.Play.Server.PLAYER_INFO);
        packet.getPlayerInfoActions().write(0, EnumSet.of(EnumWrappers.PlayerInfoAction.UPDATE_DISPLAY_NAME));
        packet.getPlayerInfoDataLists().write(1, List.of(infoData));

        for (Player viewer : Bukkit.getOnlinePlayers()) {
            EasyPronouns.getInstance().protocolManager.sendServerPacket(viewer, packet);
        }
    }
}
