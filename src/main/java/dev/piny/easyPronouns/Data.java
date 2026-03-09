package dev.piny.easyPronouns;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Data {
    public static HashMap<UUID, String> pronouns = new HashMap<>();

    public static void setPronouns(UUID uuid, String str) {
        pronouns.put(uuid, str);

        EasyPronouns.getInstance().updatePlayerDisplay(Bukkit.getPlayer(uuid));

        try {
            save();
        } catch (IOException e) {
            EasyPronouns.getInstance().getLogger().severe("Failed to save data: " + e.getMessage());
        }
    }

    public static String getPronouns(UUID uuid) {
        return pronouns.getOrDefault(uuid, "");
    }

    public static void removePronouns(UUID uuid) {
        pronouns.remove(uuid);
        try {
            save();
        } catch (IOException e) {
            EasyPronouns.getInstance().getLogger().severe("Failed to save data: " + e.getMessage());
        }
    }

    public static void save() throws IOException, com.google.gson.JsonSyntaxException {
        if (pronouns.isEmpty()) return; // Don't save if there's no data to save.
        Gson gson = new Gson();
        gson.toJson(pronouns); // Check if we messed up the data before writing it.
        String json = gson.toJson(pronouns);

        File file = new File(EasyPronouns.getInstance().getDataFolder(), "data.json");

        Files.write(file.toPath(), json.getBytes());
    }

    public static void load() throws IOException, com.google.gson.JsonSyntaxException {
        File file = new File(EasyPronouns.getInstance().getDataFolder(), "data.json");
        if (!file.exists()) {
            return;
        }

        String json = new String(Files.readAllBytes(file.toPath()));
        Gson gson = new Gson();
        // Throw any errors before we set the pronouns, this is to prevent data loss if the file is corrupted
        HashMap<String, String> raw = gson.fromJson(json, new TypeToken<HashMap<String, String>>(){}.getType());

        HashMap<UUID, String> loaded = new HashMap<>();
        for (Map.Entry<String, String> entry : raw.entrySet()) {
            loaded.put(UUID.fromString(entry.getKey()), entry.getValue());
        }
        pronouns = loaded;
    }
}
