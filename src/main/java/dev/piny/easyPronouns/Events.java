package dev.piny.easyPronouns;

import io.papermc.paper.connection.PlayerGameConnection;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.event.player.AsyncChatEvent;
import io.papermc.paper.event.player.PlayerCustomClickEvent;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class Events implements Listener {
    public Events() {
        Bukkit.getServer().getPluginManager().registerEvents(this, EasyPronouns.getInstance());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        EasyPronouns.getInstance().updatePlayerDisplay(event.getPlayer());
    }

    @EventHandler
    public void handlePronounsDialog(PlayerCustomClickEvent event) {
        if (!event.getIdentifier().equals(Key.key("easypronouns:set/confirm"))) {
            return;
        }

        DialogResponseView view = event.getDialogResponseView();
        if (view == null) {
            return;
        }

        String pronouns = view.getText("pronouns");

        if (event.getCommonConnection() instanceof PlayerGameConnection conn) {
            Player player = conn.getPlayer();
            if (pronouns == null) {
                player.sendRichMessage("<red>You must enter pronouns to set them!");
                return;
            }

            if (pronouns.length() > EasyPronouns.getInstance().getConfig().getInt("maxPronounSize", 16)) {
                player.sendRichMessage("<red>Your pronouns cannot be longer than %s characters!".formatted(EasyPronouns.getInstance().getConfig().getInt("maxPronounSize", 16)));
                return;
            }

            player.sendRichMessage("<green>Your pronouns have been set to: <grey>" + pronouns);
            Data.setPronouns(player.getUniqueId(), pronouns);
        }
    }

    @EventHandler
    public void handleManageDialog(PlayerCustomClickEvent event) {
        if (!event.getIdentifier().equals(Key.key("easypronouns:manage/confirm"))) {
            return;
        }

        DialogResponseView view = event.getDialogResponseView();
        if (view == null) {
            return;
        }

        if (
                view.getFloat("maxSize") == null ||
                        view.getFloat("maxSize").intValue() < 1 ||
                        view.getFloat("maxSize").intValue() > 64
        ) {
            if (event.getCommonConnection() instanceof PlayerGameConnection conn) {
                Player player = conn.getPlayer();
                player.sendRichMessage("<red>Max pronoun size must be between 1 and 64!");
            }
            return;
        }

        if (
                view.getText("nameFormat") == null ||
                        view.getText("nameFormat").isEmpty()
        ) {
            if (event.getCommonConnection() instanceof PlayerGameConnection conn) {
                Player player = conn.getPlayer();
                player.sendRichMessage("<red>Name format cannot be empty!");
            }
            return;
        }

        if (
                view.getText("chatFormat") == null ||
                        view.getText("chatFormat").isEmpty()
        ) {
            if (event.getCommonConnection() instanceof PlayerGameConnection conn) {
                Player player = conn.getPlayer();
                player.sendRichMessage("<red>Chat format cannot be empty!");
            }
            return;
        }

        if (
                view.getText("tabFormat") == null ||
                        view.getText("tabFormat").isEmpty()
        ) {
            if (event.getCommonConnection() instanceof PlayerGameConnection conn) {
                Player player = conn.getPlayer();
                player.sendRichMessage("<red>Tab format cannot be empty!");
            }
            return;
        }

        assert view.getFloat("maxSize") != null;
        int maxSize = view.getFloat("maxSize").intValue();
        EasyPronouns.getInstance().getConfig().set("maxPronounSize", maxSize);

        boolean displayTab = Boolean.TRUE.equals(view.getBoolean("tabToggle"));
        EasyPronouns.getInstance().getConfig().set("display.tab.enabled", displayTab);

        String tabFormat = view.getText("tabFormat");
        EasyPronouns.getInstance().getConfig().set("display.tab.format", tabFormat);
        if (EasyPronouns.getInstance().protocolManager == null) {
            if (event.getCommonConnection() instanceof PlayerGameConnection conn) {
                Player player = conn.getPlayer();
                player.sendRichMessage("<yellow>Since ProtocolLib is not installed, tab list display will not function.</yellow>");
            }
        }

        String belowNameFormat = view.getText("nameFormat");
        EasyPronouns.getInstance().getConfig().set("display.name.format", belowNameFormat);

        boolean displayChat = Boolean.TRUE.equals(view.getBoolean("chatToggle"));
        EasyPronouns.getInstance().getConfig().set("display.chat.enabled", displayChat);

        String chatFormat = view.getText("chatFormat");
        EasyPronouns.getInstance().getConfig().set("display.chat.format", chatFormat);

        EasyPronouns.getInstance().saveConfig();

        Bukkit.getServer().getOnlinePlayers().forEach(player -> EasyPronouns.getInstance().updatePlayerDisplay(player));

        if (event.getCommonConnection() instanceof PlayerGameConnection conn) {
            Player player = conn.getPlayer();
            player.sendRichMessage("<green>Configuration updated!");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void handleChatMessage(AsyncChatEvent event) {
        if (!EasyPronouns.getInstance().getConfig().getBoolean("display.chat.enabled")) return;
        Component message = event.message();
        Player player = event.getPlayer();
        String pronouns = Data.getPronouns(player.getUniqueId());
        if (pronouns.isEmpty()) pronouns = "Not Set";
        String mm = MiniMessage.miniMessage().serialize(message);
        String format = EasyPronouns.getInstance().getConfig().getString("display.chat.format", "<grey>[<pronouns>] <player>: <message>");

        Component finalMessage = MiniMessage.miniMessage().deserialize("<" + format + "> " + mm, Placeholder.component("pronouns", Component.text(pronouns)), Placeholder.component("player", Component.text(player.getName())));
        Bukkit.broadcast(finalMessage);
        event.setCancelled(true);
    }
}