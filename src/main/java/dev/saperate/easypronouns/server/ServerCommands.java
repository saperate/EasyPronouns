package dev.saperate.easypronouns.server;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.saperate.easypronouns.EasyPronouns;
import dev.saperate.easypronouns.data.Pronouns;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class ServerCommands {
    public static void registerCommands() {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("pronoun");

        addPronounsCommand(root);
        removePronounsCommand(root);

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(root));
    }

    public static void addPronounsCommand(LiteralArgumentBuilder<CommandSourceStack> root) {
        root.then(Commands.literal("add").then(
                        Commands.argument("pronoun", StringArgumentType.string())
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayer();
                                    if (player == null) {
                                        return 1;
                                    }
                                    
                                    if(Pronouns.getPlayerData(player).pronounCount() > EasyPronouns.getConfig().getMaxPronouns()){
                                        context.getSource().sendSuccess(() ->
                                                        Component.literal(
                                                                "Too many pronouns! Server allows for up to " 
                                                                        + EasyPronouns.getConfig().getMaxPronouns()),
                                                false);
                                        return 0;
                                    }
                                    String pronoun = context.getArgument("pronoun", String.class);
                                    if(pronoun.length() > EasyPronouns.getConfig().getMaxPronounSize()){
                                        context.getSource().sendSuccess(() ->
                                                        Component.literal(
                                                                "Pronoun is too long! Max character length is "
                                                                        + EasyPronouns.getConfig().getMaxPronouns()),
                                                false);
                                        return 0;
                                    }
                                    
                                    Pronouns.getPlayerData(player).addPronoun(player, pronoun);
                                    context.getSource().getServer().getPlayerList().broadcastAll(
                                            new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME, player)
                                    );
                                    context.getSource().sendSuccess(() ->
                                                    Component.literal("Added pronoun: " + pronoun),
                                            false);
                                    return 1;
                                })
                )
        );
    }


    public static void removePronounsCommand(LiteralArgumentBuilder<CommandSourceStack> root) {
        root.then(Commands.literal("remove").then(
                        Commands.argument("pronoun", StringArgumentType.word())
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayer();
                                    if (player == null) {
                                        return 1;
                                    }
                                    String pronoun = context.getArgument("pronoun", String.class);
                                    if(!Pronouns.getPlayerData(player).hasPronoun(pronoun)){
                                        context.getSource().sendSuccess(() ->
                                                        Component.literal("Did not have pronoun: " + pronoun),
                                                false);
                                        return 1;
                                    }
                                    
                                    Pronouns.getPlayerData(player).removePronoun(player, pronoun);
                                    context.getSource().getServer().getPlayerList().broadcastAll(
                                            new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME, player)
                                    );
                                    context.getSource().sendSuccess(() ->
                                                    Component.literal("Removed pronoun: " + pronoun),
                                            false);
                                    return 1;
                                })
                )
        );
    }
}
