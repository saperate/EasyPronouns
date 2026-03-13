package dev.piny.easyPronouns;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PronounsExpansion extends PlaceholderExpansion {

    @Override
    public @NotNull String getIdentifier() {
        return "easypronouns";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Ember <piny.dev>";
    }

    @Override
    public @NotNull String getVersion() {
        return EasyPronouns.getInstance().getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) {
            return "";
        }

        if (params.equalsIgnoreCase("pronouns")) {
            String pronouns = Data.getPronouns(player.getUniqueId());
            return pronouns == null ? "" : pronouns;
        }

        return null;
    }
}
