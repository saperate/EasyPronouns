package dev.piny.easyPronouns;

import io.papermc.paper.scoreboard.numbers.NumberFormat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;

import java.io.IOException;
import java.util.List;

public final class EasyPronouns extends JavaPlugin {
    private Objective objective;
    public boolean packetEventsEnabled = false;

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        getConfig().setComments("display", List.of(
                "Whenever you are able to format a string you can use any MiniMessage you want (https://webui.advntr.dev/).",
"You can use the following placeholders",
"<pronouns> - The player's pronouns.",
"<player> - The player's name.",
"<flag:n> - The player's pronoun flag in the nth slot (starting at 1). For example, if a player has the flags \"bisexual\" and \"transgender\" and you use <flag:2>, it will display the trans flag. If the player doesn't have a flag in that slot, it will display nothing."
        ));
        saveConfig();
        new Command();
        new Events();

        if (Bukkit.getPluginManager().isPluginEnabled("packetevents")) {
            packetEventsEnabled = true;
            new PacketListener();
        } else {
            getLogger().warning("PacketEvents not found! Some features will be disabled or function differently. Please install PacketEvents for the best experience.");
        }

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new PronounsExpansion().register();
        } else {
            getLogger().warning("PlaceholderAPI not found! Placeholder support will be disabled. Please install PlaceholderAPI for the best experience.");
        }

        try {
            Data.load();
        } catch (IOException e) {
            getLogger().severe("Failed to load data: " + e.getMessage());
            Bukkit.getServer().getPluginManager().disablePlugin(this);
        }

        Bukkit.getScheduler().runTaskLater(this, () -> { // Make sure the world has FULLY loaded before we try to access the scoreboard.
            objective = Bukkit.getScoreboardManager().getMainScoreboard().getObjective("pronouns");
            if (objective == null) {
                objective = Bukkit.getScoreboardManager().getMainScoreboard().registerNewObjective("pronouns", Criteria.DUMMY, Component.empty());
                objective.setDisplaySlot(DisplaySlot.BELOW_NAME);
            }
        }, 1L);
    }

    public void updatePlayerDisplay(Player target) {
        // Probably fine but just in case
        objective = Bukkit.getScoreboardManager().getMainScoreboard().getObjective("pronouns");
        if (objective == null) {
            objective = Bukkit.getScoreboardManager().getMainScoreboard().registerNewObjective("pronouns", Criteria.DUMMY, Component.empty());
            objective.setDisplaySlot(DisplaySlot.BELOW_NAME);
        }

        Score score = objective.getScore(target);
        score.setScore(0);
        score.numberFormat(NumberFormat.fixed(MiniMessage.miniMessage().deserialize(getConfig().getString("display.name.format", "<grey> <pronouns>"),
                Placeholder.component("pronouns", Component.text(Data.getPronouns(target.getUniqueId()))),
                Placeholder.component("player", Component.text(target.getName())),
                Formatters.flagResolver(target.getUniqueId())
        )));

        PacketListener.updateTabDisplay(target);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        try {
            Data.save();
        } catch (IOException e) {
            getLogger().severe("Failed to save data: " + e.getMessage());
        }
    }

    public static EasyPronouns getInstance() {
        return JavaPlugin.getPlugin(EasyPronouns.class);
    }
}
