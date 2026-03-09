package dev.saperate.easypronouns.server;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.saperate.easypronouns.EasyPronouns;
import dev.saperate.easypronouns.data.Pronouns;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class ServerCommands {
    public static void registerCommands() {
        LiteralArgumentBuilder<ServerCommandSource> root = CommandManager.literal("pronoun");

        addPronounsCommand(root);
        removePronounsCommand(root);

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(root));
    }

    public static void addPronounsCommand(LiteralArgumentBuilder<ServerCommandSource> root) {
        root.then(CommandManager.literal("add").then(
                        CommandManager.argument("pronoun", StringArgumentType.string())
                                .executes(context -> {
                                    ServerPlayerEntity player = context.getSource().getPlayer();
                                    if (player == null) {
                                        return 1;
                                    }
                                    
                                    if(Pronouns.getPlayerData(player).pronounCount() > EasyPronouns.getConfig().getMaxPronouns()){
                                        context.getSource().sendFeedback(() ->
                                                        Text.literal(
                                                                "Too many pronouns! Server allows for up to " 
                                                                        + EasyPronouns.getConfig().getMaxPronouns()),
                                                false);
                                        return 0;
                                    }
                                    String pronoun = context.getArgument("pronoun", String.class);
                                    if(pronoun.length() > EasyPronouns.getConfig().getMaxPronounSize()){
                                        context.getSource().sendFeedback(() ->
                                                        Text.literal(
                                                                "Pronoun is too long! Max character length is "
                                                                        + EasyPronouns.getConfig().getMaxPronouns()),
                                                false);
                                        return 0;
                                    }
                                    
                                    Pronouns.getPlayerData(player).addPronoun(player, pronoun);
                                    context.getSource().getServer().getPlayerManager().sendToAll(
                                            new PlayerListS2CPacket(PlayerListS2CPacket.Action.UPDATE_DISPLAY_NAME, player)
                                    );
                                    context.getSource().sendFeedback(() ->
                                                    Text.literal("Added pronoun: " + pronoun),
                                            false);
                                    return 1;
                                })
                )
        );
    }


    public static void removePronounsCommand(LiteralArgumentBuilder<ServerCommandSource> root) {
        root.then(CommandManager.literal("remove").then(
                        CommandManager.argument("pronoun", StringArgumentType.word())
                                .executes(context -> {
                                    ServerPlayerEntity player = context.getSource().getPlayer();
                                    if (player == null) {
                                        return 1;
                                    }
                                    String pronoun = context.getArgument("pronoun", String.class);
                                    if(!Pronouns.getPlayerData(player).hasPronoun(pronoun)){
                                        context.getSource().sendFeedback(() ->
                                                        Text.literal("Did not have pronoun: " + pronoun),
                                                false);
                                        return 1;
                                    }
                                    
                                    Pronouns.getPlayerData(player).removePronoun(player, pronoun);
                                    context.getSource().getServer().getPlayerManager().sendToAll(
                                            new PlayerListS2CPacket(PlayerListS2CPacket.Action.UPDATE_DISPLAY_NAME, player)
                                    );
                                    context.getSource().sendFeedback(() ->
                                                    Text.literal("Removed pronoun: " + pronoun),
                                            false);
                                    return 1;
                                })
                )
        );
    }
}
