package fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public class CardinalMovementMod implements ClientModInitializer {
    private static boolean enabled = false; // Toggle flag
    private static KeyBinding toggleKey; // Key binding

    @Override
    public void onInitializeClient() {
        // Register keybinding
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.cardinalmovement.toggle", // Translation key
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_G, // Default to G
                "category.cardinalmovement"    // Controls category
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) return;

            // Check toggle key press
            while (toggleKey.wasPressed()) {
                enabled = !enabled;
                client.player.sendMessage(
                    net.minecraft.text.Text.literal("Cardinal Movement " + (enabled ? "enabled" : "disabled")),
                    true
                );
            }

            if (!enabled) return;

            // Unlock the cursor for mouse movement
            if (!client.mouse.isCursorLocked()) {
                client.mouse.unlockCursor();
            }

            boolean w = InputUtil.isKeyPressed(client.getWindow().getHandle(), GLFW.GLFW_KEY_W);
            boolean a = InputUtil.isKeyPressed(client.getWindow().getHandle(), GLFW.GLFW_KEY_A);
            boolean s = InputUtil.isKeyPressed(client.getWindow().getHandle(), GLFW.GLFW_KEY_S);
            boolean d = InputUtil.isKeyPressed(client.getWindow().getHandle(), GLFW.GLFW_KEY_D);

            Vec3d movement = Vec3d.ZERO;
            if (w) movement = movement.add(0, 0, -1); // North
            if (s) movement = movement.add(0, 0, 1);  // South
            if (a) movement = movement.add(-1, 0, 0); // West
            if (d) movement = movement.add(1, 0, 0);  // East

            if (!movement.equals(Vec3d.ZERO)) {
                movement = movement.normalize().multiply(0.1);
                client.player.setVelocity(movement);
                client.player.velocityDirty = true;

                double yaw = Math.toDegrees(Math.atan2(-movement.x, movement.z));
                client.player.setYaw((float) yaw);
                client.player.setHeadYaw((float) yaw);
                client.player.setBodyYaw((float) yaw);
            }

            // Use the cursor to interact with blocks
            if (client.crosshairTarget instanceof BlockHitResult hitResult) {
                if (InputUtil.isKeyPressed(client.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_LEFT)) {
                    client.interactionManager.attackBlock(hitResult.getBlockPos(), hitResult.getSide());
                } else if (InputUtil.isKeyPressed(client.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_RIGHT)) {
                    client.interactionManager.interactBlock(client.player, Hand.MAIN_HAND, hitResult);
                }
            }
        });
    }
}