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

        if (params.toLowerCase().startsWith("flag_")) {
            String[] parts = params.split("_");
            int index = Integer.parseInt(parts[1]);

            if (index < 1) {
                return "";
            }

            if (parts.length == 2) {
                return Data.getFlags(player.getUniqueId()).stream()
                        .skip(index - 1)
                        .findFirst()
                        .map(Flags::getUnicode)
                        .orElse("");
            }

            String mode = parts[2].toLowerCase();

            if (mode.equalsIgnoreCase("rich")) {
                return Data.getFlags(player.getUniqueId()).stream()
                        .skip(index - 1)
                        .findFirst()
                        .map(flag -> "<font:easypronouns:flags>" + flag.getUnicode() + "</font>")
                        .orElse("");
            }

            if (mode.equalsIgnoreCase("name")) {
                return Data.getFlags(player.getUniqueId()).stream()
                        .skip(index - 1)
                        .findFirst()
                        .map(Flags::getName)
                        .orElse("");
            }
        }

        return null;
    }

    @Override
    public boolean persist() {
        return true;
    }
}
