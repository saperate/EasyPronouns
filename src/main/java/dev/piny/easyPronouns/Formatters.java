package dev.piny.easyPronouns;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.List;
import java.util.UUID;

public class Formatters {
    public static TagResolver flagResolver(UUID uuid) {
        return TagResolver.resolver("flag", (argumentQueue, context) -> {
            int index = Integer.parseInt(argumentQueue.popOr("flag index required").value()) - 1;
            List<Flags> playerFlags = Data.getFlags(uuid);
            if (index < 0 || index >= playerFlags.size()) {
                return Tag.selfClosingInserting(Component.empty());
            }
            Component flagComponent = Component.text(playerFlags.get(index).getUnicode())
                    .style(Style.style().font(Key.key("easypronouns:flags")).build());
            return Tag.selfClosingInserting(flagComponent);
        });
    }
}
