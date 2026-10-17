package org.hotamachisubaru.hiraganablock.paper;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.persistence.PersistentDataType;
import org.joml.Matrix4f;
import org.hotamachisubaru.hiraganablock.core.*;

final class LetterDisplays {
    private final NamespacedKey marker;
    LetterDisplays(HiraganaPlugin plugin) { marker = new NamespacedKey(plugin, "position"); }

    void clear(Chunk chunk, BlockPosition only) {
        for (var entity : chunk.getEntities()) {
            var value = entity.getPersistentDataContainer().get(marker, PersistentDataType.STRING);
            if (value != null && (only == null || value.equals(only.key()))) entity.remove();
        }
    }

    void render(World world, BlockPosition p, LetterBlock letter) {
        if (!world.isChunkLoaded(p.x() >> 4, p.z() >> 4)) return;
        clear(world.getChunkAt(p.x() >> 4, p.z() >> 4), p);
        // Each face is a separate, fixed plane. Actual concrete supplies the collision box.
        face(world, p, letter, .5, .5, 1.002, 0, 0);
        face(world, p, letter, .5, .5, -.002, 180, 0);
        face(world, p, letter, -.002, .5, .5, 90, 0);
        face(world, p, letter, 1.002, .5, .5, -90, 0);
        face(world, p, letter, .5, 1.002, .5, 0, -90);
        face(world, p, letter, .5, -.002, .5, 0, 90);
    }

    @SuppressWarnings("deprecation")
    private void face(World world, BlockPosition p, LetterBlock letter, double x, double y, double z, float yaw, float pitch) {
        world.spawn(new Location(world, p.x() + .5, p.y() + .5, p.z() + .5), TextDisplay.class, display -> {
            display.setText(ChatColor.WHITE + letter.kana());
            display.setBillboard(Display.Billboard.FIXED);
            display.setAlignment(TextDisplay.TextAlignment.CENTER);
            display.setBackgroundColor(Color.fromARGB(0));
            display.setDefaultBackground(false);
            display.setShadowed(false);
            display.setSeeThrough(false);
            display.setLineWidth(100);
            display.setBrightness(new Display.Brightness(15, 15));
            // Keep entity anchors in the owning chunk even on chunk borders.
            display.setTransformationMatrix(new Matrix4f().translate((float)x - .5f, (float)y - .5f, (float)z - .5f)
                    .rotateY((float)Math.toRadians(-yaw)).rotateX((float)Math.toRadians(pitch))
                    .translate(0, -.36f, 0).scale(3.0f));
            display.setPersistent(false);
            display.getPersistentDataContainer().set(marker, PersistentDataType.STRING, p.key());
        });
    }
}
