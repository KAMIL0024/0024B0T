package pl.kamil0024.logs.logger;

import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.audit.AuditLogOption;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.jetbrains.annotations.Nullable;
import pl.kamil0024.core.Ustawienia;
import pl.kamil0024.core.logger.Log;
import pl.kamil0024.core.util.UserUtil;

import javax.annotation.Nonnull;
import java.awt.*;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Logger extends ListenerAdapter {

    private final MessageManager manager;
    private final ShardManager jda;

    public Logger(MessageManager manager, ShardManager jda) {
        this.manager = manager;
        this.jda = jda;
    }

    @Override
    public void onGuildMessageDelete(@Nonnull GuildMessageDeleteEvent event) {
        FakeMessage msg = manager.get(event.getMessageId());
        if (msg == null) return;

        List<AuditLogEntry> audit = event.getGuild().retrieveAuditLogs().type(ActionType.MESSAGE_DELETE).complete();

        User deletedBy = null;
        for (AuditLogEntry auditlog : audit) {
            if (auditlog.getType() == ActionType.MESSAGE_DELETE
                    && auditlog.getTimeCreated().isAfter(OffsetDateTime.now().minusMinutes(1))
                    && event.getChannel().getId().equals(auditlog.getOption(AuditLogOption.CHANNEL))
                    && msg.getAuthor().equals(auditlog.getTargetId())) {
                deletedBy = auditlog.getUser();
                break;
            }
        }
        EmbedBuilder eb = getLogMessage(Action.DELETED, msg, deletedBy);
        String content = msg.getContent();
        if (content.length() > 1024) { content = content.substring(0, 1024); }
        eb.addField("Treść wiadomości:", content, false);
        sendLog(eb);
        manager.getMap().remove(event.getMessageId());
    }

    @Override
    public void onGuildMessageUpdate(@Nonnull GuildMessageUpdateEvent event) {
        FakeMessage msg = manager.get(event.getMessage().getId());

        if (msg == null) return;
        if (event.getMessage().getContentRaw().equals(msg.getContent())) { return; }

        EmbedBuilder eb = getLogMessage(Action.EDITED, msg, null);
        eb.addField("Stara treść wiadomości:", msg.getContent(), false);
        eb.addField("Nowa treść wiadomości:", event.getMessage().getContentRaw(), false);
        sendLog(eb);
        manager.edit(event.getMessage());
    }

    @SuppressWarnings("ConstantConditions")
    private EmbedBuilder getLogMessage(Action action, FakeMessage message, @Nullable User deletedBy) {
        EmbedBuilder eb = new EmbedBuilder();

        User user = jda.retrieveUserById(message.getAuthor()).complete();
        TextChannel kanal = jda.getTextChannelById(message.getChannel());

        eb.setFooter(action.getSlownie());
        eb.setTimestamp(message.getCreatedAt());
        eb.setColor(Color.RED);

        eb.addField("Autor wiadomości:", UserUtil.getLogName(user), false);
        if (action == Action.DELETED && deletedBy != null) {
            eb.addField("Usunięte przez", UserUtil.getLogName(deletedBy), false);
        }
        eb.addField("Kanał:", String.format("%s (%s) [%s]",
                kanal.getAsMention(), "#" + kanal.getName(), kanal.getId()), false);

        return eb;
    }

    public void sendLog(EmbedBuilder em) {
        TextChannel channel = jda.getTextChannelById(Ustawienia.instance.channel.wiadomosci);
        if (channel == null) {
            Log.error("Kanał do logów jest null!");
            return;
        }
        channel.sendMessage(em.build()).queue();
    }

    public enum Action {

        DELETED("Wiadomość usunięta"),
        EDITED("Wiadomość edytowana");

        @Getter private final String slownie;

        Action(String slownie) { this.slownie = slownie; }

    }

}
