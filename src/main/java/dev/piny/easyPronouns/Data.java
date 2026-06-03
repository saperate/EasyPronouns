package dev.piny.easyPronouns;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Data {

    public static class PlayerData {
        public String pronouns;
        public List<String> flags;

        public PlayerData() {
            this.pronouns = "";
            this.flags = new ArrayList<>();
        }

        public PlayerData(String pronouns) {
            this.pronouns = pronouns;
            this.flags = new ArrayList<>();
        }
    }

    public static HashMap<UUID, PlayerData> playerData = new HashMap<>();

    public static void setPronouns(UUID uuid, String str) {
        playerData.computeIfAbsent(uuid, k -> new PlayerData()).pronouns = str;

        EasyPronouns.getInstance().updatePlayerDisplay(Bukkit.getPlayer(uuid));

        try {
            save();
        } catch (IOException e) {
            EasyPronouns.getInstance().getLogger().severe("Failed to save data: " + e.getMessage());
        }
    }

    public static String getPronouns(UUID uuid) {
        PlayerData data = playerData.get(uuid);
        return data == null ? "" : data.pronouns;
    }

    public static void removePronouns(UUID uuid) {
        PlayerData data = playerData.get(uuid);
        if (data != null) {
            data.pronouns = "";
            if (data.flags.isEmpty()) playerData.remove(uuid);
        }
        try {
            save();
        } catch (IOException e) {
            EasyPronouns.getInstance().getLogger().severe("Failed to save data: " + e.getMessage());
        }
    }

    public static List<Flags> getFlags(UUID uuid) {
        PlayerData data = playerData.get(uuid);
        if (data == null) return new ArrayList<>();
        List<Flags> result = new ArrayList<>();
        for (String name : data.flags) {
            try {
                result.add(Flags.valueOf(name));
            } catch (IllegalArgumentException ignored) {}
        }
        return result;
    }

    public static void setFlags(UUID uuid, List<Flags> flags) {
        PlayerData data = playerData.computeIfAbsent(uuid, k -> new PlayerData());
        data.flags = new ArrayList<>();
        for (Flags flag : flags) {
            data.flags.add(flag.name());
        }
        try {
            save();
        } catch (IOException e) {
            EasyPronouns.getInstance().getLogger().severe("Failed to save data: " + e.getMessage());
        }
    }

    public static void addFlag(UUID uuid, Flags flag) {
        PlayerData data = playerData.computeIfAbsent(uuid, k -> new PlayerData());
        String name = flag.name();
        if (!data.flags.contains(name)) {
            data.flags.add(name);
        }
        try {
            save();
        } catch (IOException e) {
            EasyPronouns.getInstance().getLogger().severe("Failed to save data: " + e.getMessage());
        }
    }

    public static void removeFlag(UUID uuid, Flags flag) {
        PlayerData data = playerData.get(uuid);
        if (data != null) {
            data.flags.remove(flag.name());
            if (data.flags.isEmpty() && data.pronouns.isEmpty()) playerData.remove(uuid);
        }
        try {
            save();
        } catch (IOException e) {
            EasyPronouns.getInstance().getLogger().severe("Failed to save data: " + e.getMessage());
        }
    }

    public static void save() throws IOException, com.google.gson.JsonSyntaxException {
        if (playerData.isEmpty()) return;
        Gson gson = new Gson();

        HashMap<String, PlayerData> serialisable = new HashMap<>();
        for (Map.Entry<UUID, PlayerData> entry : playerData.entrySet()) {
            serialisable.put(entry.getKey().toString(), entry.getValue());
        }

        gson.toJson(serialisable);
        String json = gson.toJson(serialisable);

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

        HashMap<String, JsonElement> raw = gson.fromJson(json,
                new TypeToken<HashMap<String, JsonElement>>(){}.getType());

        HashMap<UUID, PlayerData> loaded = new HashMap<>();
        boolean needsMigration = false;

        for (Map.Entry<String, JsonElement> entry : raw.entrySet()) {
            UUID uuid = UUID.fromString(entry.getKey());
            JsonElement value = entry.getValue();

            if (value.isJsonPrimitive()) {
                loaded.put(uuid, new PlayerData(value.getAsString()));
                needsMigration = true;
            } else if (value.isJsonObject()) {
                JsonObject obj = value.getAsJsonObject();
                PlayerData pd = new PlayerData();
                if (obj.has("pronouns") && obj.get("pronouns").isJsonPrimitive()) {
                    pd.pronouns = obj.get("pronouns").getAsString();
                }
                if (obj.has("flags") && obj.get("flags").isJsonArray()) {
                    for (JsonElement flagEl : obj.get("flags").getAsJsonArray()) {
                        if (flagEl.isJsonPrimitive()) {
                            pd.flags.add(flagEl.getAsString());
                        }
                    }
                }
                loaded.put(uuid, pd);
            }
        }

        playerData = loaded;

        if (needsMigration) {
            EasyPronouns.getInstance().getLogger().info(
                    "Migrated data.json from legacy format to the new format (" + loaded.size() + " entries).");
            try {
                save();
            } catch (IOException e) {
                EasyPronouns.getInstance().getLogger().severe("Failed to save migrated data: " + e.getMessage());
            }
        }
    }
}
