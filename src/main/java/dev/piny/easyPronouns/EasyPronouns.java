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

    @Override
    public void onEnable() {
        // Plugin startup logic
        Objective objective = Bukkit.getScoreboardManager().getMainScoreboard().getObjective("pronouns");
        if (objective == null) {
            objective = Bukkit.getScoreboardManager().getMainScoreboard().registerNewObjective("pronouns", Criteria.DUMMY, Component.empty());
            objective.setDisplaySlot(DisplaySlot.BELOW_NAME);
        }

        saveDefaultConfig();

        new Events();
        new Command();

        try {
            Data.load();
        } catch (IOException e) {
            getLogger().severe("Failed to load data: " + e.getMessage());
            Bukkit.getServer().getPluginManager().disablePlugin(this);
        }
    }

    Objective objective = Bukkit.getScoreboardManager().getMainScoreboard().getObjective("pronouns");

    public void updatePlayerDisplay(Player target) {
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
