package org.hotamachisubaru.hiraganablock.paper;

import java.util.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.*;
import org.bukkit.event.world.*;
import org.bukkit.inventory.*;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.hotamachisubaru.hiraganablock.core.*;

@SuppressWarnings("deprecation")
public final class HiraganaPlugin extends JavaPlugin implements Listener, TabExecutor {
    private BlockStore store;
    private LetterDisplays displays;
    private NamespacedKey kanaKey;
    private NamespacedKey colorKey;

    @Override public void onEnable() {
        kanaKey = new NamespacedKey(this, "kana");
        colorKey = new NamespacedKey(this, "color");
        store = new BlockStore(this);
        displays = new LetterDisplays(this);
        try { store.load(); }
        catch (Exception e) {
            getLogger().log(java.util.logging.Level.SEVERE, "blocks.ymlを読み込めないため停止します。元ファイルは保持しています。", e);
            store = null;
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        getServer().getPluginManager().registerEvents(this, this);
        Objects.requireNonNull(getCommand("hiragana")).setExecutor(this);
        Objects.requireNonNull(getCommand("hiragana")).setTabCompleter(this);
        for (var world : Bukkit.getWorlds()) for (var chunk : world.getLoadedChunks()) restore(chunk);
        getLogger().info("ひらがな " + KanaCatalog.ALL.size() + "文字 / 3色を読み込みました。");
    }

    @Override public void onDisable() {
        if (store == null) return;
        store.save();
        for (var world : Bukkit.getWorlds()) for (var chunk : world.getLoadedChunks()) displays.clear(chunk, null);
    }

    static Material material(BlockColor color) {
        return switch (color) { case BLACK -> Material.BLACK_CONCRETE; case GREEN -> Material.GREEN_CONCRETE; case YELLOW -> Material.YELLOW_CONCRETE; };
    }

    ItemStack item(LetterBlock letter, int count) {
        var item = new ItemStack(material(letter.color()), count);
        var meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.WHITE + letter.kana() + "（" + letter.color().label() + "）");
        meta.setLore(List.of(ChatColor.GRAY + "ひらがなブロック"));
        meta.getPersistentDataContainer().set(kanaKey, PersistentDataType.STRING, letter.kana());
        meta.getPersistentDataContainer().set(colorKey, PersistentDataType.STRING, letter.color().name());
        item.setItemMeta(meta);
        return item;
    }

