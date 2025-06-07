package com.github.cargocats.illicitblocks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve(IllicitBlocks.MOD_ID + ".json");
    private static final File CONFIG_FILE = CONFIG_PATH.toFile();

    public static Config config = new Config();

    public static void loadConfig() {
        if (CONFIG_FILE.exists()) {
            try (FileReader fileReader = new FileReader(CONFIG_FILE)) {
                config = GSON.fromJson(fileReader, Config.class);
                IllicitBlocks.LOG.info("Successfully config file");
            } catch (IOException ioExceptionError) {
                IllicitBlocks.LOG.warn("Failed to read config file");
            }
        } else {
            saveConfig();
            IllicitBlocks.LOG.info("No config file, generating blank one");
        }
    }

    public static void saveConfig() {
        try {
            CONFIG_FILE.getParentFile().mkdirs();

            try (FileWriter fileWriter = new FileWriter(CONFIG_FILE)) {
                GSON.toJson(config, fileWriter);
            }
        } catch (IOException ioExceptionError) {
            IllicitBlocks.LOG.warn("Failed to save config file");
        }
    }
}
