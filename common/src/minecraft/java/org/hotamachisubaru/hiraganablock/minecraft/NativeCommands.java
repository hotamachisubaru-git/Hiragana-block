package org.hotamachisubaru.hiraganablock.minecraft;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.LevelResource;
import org.hotamachisubaru.hiraganablock.core.*;

public final class NativeCommands {
    private NativeCommands() { }
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        var root = Commands.literal("hiragana")
                .requires(source -> source.hasPermission(2))
                .executes(c -> { c.getSource().sendSuccess(() -> Component.literal("/hb give <文字> [色] [個数] | answer <答え> | check <x> <y> <z> <方向> | judge <答え> <回答>"), false); return 1; });
        root.then(Commands.literal("give").then(Commands.argument("kana", StringArgumentType.word())
                .suggests((c,b) -> SharedSuggestionProvider.suggest(KanaCatalog.ALL,b))
                .executes(c -> give(c, BlockColor.BLACK, 1))
                .then(Commands.argument("color", StringArgumentType.word()).suggests((c,b) -> SharedSuggestionProvider.suggest(List.of("black","green","yellow"),b))
                        .executes(c -> give(c, color(c), 1))
                        .then(Commands.argument("count", IntegerArgumentType.integer(1,64)).executes(c -> give(c, color(c), IntegerArgumentType.getInteger(c,"count")))))));
        root.then(Commands.literal("answer").then(Commands.argument("answer", StringArgumentType.word()).executes(NativeCommands::answer)));
        root.then(Commands.literal("check").then(Commands.argument("pos", BlockPosArgument.blockPos())
                .then(Commands.argument("direction", StringArgumentType.word()).suggests((c,b) -> SharedSuggestionProvider.suggest(List.of("east","west","south","north"),b))
                        .executes(NativeCommands::check))));
        root.then(Commands.literal("judge").then(Commands.argument("answer", StringArgumentType.word())
                .then(Commands.argument("guess", StringArgumentType.word()).executes(c -> {
                    try {
                        var result = WordJudge.evaluate(StringArgumentType.getString(c,"answer"), StringArgumentType.getString(c,"guess"));
                        c.getSource().sendSuccess(() -> Component.literal(format(result)), false); return 1;
                    } catch (IllegalArgumentException e) { throw error(e.getMessage()); }
                }))));
        var node = dispatcher.register(root);
        dispatcher.register(Commands.literal("hb").requires(source -> source.hasPermission(2)).redirect(node));
    }

    private static CommandSyntaxException error(String message) { return new SimpleCommandExceptionType(Component.literal(message)).create(); }
    private static BlockColor color(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        try { return BlockColor.parse(StringArgumentType.getString(c,"color")); }
        catch (IllegalArgumentException e) { throw error(e.getMessage()); }
    }
    private static int give(CommandContext<CommandSourceStack> c, BlockColor color, int count) throws CommandSyntaxException {
        try {
            var player = c.getSource().getPlayerOrException();
            var letter = new LetterBlock(StringArgumentType.getString(c,"kana"),color);
            var stack = new ItemStack(NativeBlocks.get(letter).asItem(),count);
            if (player.getInventory().getFreeSlot() < 0) throw error("インベントリに空きを作ってください。");
            player.getInventory().add(stack);
            return 1;
        } catch (IllegalArgumentException e) { throw error(e.getMessage()); }
    }

    private static Path answerFile(CommandSourceStack source) {
        return source.getServer().getWorldPath(LevelResource.ROOT).resolve("hiraganablock-answers.properties");
    }
    private static Properties loadAnswers(CommandSourceStack source) throws IOException {
        var result = new Properties(); var path = answerFile(source);
        if (Files.exists(path)) try (var reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) { result.load(reader); }
        return result;
    }
    private static int answer(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        try {
            var player = c.getSource().getPlayerOrException();
            var word = String.join("", KanaCatalog.word(StringArgumentType.getString(c,"answer")));
            var data = loadAnswers(c.getSource()); data.setProperty(player.getUUID().toString(),word);
            var path = answerFile(c.getSource()); var tmp = path.resolveSibling(path.getFileName()+".tmp");
            try (var writer = Files.newBufferedWriter(tmp,StandardCharsets.UTF_8)) { data.store(writer,"Hiragana Block answers"); }
            try { Files.move(tmp,path,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING); }
            catch (AtomicMoveNotSupportedException e) { Files.move(tmp,path,StandardCopyOption.REPLACE_EXISTING); }
            c.getSource().sendSuccess(() -> Component.literal("答えを設定しました（"+word.length()+"文字）。"),false);
            return 1;
        } catch (IllegalArgumentException | IOException e) { throw error("答えを保存できません: "+e.getMessage()); }
    }
    private static int check(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        try {
            var source = c.getSource(); var player = source.getPlayerOrException();
            var answer = loadAnswers(source).getProperty(player.getUUID().toString());
            if (answer == null) throw error("先に /hb answer <答え> を実行してください。");
            var start = BlockPosArgument.getLoadedBlockPos(c,"pos");
            var direction = switch (StringArgumentType.getString(c,"direction")) {
                case "east" -> Direction.EAST; case "west" -> Direction.WEST; case "north" -> Direction.NORTH; case "south" -> Direction.SOUTH;
                default -> throw error("方向は east / west / south / north です。");
            };
            var level = source.getLevel(); var positions = new ArrayList<BlockPos>(); var guess = new StringBuilder();
            for (int i=0;i<KanaCatalog.word(answer).size();i++) {
                var pos = start.relative(direction,i);
                if (!level.hasChunkAt(pos)) throw error("未読み込みのチャンクがあります。");
                var letter = NativeBlocks.identify(level.getBlockState(pos).getBlock());
                if (letter == null) throw error((i+1)+"文字目にひらがなブロックがありません。");
                positions.add(pos); guess.append(letter.kana());
            }
            var result = WordJudge.evaluate(answer,guess.toString());
            for (int i=0;i<result.size();i++) level.setBlock(positions.get(i),NativeBlocks.get(result.get(i)).defaultBlockState(),3);
            source.sendSuccess(() -> Component.literal(format(result)),false); return 1;
        } catch (IllegalArgumentException | IOException e) { throw error(e.getMessage()); }
    }
    private static String format(List<LetterBlock> result) {
        var text = new StringBuilder();
        for (var letter : result) text.append(letter.kana()).append('[').append(letter.color().label()).append("] ");
        return text.toString();
    }
}
