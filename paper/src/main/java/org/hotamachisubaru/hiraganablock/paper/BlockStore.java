package org.hotamachisubaru.hiraganablock.paper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.configuration.file.YamlConfiguration;
import org.hotamachisubaru.hiraganablock.core.*;

final class BlockStore {
    final Map<BlockPosition, LetterBlock> blocks = new HashMap<>();
    final Map<UUID, String> answers = new HashMap<>();
    private final HiraganaPlugin plugin;
    BlockStore(HiraganaPlugin plugin) { this.plugin = plugin; }

    void load() throws Exception {
        var file = plugin.getDataFolder().toPath().resolve("blocks.yml");
        if (!Files.exists(file)) return;
        var yaml = new YamlConfiguration();
        yaml.load(file.toFile()); // Fail startup on corrupt data; never silently overwrite it.
        var section = yaml.getConfigurationSection("blocks");
        if (section != null) for (var key : section.getKeys(false)) {
            blocks.put(BlockPosition.parse(key), new LetterBlock(section.getString(key + ".kana"), BlockColor.parse(section.getString(key + ".color", "black"))));
        }
        section = yaml.getConfigurationSection("answers");
        if (section != null) for (var key : section.getKeys(false)) {
            String answer = section.getString(key);
            KanaCatalog.word(answer);
            answers.put(UUID.fromString(key), KanaCatalog.normalize(answer));
        }
    }

    void save() {
        try {
            var folder = plugin.getDataFolder().toPath();
            Files.createDirectories(folder);
            var yaml = new YamlConfiguration();
            blocks.forEach((pos, letter) -> {
                yaml.set("blocks." + pos.key() + ".kana", letter.kana());
                yaml.set("blocks." + pos.key() + ".color", letter.color().name());
            });
            answers.forEach((id, word) -> yaml.set("answers." + id, word));
            var tmp = folder.resolve("blocks.yml.tmp");
            var target = folder.resolve("blocks.yml");
            yaml.save(tmp.toFile());
            try { Files.move(tmp, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING); }
            catch (java.nio.file.AtomicMoveNotSupportedException e) { Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING); }
        } catch (IOException e) { plugin.getLogger().log(java.util.logging.Level.SEVERE, "ブロックの保存に失敗しました", e); }
    }
}
