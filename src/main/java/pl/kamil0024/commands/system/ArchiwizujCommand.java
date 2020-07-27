package pl.kamil0024.commands.system;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.PermissionOverride;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.managers.ChannelManager;
import pl.kamil0024.core.Main;
import pl.kamil0024.core.Ustawienia;
import pl.kamil0024.core.command.Command;
import pl.kamil0024.core.command.CommandContext;
import pl.kamil0024.core.command.enums.PermLevel;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ArchiwizujCommand extends Command {

    public ArchiwizujCommand() {
        name = "archiwizuj";
        aliases.add("archiwum");
        permLevel = PermLevel.DEVELOPER;
    }

    @Override
    public boolean execute(CommandContext context) {
        TextChannel txt = context.getParsed().getTextChannel(context.getArgs().get(0));
        Category cate = context.getGuild().getCategoryById(Ustawienia.instance.inne.kategoriaArchiwum);
        if (cate == null) throw new NullPointerException("Kategoria do archiwum jest nullem");

        if (txt == null) {
            context.sendTranslate("archiwizuj.badchannel").queue();
            return true;
        }

        if (txt.getParent() == cate) {
            context.sendTranslate("archiwizuj.already").queue();
            return false;
        }

        if (!context.getGuild().getSelfMember().hasPermission(txt, Permission.MANAGE_CHANNEL)) {
            context.sendTranslate("archiwizuj.perms", txt.getAsMention()).queue();
            return false;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yy");
        String name = sdf.format(new Date()) + "-" + txt.getName();

        ChannelManager manager = txt.getManager().clearOverridesAdded().clearOverridesRemoved().setParent(cate).setName(name);

        for (PermissionOverride permissionOverride : cate.getPermissionOverrides()) {
            if (permissionOverride.getPermissionHolder() != null) {
                manager = manager.putPermissionOverride(permissionOverride.getPermissionHolder(), permissionOverride.getAllowed(), permissionOverride.getDenied());
            }
        }
        manager.queue();

        context.sendTranslate("archiwizuj.succes").queue();
        return true;
    }

}
