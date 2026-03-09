package dev.piny.easyPronouns;

import com.mojang.brigadier.arguments.StringArgumentType;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;

public class Command {
    public Command() {
        EasyPronouns.getInstance().getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> commands.registrar().register(
                Commands.literal("pronoun")
                        .then(Commands.literal("manage")
                                .then(Commands.literal("set")
                                        .then(Commands.argument("player", ArgumentTypes.player())
                                        .then(Commands.argument("pronouns", StringArgumentType.string())
                                        .requires(source -> source.getSender().hasPermission("easypronouns.manage"))
                                        .executes(context -> {
                                            String pronouns = StringArgumentType.getString(context, "pronouns");
                                            if (pronouns.length() > EasyPronouns.getInstance().getConfig().getInt("maxPronounSize", 16)) {
                                                context.getSource().getSender().sendRichMessage("<red>Pronouns cannot be longer than %s characters!".formatted(EasyPronouns.getInstance().getConfig().getInt("maxPronounSize", 16)));
                                                return 0;
                                            }

                                            final PlayerSelectorArgumentResolver targetResolver = context.getArgument("player", PlayerSelectorArgumentResolver.class);
                                            final Player target = targetResolver.resolve(context.getSource()).getFirst();

                                            Data.setPronouns(target.getUniqueId(), pronouns);

                                            return 1;
                                        })))
                                )
                                .then(Commands.literal("reload")
                                        .requires(source -> source.getSender().hasPermission("easypronouns.manage"))
                                        .executes(context -> {
                                            EasyPronouns.getInstance().reloadConfig();
                                            context.getSource().getSender().sendRichMessage("<green>Configuration reloaded!");
                                            return 1;
                                        })
                                )
                                .requires(source -> source.getSender().hasPermission("easypronouns.manage"))
                                .executes(context -> {
                                    Dialog dialog = Dialog.create(builder -> builder.empty()
                                            .base(DialogBase.builder(Component.text("EasyPronouns Config"))
                                                    .inputs(List.of(
                                                            DialogInput.numberRange(
                                                                    "maxSize",
                                                                    Component.text("Max pronoun size"),
                                                                    1,
                                                                    64
                                                            ).step(1f).initial(Float.parseFloat(String.valueOf(EasyPronouns.getInstance().getConfig().getInt("maxPronounSize", 16)))).build(),
//                                                            DialogInput.bool("tabToggle", Component.text("Tab Display?"))
//                                                                    .initial(EasyPronouns.getInstance().getConfig().getBoolean("display.tab.enabled", false))
//                                                                    .build(),
//                                                            DialogInput.text("tabFormat", Component.text("Tab Display Format (<pronouns> / <player> are available.)"))
//                                                                    .initial(EasyPronouns.getInstance().getConfig().getString("display.tab.format", "<grey><pronouns><white> <player>"))
//                                                                    .maxLength(256) // Probably way bigger than actually allowed, but we need to consider formatting
//                                                                    .build(),
                                                            DialogInput.text("nameFormat", Component.text("Below Name Format (<pronouns> is available.)"))
                                                                    .initial(EasyPronouns.getInstance().getConfig().getString("display.name.format", "<grey> <pronouns>"))
                                                                    .maxLength(256)
                                                                    .build()
                                                    )).build()
                                            )
                                            .type(DialogType.confirmation(
                                                    ActionButton.create(
                                                            Component.text("Submit").color(NamedTextColor.GREEN),
                                                            Component.text("Save the configuration"),
                                                            100,
                                                            DialogAction.customClick(Key.key("easypronouns:manage/confirm"), null)
                                                    ),
                                                    ActionButton.create(
                                                            Component.text("Cancel").color(NamedTextColor.RED),
                                                            Component.text("Discard your changes"),
                                                            100,
                                                            null // Just close
                                                    )
                                            ))
                                    );

                                    Objects.requireNonNull(context.getSource().getExecutor()).showDialog(dialog);

                                    return 1;
                                }))
                        .executes(context -> {
                            Dialog dialog = Dialog.create(builder -> builder.empty()
                                    .base(DialogBase.builder(Component.text("EasyPronouns - Set Pronouns"))
                                            .inputs(List.of(
                                                    DialogInput.text("pronouns", Component.text("Enter your pronouns"))
                                                            .initial(Data.getPronouns(context.getSource().getExecutor().getUniqueId()))
                                                            .maxLength(EasyPronouns.getInstance().getConfig().getInt("maxPronounSize", 16))
                                                            .build()
                                            )).build()
                                    )
                                    .type(DialogType.confirmation(
                                            ActionButton.create(
                                                    Component.text("Submit").color(NamedTextColor.GREEN),
                                                    Component.text("Save your pronouns"),
                                                    100,
                                                    DialogAction.customClick(Key.key("easypronouns:set/confirm"), null)
                                            ),
                                            ActionButton.create(
                                                    Component.text("Cancel").color(NamedTextColor.RED),
                                                    Component.text("Discard your changes"),
                                                    100,
                                                    null // Just close
                                            )
                                    ))
                            );

                            Objects.requireNonNull(context.getSource().getExecutor()).showDialog(dialog);

                            return 1;
                        })
                        .build()
        ));
    }
}
