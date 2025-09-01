package net.Neomoon.dronebox;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

public class ClientConfig {

	private static final Path CONFIG_PATH = FabricLoader.getInstance()
		.getConfigDir().resolve("my_client_data.json");
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static JsonObject data = new JsonObject();

	public static void load() {
		if (Files.exists(CONFIG_PATH)) {
			try {
				String content = Files.readString(CONFIG_PATH);
				data = GSON.fromJson(content, JsonObject.class);
				if (data == null) data = new JsonObject();
			} catch (IOException e) {
				e.printStackTrace();
				data = new JsonObject();
			}
		} else {
			data = new JsonObject();
		}
	}

	public static void save() {
		try {
			Files.writeString(CONFIG_PATH, GSON.toJson(data));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void addDefault(String key, JsonElement value) {
		if (!data.has(key)) {
			data.add(key, value);
		}
	}

	public static void addDefault(String key, String value) {
		addDefault(key, GSON.toJsonTree(value));
	}

	public static void addDefault(String key, int value) {
		addDefault(key, GSON.toJsonTree(value));
	}

	public static void addDefault(String key, boolean value) {
		addDefault(key, GSON.toJsonTree(value));
	}

	public static void addDefault(String key, double value) {
		addDefault(key, GSON.toJsonTree(value));
	}

	public static String getString(String key, String fallback) {
		return data.has(key) ? data.get(key).getAsString() : fallback;
	}

	public static int getInt(String key, int fallback) {
		return data.has(key) ? data.get(key).getAsInt() : fallback;
	}

	public static boolean getBoolean(String key, boolean fallback) {
		return data.has(key) ? data.get(key).getAsBoolean() : fallback;
	}

	public static double getDouble(String key, double fallback) {
		return data.has(key) ? data.get(key).getAsDouble() : fallback;
	}

	public static Set<String> getKeys() {
		return data.keySet();
	}

	public static boolean has(String key) {
		return data.has(key);
	}

	public static void set(String key, JsonElement value) {
		data.add(key, value);
	}

	public static void remove(String key) {
		data.remove(key);
	}
}
