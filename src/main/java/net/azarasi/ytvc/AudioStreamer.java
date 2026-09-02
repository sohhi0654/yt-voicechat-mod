package net.azarasi.ytvc;

import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.audio.AudioEncoder;
import de.maxhenkel.voicechat.api.audio.LocationalAudioChannel;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

public class AudioStreamer {

    public static void playYoutubeAudio(ServerPlayerEntity player, String url) {
        try {
            VoicechatServerApi api = VoicechatPluginImpl.getApi();
            if (api == null) {
                player.sendMessage(Text.literal("Voice Chat APIの準備ができていないみたい。"));
                return;
            }

            // 【重要】26.2仕様のYaw/Pitch正規化 (音源の位置や回転の計算をバグらせないため)
            float normalizedYaw = MathHelper.wrapDegrees(player.getYaw());
            float normalizedPitch = MathHelper.clamp(player.getPitch(), -90.0F, 90.0F);

            // yt-dlp から音声のみを取得 (標準出力へ)
            ProcessBuilder ytDlp = new ProcessBuilder("yt-dlp", "-o", "-", "-f", "ba", url);
            // ffmpeg で ボイスチャットAPIが要求する 48kHz 16-bit Mono PCM に変換
            ProcessBuilder ffmpeg = new ProcessBuilder("ffmpeg", "-i", "pipe:0", "-f", "s16le", "-ar", "48000", "-ac", "1", "pipe:1");

            List<Process> processes = ProcessBuilder.startPipeline(List.of(ytDlp, ffmpeg));
            Process ffmpegProc = processes.get(processes.size() - 1);

            AudioEncoder encoder = api.createEncoder();
            
            // プレイヤーの位置に音源チャンネルを作成
            LocationalAudioChannel channel = api.createLocationalAudioChannel(
                    UUID.randomUUID(),
                    api.fromServerLevel(player.getServerWorld()),
                    api.createPosition(player.getX(), player.getY(), player.getZ())
            );

            // Opusエンコーダーのフレームサイズが 960 (20ms) なので、16bitモノラルの場合は 960 * 2 = 1920 バイト
            byte[] buffer = new byte[1920]; 
            int bytesRead;
            
            player.sendMessage(Text.literal("▶️ 再生開始！"));

            try (InputStream pcmStream = ffmpegProc.getInputStream()) {
                while ((bytesRead = pcmStream.read(buffer)) != -1) {
                    if (bytesRead < buffer.length) {
                        break; // 終端に達した場合
                    }

                    // 短整数(short)配列に変換してエンコーダーへ
                    short[] shortBuffer = new short[960];
                    for (int i = 0; i < 960; i++) {
                        shortBuffer[i] = (short) ((buffer[i * 2] & 0xFF) | (buffer[i * 2 + 1] << 8));
                    }

                    byte[] opusData = encoder.encode(shortBuffer);
                    channel.send(opusData);

                    // 20ms待機して再生速度をリアルタイムに合わせる
                    Thread.sleep(20);
                }
            }

            player.sendMessage(Text.literal("⏹ 再生終了！"));
            encoder.close();

        } catch (InterruptedException e) {
            YoutubeVoiceChatMod.LOGGER.warn("再生が中断されたよ: " + e.getMessage());
            player.sendMessage(Text.literal("再生を強制終了したよ。"));
        } catch (Exception e) {
            YoutubeVoiceChatMod.LOGGER.error("オーディオストリームエラー", e);
            player.sendMessage(Text.literal("エラーが発生したよ。サーバーのログを確認してね。"));
        }
    }
}
