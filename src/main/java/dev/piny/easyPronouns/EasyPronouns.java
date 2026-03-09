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

public final class EasyPronouns extends JavaPlugin {

    private Objective objective;

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        new Command();
        new Events();

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
        score.numberFormat(NumberFormat.fixed(MiniMessage.miniMessage().deserialize(getConfig().getString("display.name.format", "<grey> <pronouns>"), Placeholder.component("pronouns", Component.text(Data.getPronouns(target.getUniqueId()))), Placeholder.component("player", Component.text(target.getName())))));
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