    private LetterBlock fromItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        var data = item.getItemMeta().getPersistentDataContainer();
        var kana = data.get(kanaKey, PersistentDataType.STRING);
        var color = data.get(colorKey, PersistentDataType.STRING);
        if (kana == null || color == null) return null;
        try {
            var letter = new LetterBlock(kana, BlockColor.parse(color));
            return item.getType() == material(letter.color()) ? letter : null;
        } catch (IllegalArgumentException e) { return null; }
    }

    private boolean managed(Block block) { return store.blocks.containsKey(BlockPosition.of(block)); }

    private void put(Block block, LetterBlock letter) {
        var pos = BlockPosition.of(block);
        block.setType(material(letter.color()), false);
        store.blocks.put(pos, letter);
        displays.render(block.getWorld(), pos, letter);
    }

    private void restore(Chunk chunk) {
        displays.clear(chunk, null);
        boolean changed = false;
        var it = store.blocks.entrySet().iterator();
        while (it.hasNext()) {
            var entry = it.next(); var p = entry.getKey();
            if (!p.world().equals(chunk.getWorld().getUID()) || (p.x() >> 4) != chunk.getX() || (p.z() >> 4) != chunk.getZ()) continue;
            var block = chunk.getWorld().getBlockAt(p.x(), p.y(), p.z());
            if (block.getType() != material(entry.getValue().color())) { it.remove(); changed = true; }
            else displays.render(chunk.getWorld(), p, entry.getValue());
        }
        if (changed) store.save();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void placed(BlockPlaceEvent e) {
        var letter = fromItem(e.getItemInHand());
        if (letter == null) return;
        put(e.getBlockPlaced(), letter); store.save();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void breaking(BlockBreakEvent e) {
        if (managed(e.getBlock())) e.setDropItems(false);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void broken(BlockBreakEvent e) {
        var pos = BlockPosition.of(e.getBlock());
        var letter = store.blocks.remove(pos);
        if (letter == null) return;
        displays.clear(e.getBlock().getChunk(), pos);
        if (e.getPlayer().getGameMode() != GameMode.CREATIVE) {
            e.getBlock().getWorld().dropItemNaturally(e.getBlock().getLocation().add(.5, .5, .5), item(letter, 1));
        }
        store.save();
    }

    @EventHandler public void chunkLoad(ChunkLoadEvent e) { restore(e.getChunk()); }
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void chunkUnload(ChunkUnloadEvent e) { displays.clear(e.getChunk(), null); }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void piston(BlockPistonExtendEvent e) { if (e.getBlocks().stream().anyMatch(this::managed)) e.setCancelled(true); }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void piston(BlockPistonRetractEvent e) { if (e.getBlocks().stream().anyMatch(this::managed)) e.setCancelled(true); }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void explosion(EntityExplodeEvent e) { e.blockList().removeIf(this::managed); }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void explosion(BlockExplodeEvent e) { e.blockList().removeIf(this::managed); }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void change(EntityChangeBlockEvent e) { if (managed(e.getBlock())) e.setCancelled(true); }

    private void give(Player player, ItemStack item) {
        player.getInventory().addItem(item).values().forEach(left -> player.getWorld().dropItemNaturally(player.getLocation(), left));
    }

    @Override public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("hiraganablock.admin")) { sender.sendMessage("権限がありません。"); return true; }
        try {
            if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
                sender.sendMessage("/hb menu | give <文字> [black|green|yellow] [個数] | answer <答え> | check <x> <y> <z> <east|west|south|north> | judge <答え> <回答>");
                return true;
            }
            if (args[0].equalsIgnoreCase("judge") && args.length == 3) {
                sender.sendMessage(format(WordJudge.evaluate(args[1], args[2]))); return true;
            }
            if (!(sender instanceof Player player)) throw new IllegalArgumentException("このコマンドはゲーム内で実行してください。");
            switch (args[0].toLowerCase(Locale.ROOT)) {
                case "menu" -> openMenu(player, 0, BlockColor.BLACK);
                case "give" -> {
                    if (args.length < 2 || args.length > 4) throw new IllegalArgumentException("/hb give <文字> [black|green|yellow] [個数]");
                    int count = args.length == 4 ? Integer.parseInt(args[3]) : 1;
                    if (count < 1 || count > 64) throw new IllegalArgumentException("個数は1〜64です。");
                    var color = args.length >= 3 ? BlockColor.parse(args[2]) : BlockColor.BLACK;
                    give(player, item(new LetterBlock(args[1], color), count));
                }
                case "answer" -> {
                    if (args.length != 2) throw new IllegalArgumentException("/hb answer <答え>");
                    var word = KanaCatalog.word(args[1]);
                    store.answers.put(player.getUniqueId(), String.join("", word)); store.save();
                    player.sendMessage("答えを設定しました（" + word.size() + "文字）。");
                }
                case "check" -> check(player, args);
                default -> throw new IllegalArgumentException("/hb help で使い方を確認してください。");
            }
        } catch (IllegalArgumentException e) { sender.sendMessage(ChatColor.RED + e.getMessage()); }
        return true;
    }

    private void check(Player player, String[] args) {
        if (args.length != 5) throw new IllegalArgumentException("/hb check <x> <y> <z> <east|west|south|north>");
        var answer = store.answers.get(player.getUniqueId());
        if (answer == null) throw new IllegalArgumentException("先に /hb answer <答え> を実行してください。");
        int x = Integer.parseInt(args[1]), y = Integer.parseInt(args[2]), z = Integer.parseInt(args[3]);
        int dx = 0, dz = 0;
        switch (args[4].toLowerCase(Locale.ROOT)) {
            case "east" -> dx = 1; case "west" -> dx = -1; case "south" -> dz = 1; case "north" -> dz = -1;
            default -> throw new IllegalArgumentException("方向は east / west / south / north です。");
        }
        var world = player.getWorld();
        if (y < world.getMinHeight() || y >= world.getMaxHeight() || Math.abs((long)x) > 29999900 || Math.abs((long)z) > 29999900)
            throw new IllegalArgumentException("座標が範囲外です。");
        var blocks = new ArrayList<Block>(); var guess = new StringBuilder();
        for (int i = 0; i < KanaCatalog.word(answer).size(); i++) {
            int bx = x + dx*i, bz = z + dz*i;
            if (!world.isChunkLoaded(bx >> 4, bz >> 4)) throw new IllegalArgumentException("未読み込みのチャンクがあります。");
            var block = world.getBlockAt(bx, y, bz);
            var letter = store.blocks.get(BlockPosition.of(block));
            if (letter == null || block.getType() != material(letter.color())) throw new IllegalArgumentException((i+1) + "文字目にひらがなブロックがありません。");
            blocks.add(block); guess.append(letter.kana());
        }
        // Validate every cell before changing any block.
        var result = WordJudge.evaluate(answer, guess.toString());
        for (int i = 0; i < result.size(); i++) put(blocks.get(i), result.get(i));
        store.save(); player.sendMessage(format(result));
    }

    private String format(List<LetterBlock> letters) {
        var text = new StringBuilder();
        for (var letter : letters) text.append(switch(letter.color()) { case GREEN -> ChatColor.GREEN; case YELLOW -> ChatColor.YELLOW; case BLACK -> ChatColor.GRAY; }).append(letter.kana());
        return text.toString();
    }

    private final class Menu implements InventoryHolder {
        final int page; final BlockColor color; final Inventory inventory;
        Menu(int page, BlockColor color) {
            this.page = page; this.color = color;
            inventory = Bukkit.createInventory(this, 54, "ひらがな / " + color.label() + " / " + (page+1));
        }
        @Override public Inventory getInventory() { return inventory; }
    }

    private ItemStack button(Material material, String title) {
        var item = new ItemStack(material); var meta = item.getItemMeta(); meta.setDisplayName(title); item.setItemMeta(meta); return item;
    }

    private void openMenu(Player player, int page, BlockColor color) {
        var menu = new Menu(page, color);
        for (int slot = 0; slot < 45 && page*45 + slot < KanaCatalog.ALL.size(); slot++)
            menu.inventory.setItem(slot, item(new LetterBlock(KanaCatalog.ALL.get(page*45 + slot), color), 1));
        if (page > 0) menu.inventory.setItem(45, button(Material.ARROW, "前のページ"));
        if ((page+1)*45 < KanaCatalog.ALL.size()) menu.inventory.setItem(53, button(Material.ARROW, "次のページ"));
        for (var c : BlockColor.values()) menu.inventory.setItem(48+c.ordinal(), button(material(c), c.label()));
        player.openInventory(menu.inventory);
    }

    @EventHandler public void click(InventoryClickEvent e) {
        if (!(e.getView().getTopInventory().getHolder() instanceof Menu menu)) return;
        e.setCancelled(true);
        if (!(e.getWhoClicked() instanceof Player player) || !player.hasPermission("hiraganablock.admin")) return;
        int slot = e.getRawSlot();
        if (slot >= 0 && slot < 45 && menu.page*45+slot < KanaCatalog.ALL.size()) {
            give(player, item(new LetterBlock(KanaCatalog.ALL.get(menu.page*45+slot), menu.color), e.isShiftClick() ? 64 : 1));
        } else {
            int page = menu.page; var color = menu.color;
            if (slot == 45 && page > 0) page--;
            else if (slot == 53 && (page+1)*45 < KanaCatalog.ALL.size()) page++;
            else if (slot >= 48 && slot <= 50) color = BlockColor.values()[slot-48];
            else return;
            int next = page; var nextColor = color;
            Bukkit.getScheduler().runTask(this, () -> { if (player.isOnline()) openMenu(player, next, nextColor); });
        }
    }

    @EventHandler public void drag(InventoryDragEvent e) {
        if (e.getView().getTopInventory().getHolder() instanceof Menu) e.setCancelled(true);
    }

    @Override public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("hiraganablock.admin") || args.length == 0) return List.of();
        List<String> choices = List.of();
        if (args.length == 1) choices = List.of("menu", "give", "answer", "check", "judge", "help");
        else if (args[0].equalsIgnoreCase("give") && args.length == 2) choices = KanaCatalog.ALL;
        else if (args[0].equalsIgnoreCase("give") && args.length == 3) choices = List.of("black", "green", "yellow");
        else if (args[0].equalsIgnoreCase("check") && args.length == 5) choices = List.of("east", "west", "south", "north");
        String prefix = args[args.length-1].toLowerCase(Locale.ROOT);
        return choices.stream().filter(s -> s.startsWith(prefix)).toList();
    }
}
