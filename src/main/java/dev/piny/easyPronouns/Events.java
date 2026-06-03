package dev.piny.easyPronouns;

import io.papermc.paper.connection.PlayerGameConnection;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.event.player.AsyncChatEvent;
import io.papermc.paper.event.player.PlayerCustomClickEvent;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.resource.ResourcePackInfo;
import net.kyori.adventure.resource.ResourcePackRequest;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@SuppressWarnings("UnstableApiUsage")
public class Events implements Listener {
    public Events() {
        Bukkit.getServer().getPluginManager().registerEvents(this, EasyPronouns.getInstance());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        EasyPronouns.getInstance().updatePlayerDisplay(event.getPlayer());

        if (EasyPronouns.getInstance().getConfig().getBoolean("flags.enabled", false)) {
            event.getPlayer().sendResourcePacks(Util.createResourcePackRequest());
        }
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
    public void showFlagsDialog(PlayerCustomClickEvent event) {
        if (!event.getIdentifier().equals(Key.key("easypronouns:set/open_flags"))) {
            return;
        }

        if (event.getCommonConnection() instanceof PlayerGameConnection conn) {
            Player player = conn.getPlayer();

            ArrayList<ActionButton> buttons = new ArrayList<>();

            for (Flags flag : Flags.values()) {
                String name = flag.name();
                buttons.add(ActionButton.builder(Component.text(flag.getUnicode(), Style.style().font(Key.key("easypronouns", "flags")).build()).append(Component.text(Util.capitaliseFirstLetter(" "+name.toLowerCase()), Style.style().font(Key.key("minecraft", "default")).build())))
                        .tooltip(Component.text("Sets your flag to " + name.toLowerCase()))
                        .action(DialogAction.customClick(Key.key("easypronouns", "set/set_flag/" + name.toLowerCase()), null))
                        .build());
            }

            Dialog dialog = Dialog.create(builder -> builder.empty()
                    .base(DialogBase.builder(Component.text("Choose Flag"))
                            .inputs(List.of(
                                    DialogInput.numberRange("flagNum", Component.text("Flag Number"), 1, EasyPronouns.getInstance().getConfig().getInt("flags.max", 2)).step(1f).initial(1f).build()
                            ))
                            .build())
                    .type(DialogType.multiAction(buttons).build())
            );

            player.showDialog(dialog);
        }
    }

    @EventHandler
    public void handleSetFlag(PlayerCustomClickEvent event) {
        if (!event.getIdentifier().asString().startsWith("easypronouns:set/set_flag/")) {
            return;
        }

        String[] parts = event.getIdentifier().asString().split("/");
        EasyPronouns.getInstance().getLogger().info("Received parts: " + String.join(", ", parts));
        if (parts.length != 3) { // easypronouns:set is just one part so yes this seems wrong but it's correct trust me
            return;
        }

        String flagName = parts[2].toUpperCase();
        EasyPronouns.getInstance().getLogger().info("Setting flag: " + flagName);
        Flags flag;
        try {
            flag = Flags.valueOf(flagName);
        } catch (IllegalArgumentException e) {
            return;
        }

        DialogResponseView view = event.getDialogResponseView();
        if (view == null) {
            return;
        }

        int flagNum = view.getFloat("flagNum") == null ? 1 : view.getFloat("flagNum").intValue();
        if (flagNum < 1 || flagNum > EasyPronouns.getInstance().getConfig().getInt("flags.max", 2)) {
            if (event.getCommonConnection() instanceof PlayerGameConnection conn) {
                Player player = conn.getPlayer();
                player.sendRichMessage("<red>Flag number must be between 1 and " + EasyPronouns.getInstance().getConfig().getInt("flags.max", 2) + "!");
            }
            return;
        }

        if (event.getCommonConnection() instanceof PlayerGameConnection conn) {
            Player player = conn.getPlayer();
            List<Flags> flags = Data.getFlags(player.getUniqueId());
            while (flags.size() < flagNum) {
                flags.add(null);
            }
            flags.set(flagNum - 1, flag);
            Data.playerData.computeIfAbsent(player.getUniqueId(), k -> new Data.PlayerData()).flags = flags.stream().filter(Objects::nonNull).map(Enum::name).toList();

            player.sendRichMessage("<green>Flag " + flagNum + " set to " + flag.name().toLowerCase() + "!");
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
                        Objects.requireNonNull(view.getFloat("maxSize")).intValue() < 1 ||
                        Objects.requireNonNull(view.getFloat("maxSize")).intValue() > 64
        ) {
            if (event.getCommonConnection() instanceof PlayerGameConnection conn) {
                Player player = conn.getPlayer();
                player.sendRichMessage("<red>Max pronoun size must be between 1 and 64!");
            }
            return;
        }

        if (
                view.getText("nameFormat") == null ||
                        Objects.requireNonNull(view.getText("nameFormat")).isEmpty()
        ) {
            if (event.getCommonConnection() instanceof PlayerGameConnection conn) {
                Player player = conn.getPlayer();
                player.sendRichMessage("<red>Name format cannot be empty!");
            }
            return;
        }

        if (
                view.getText("chatFormat") == null ||
                        Objects.requireNonNull(view.getText("chatFormat")).isEmpty()
        ) {
            if (event.getCommonConnection() instanceof PlayerGameConnection conn) {
                Player player = conn.getPlayer();
                player.sendRichMessage("<red>Chat format cannot be empty!");
            }
            return;
        }

        if (
                view.getText("tabFormat") == null ||
                        Objects.requireNonNull(view.getText("tabFormat")).isEmpty()
        ) {
            if (event.getCommonConnection() instanceof PlayerGameConnection conn) {
                Player player = conn.getPlayer();
                player.sendRichMessage("<red>Tab format cannot be empty!");
            }
            return;
        }

        assert view.getFloat("maxSize") != null;
        int maxSize = view.getFloat("maxSize").intValue();
        EasyPronouns.getInstance().getConfig().set("maxPronounSize", maxSize);

        boolean displayTab = Boolean.TRUE.equals(view.getBoolean("tabToggle"));
        EasyPronouns.getInstance().getConfig().set("display.tab.enabled", displayTab);

        String tabFormat = view.getText("tabFormat");
        EasyPronouns.getInstance().getConfig().set("display.tab.format", tabFormat);
        if (!EasyPronouns.getInstance().packetEventsEnabled) {
            if (event.getCommonConnection() instanceof PlayerGameConnection conn) {
                Player player = conn.getPlayer();
                player.sendRichMessage("<yellow>Since PacketEvents is not installed, tab list display will not function.</yellow>");
            }
        }

        String belowNameFormat = view.getText("nameFormat");
        EasyPronouns.getInstance().getConfig().set("display.name.format", belowNameFormat);

        boolean displayChat = Boolean.TRUE.equals(view.getBoolean("chatToggle"));
        EasyPronouns.getInstance().getConfig().set("display.chat.enabled", displayChat);

        String chatFormat = view.getText("chatFormat");
        EasyPronouns.getInstance().getConfig().set("display.chat.format", chatFormat);

        boolean allowFlags = Boolean.TRUE.equals(view.getBoolean("flagsToggle"));
        boolean existingFlagsAllow = EasyPronouns.getInstance().getConfig().getBoolean("flags.enabled", false);
        EasyPronouns.getInstance().getConfig().set("flags.enabled", allowFlags);

        if (allowFlags && !existingFlagsAllow) {
            Bukkit.getServer().getOnlinePlayers().forEach(player -> player.sendResourcePacks(Util.createResourcePackRequest().replace(true)));
        }

        float maxFlags = view.getFloat("maxFlags") == null ? 2 : view.getFloat("maxFlags");
        EasyPronouns.getInstance().getConfig().set("flags.max", (int) maxFlags);

        EasyPronouns.getInstance().saveConfig();

        Bukkit.getServer().getOnlinePlayers().forEach(player -> EasyPronouns.getInstance().updatePlayerDisplay(player));

        if (event.getCommonConnection() instanceof PlayerGameConnection conn) {
            Player player = conn.getPlayer();
            player.sendRichMessage("<green>Configuration updated!");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void handleChatMessage(AsyncChatEvent event) {
        if (!EasyPronouns.getInstance().getConfig().getBoolean("display.chat.enabled")) return;
        Component message = event.message();
        Player player = event.getPlayer();
        String pronouns = Data.getPronouns(player.getUniqueId());
        if (pronouns.isEmpty()) pronouns = "Not Set";
        String mm = MiniMessage.miniMessage().serialize(message);
        String format = EasyPronouns.getInstance().getConfig().getString("display.chat.format", "<grey>[<pronouns>] <player>: <message>");

        Component finalMessage = MiniMessage.miniMessage().deserialize("<" + format + "> " + mm,
                Placeholder.component("pronouns", Component.text(pronouns)),
                Placeholder.component("player", Component.text(player.getName())),
                Formatters.flagResolver(player.getUniqueId())
        );
        Bukkit.broadcast(finalMessage);
        event.setCancelled(true);
    }
}