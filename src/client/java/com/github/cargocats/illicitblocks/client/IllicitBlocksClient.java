package com.github.cargocats.illicitblocks.client;

import com.github.cargocats.illicitblocks.ConfigManager;
import com.github.cargocats.illicitblocks.IllicitBlocks;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
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
    }

    private void dumpBlocks(MinecraftClient client) {
        ConfigManager.config.static_list =
                IllicitBlocks.blocksToHandle.stream().map(Identifier::toString)
                        .collect(Collectors.toCollection(ArrayList::new));

        ConfigManager.saveConfig();

        IllicitBlocks.LOG.info("Dumped blocks to static_list for config");
    }
}
