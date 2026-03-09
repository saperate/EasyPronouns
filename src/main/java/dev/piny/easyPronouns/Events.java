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
}