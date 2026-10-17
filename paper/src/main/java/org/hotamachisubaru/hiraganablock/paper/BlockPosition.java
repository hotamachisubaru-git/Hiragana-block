package org.hotamachisubaru.hiraganablock.paper;

import java.util.UUID;
import org.bukkit.block.Block;

record BlockPosition(UUID world, int x, int y, int z) {
    static BlockPosition of(Block block) { return new BlockPosition(block.getWorld().getUID(), block.getX(), block.getY(), block.getZ()); }
    String key() { return world + ":" + x + ":" + y + ":" + z; }
    static BlockPosition parse(String text) {
        var p = text.split(":");
        if (p.length != 4) throw new IllegalArgumentException("Invalid position");
        return new BlockPosition(UUID.fromString(p[0]), Integer.parseInt(p[1]), Integer.parseInt(p[2]), Integer.parseInt(p[3]));
    }
}
