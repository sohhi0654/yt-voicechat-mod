package net.azarasi.ytvc;

import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.VoicechatServerStartedEvent;

public class VoicechatPluginImpl implements VoicechatPlugin {
    private static VoicechatServerApi serverApi;

    @Override
    public String getPluginId() {
        return "ytvc_plugin";
    }

    @Override
    public void initialize(VoicechatApi api) {
        // 何もしない
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(VoicechatServerStartedEvent.class, this::onServerStarted);
    }

    private void onServerStarted(VoicechatServerStartedEvent event) {
        serverApi = event.getVoicechat();
        YoutubeVoiceChatMod.LOGGER.info("Voice Chat API に接続したよ！");
    }

    public static VoicechatServerApi getApi() {
        return serverApi;
    }
}
