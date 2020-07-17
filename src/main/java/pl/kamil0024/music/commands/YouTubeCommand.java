package pl.kamil0024.music.commands;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import pl.kamil0024.core.command.Command;
import pl.kamil0024.core.command.CommandContext;
import pl.kamil0024.core.command.enums.PermLevel;
import pl.kamil0024.core.util.UsageException;
import pl.kamil0024.music.MusicModule;

public class YouTubeCommand extends Command {

    private MusicModule musicModule;

    public YouTubeCommand(MusicModule musicModule) {
        name = "youtube";
        aliases.add("yt");
        permLevel = PermLevel.HELPER;
        this.musicModule = musicModule;
    }

    @Override
    public boolean execute(CommandContext context) {
        String tytul = context.getArgsToString(0);
        if (context.getArgs().get(0) == null) throw new UsageException();

        AudioTrack[] traki = musicModule.search(tytul);
        context.send(traki.length + "").queue();

        return true;
    }



}