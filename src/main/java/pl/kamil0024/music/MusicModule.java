package pl.kamil0024.music;

import com.google.inject.Inject;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.vimeo.VimeoAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeSearchProvider;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.sharding.ShardManager;
import pl.kamil0024.core.command.Command;
import pl.kamil0024.core.command.CommandManager;
import pl.kamil0024.core.logger.Log;
import pl.kamil0024.core.module.Modul;
import pl.kamil0024.core.util.EventWaiter;
import pl.kamil0024.music.commands.*;
import pl.kamil0024.musicmanager.entity.GuildMusicManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class MusicModule implements Modul {

    private ArrayList<Command> cmd;

    @Inject CommandManager commandManager;
    @Inject ShardManager api;
    @Inject EventWaiter eventWaiter;

    private boolean start = false;

    private final AudioPlayerManager playerManager;
    private final Map<Long, GuildMusicManager> musicManagers;

    private static YoutubeSearchProvider youtubeSearchProvider = new YoutubeSearchProvider();

    public static YoutubeAudioSourceManager youtubeSourceManager;

    public MusicModule(CommandManager commandManager, ShardManager api, EventWaiter eventWaiter) {
        this.commandManager = commandManager;
        this.api = api;
        this.eventWaiter = eventWaiter;

        this.playerManager = new DefaultAudioPlayerManager();
        this.musicManagers = new HashMap<>();

        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioSourceManagers.registerLocalSource(playerManager);

        playerManager.registerSourceManager(new YoutubeAudioSourceManager(true));
        playerManager.registerSourceManager(SoundCloudAudioSourceManager.createDefault());
        playerManager.registerSourceManager(new VimeoAudioSourceManager());
        playerManager.registerSourceManager(new TwitchStreamAudioSourceManager());
    }

    @Override
    public boolean startUp() {
        cmd = new ArrayList<>();

        cmd.add(new PlayCommand(this));
        cmd.add(new QueueCommand(this, eventWaiter));
        cmd.add(new VolumeCommand(this));
        cmd.add(new ResumeCommand(this));
        cmd.add(new SkipCommand(this));
        cmd.add(new YouTubeCommand(this));

        cmd.forEach(commandManager::registerCommand);
        setStart(true);
        return true;
    }

    @Override
    public boolean shutDown() {
        commandManager.unregisterCommands(cmd);
        setStart(false);
        return true;
    }

    @Override
    public String getName() {
        return "music";
    }

    @Override
    public boolean isStart() {
        return start;
    }

    @Override
    public void setStart(boolean bol) {
        this.start = bol;
    }


    public synchronized GuildMusicManager getGuildAudioPlayer(Guild guild) {
        long guildId = guild.getIdLong();
        GuildMusicManager musicManager = musicManagers.get(guildId);

        if (musicManager == null || musicManager.getPlayer() == null) {
            musicManager = new GuildMusicManager(playerManager);
            musicManagers.put(guildId, musicManager);
        }

        guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());

        return musicManager;
    }

    public boolean loadAndPlay(final TextChannel channel, final String trackUrl, VoiceChannel vc) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
        AtomicBoolean error = new AtomicBoolean(false);

        playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                channel.sendMessage("Dodaje do kolejki " + track.getInfo().title).queue();
                play(channel.getGuild(), musicManager, track, vc);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                for (AudioTrack track : playlist.getTracks()) {
                    play(channel.getGuild(), musicManager, track, vc);
                }

            }

            @Override
            public void noMatches() {
                channel.sendMessage("Nic nie znaleziono pod " + trackUrl).queue();
                error.set(true);
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                channel.sendMessage("Nie można odtworzyć piosenki: " + exception.getMessage()).queue();
                error.set(true);
            }
        });
        return !error.get();
    }

    public void play(Guild guild, GuildMusicManager musicManager, AudioTrack track, VoiceChannel vc) {
        connectToFirstVoiceChannel(guild.getAudioManager(), vc);

        musicManager.scheduler.queue(track);

        if (musicManager.scheduler.getAktualnaPiosenka() == null) {
            musicManager.scheduler.setAktualnaPiosenka(track);
        }
    }

    public static void connectToFirstVoiceChannel(AudioManager audioManager, VoiceChannel vc) {
        audioManager.openAudioConnection(vc);
    }

    public AudioTrack[] search(String tytul) {
        List<AudioTrack> results = new ArrayList<>();
        AudioItem playlist = null;

        playlist = youtubeSearchProvider.loadSearchResult(tytul, info -> new YoutubeAudioTrack(info, youtubeSourceManager));

        if (playlist instanceof AudioPlaylist) {
            results.addAll(((AudioPlaylist) playlist).getTracks());
        }

        return results.isEmpty() ? null :
                results.toArray(new AudioTrack[results.size()]);
    }

}