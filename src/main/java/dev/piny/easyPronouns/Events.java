package dev.piny.easyPronouns;

import io.papermc.paper.connection.PlayerGameConnection;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.event.player.PlayerCustomClickEvent;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
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

        assert view.getFloat("maxSize") != null;
        int maxSize = view.getFloat("maxSize").intValue();
        String belowNameFormat = view.getText("nameFormat");

        EasyPronouns.getInstance().getConfig().set("maxPronounSize", maxSize);
        EasyPronouns.getInstance().getConfig().set("display.name.format", belowNameFormat);
        EasyPronouns.getInstance().saveConfig();

        Bukkit.getServer().getOnlinePlayers().forEach(player -> EasyPronouns.getInstance().updatePlayerDisplay(player));

        if (event.getCommonConnection() instanceof PlayerGameConnection conn) {
            Player player = conn.getPlayer();
            player.sendRichMessage("<green>Configuration updated!");
        }
    }
}