package com.github.cargocats.illicitblocks.client;

import com.github.cargocats.illicitblocks.ConfigManager;
import com.github.cargocats.illicitblocks.IllicitBlocks;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class IllicitBlocksClient implements ClientModInitializer {
    public static final int DUMP_KEYCODE = GLFW.GLFW_KEY_RIGHT_BRACKET;
    private static boolean wasPressed = false;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client == null || client.getWindow() == null) return;

            long windowHandle = client.getWindow().getHandle();
            boolean isDown = GLFW.glfwGetKey(windowHandle, DUMP_KEYCODE) == GLFW.GLFW_PRESS;

            if (isDown && !wasPressed) {
                dumpBlocks(client);
            }

            wasPressed = isDown;
        });

        ModelLoadingPlugin.register(new IllicitModelPlugin());
    }

    private void dumpBlocks(MinecraftClient client) {
        ArrayList<Identifier> blocksToHandle = new ArrayList<>(IllicitBlocks.blocksToHandle);
        blocksToHandle.removeIf(blockId -> {
            boolean containedId = Registries.ITEM.containsId(blockId);
            IllicitBlocks.LOG.info("Block {} contained id now? {}", blockId, containedId);
            return containedId;
        });

        ConfigManager.config.static_list =
                blocksToHandle.stream().map(Identifier::toString)
                        .collect(Collectors.toCollection(ArrayList::new));

        ConfigManager.saveConfig();

        IllicitBlocks.LOG.info("Dumped blocks to static_list for config");
    }
}
