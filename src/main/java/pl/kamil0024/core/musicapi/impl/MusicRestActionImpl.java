package pl.kamil0024.core.musicapi.impl;

import lombok.SneakyThrows;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.jetbrains.annotations.Nullable;
import pl.kamil0024.core.musicapi.MusicResponse;
import pl.kamil0024.core.musicapi.MusicRestAction;
import pl.kamil0024.core.util.JSONResponse;
import pl.kamil0024.core.util.NetworkUtil;

import java.io.IOException;

@SuppressWarnings("ConstantConditions")
public class MusicRestActionImpl implements MusicRestAction {

    private final ShardManager api;
    private final Integer port;

    public MusicRestActionImpl(ShardManager api, Integer port) {
        this.api = api;
        this.port = port;
    }

    @Override
    @Nullable
    public MusicResponse testConnection() {
        try {
            return new MusicResponse(NetworkUtil.getJson(formatUrl("test")));
        } catch (Exception e) {
            return null;
        }

    }

    @Override
    public MusicResponse connect(String channelId) throws Exception {
        return new MusicResponse(NetworkUtil.getJson(formatUrl("connect/" + channelId)));
    }

    @Override
    public MusicResponse disconnect() throws Exception {
        return new MusicResponse(NetworkUtil.getJson(formatUrl("disconnect")));
    }

    @Override
    @Nullable
    public VoiceChannel getVoiceChannel() {
        try {
            JSONResponse mr = NetworkUtil.getJson(formatUrl("channel"));
            String id = mr.getString("data");
            return api.getVoiceChannelById(id);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public MusicResponse shutdown() throws IOException {
        return new MusicResponse(NetworkUtil.getJson(formatUrl("shutdown")));
    }

    @Override
    public MusicResponse play(String link) throws IOException {
        return new MusicResponse(NetworkUtil.getJson(formatUrl("playlink/" + link)));
    }

    @Override
    public MusicResponse skip() throws IOException {
        return new MusicResponse(NetworkUtil.getJson(formatUrl("skip")));
    }

    @Override
    public MusicResponse volume(Integer procent) throws IOException {
        return new MusicResponse(NetworkUtil.getJson(formatUrl("volume/" + procent)));
    }

    @SneakyThrows
    @Override
    public String clientid() {
        try {
            JSONResponse mr = NetworkUtil.getJson(formatUrl("channel"));
            return mr.getString("data");
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public MusicResponse getQueue() throws IOException {
        return new MusicResponse(NetworkUtil.getJson(formatUrl("queue")));
    }

    @Override
    public MusicResponse getPlayingTrack() throws IOException {
        return new MusicResponse(NetworkUtil.getJson(formatUrl("playingtrack")));
    }

    private String formatUrl(String path) {
        return String.format("http://0.0.0.0:%s/api/musicbot/%s", port, path);
    }

}