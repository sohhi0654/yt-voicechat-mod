package net.azarasi.ytvc;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class YtPlayCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("ytplay")
            .then(CommandManager.argument("url", StringArgumentType.string())
            .executes(context -> {
                String url = StringArgumentType.getString(context, "url");
                ServerCommandSource source = context.getSource();
                ServerPlayerEntity player = source.getPlayer();
                
                if (player != null) {
                    source.sendMessage(Text.literal("YouTubeの再生を準備中... URL: " + url));
                    
                    // Java 25 の Virtual Thread で非同期処理！
                    Thread.ofVirtual().name("yt-streamer-" + player.getName().getString()).start(() -> {
                        AudioStreamer.playYoutubeAudio(player, url);
                    });
                } else {
                    source.sendMessage(Text.literal("プレイヤーからのみ実行できるよ！"));
                }
                return 1;
            })));
    }
}
