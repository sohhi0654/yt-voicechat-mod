package net.azarasi.ytvc;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class YoutubeVoiceChatMod implements DedicatedServerModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("ytvc");

    @Override
    public void onInitializeServer() {
        LOGGER.info("YouTube Voice Chat Mod が読み込まれたよ！");
        
        // コマンドの登録
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            YtPlayCommand.register(dispatcher);
        });
    }
}
