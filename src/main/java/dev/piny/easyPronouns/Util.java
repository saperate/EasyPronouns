package dev.piny.easyPronouns;

import net.kyori.adventure.resource.ResourcePackInfo;
import net.kyori.adventure.resource.ResourcePackRequest;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.net.URI;
import java.util.UUID;

public class Util {
    public static String capitaliseFirstLetter(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public static ResourcePackRequest createResourcePackRequest() {
        // RESOURCE PACK SETTINGS
        return ResourcePackRequest.resourcePackRequest()
                .required(true)
                .prompt(
                        Component.text("This server uses a plugin called EasyPronouns that requires a resource pack to display pronoun flags.").append(
                                Component.text("\nIt will be loaded from media.piny.dev.").color(NamedTextColor.DARK_GRAY)
                        )
                )
                .packs(
                        ResourcePackInfo.resourcePackInfo(
                                UUID.fromString("057de9f2-3f96-40be-921b-9eea821118eb"),
                                URI.create("https://media.piny.dev/modrinth/EasyPronouns/pack/1.3.0.zip"),
                                "c1dd2a1826f2f9743475cade24d507134844a99d"
                        )
                )
                .build();
    }
}
