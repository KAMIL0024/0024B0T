package pl.kamil0024.commands.zabawa;

import net.dv8tion.jda.api.entities.Member;
import pl.kamil0024.commands.kolkoikrzyzyk.KolkoIKrzyzykManager;
import pl.kamil0024.commands.kolkoikrzyzyk.entites.Zaproszenie;
import pl.kamil0024.core.command.Command;
import pl.kamil0024.core.command.CommandContext;
import pl.kamil0024.core.util.UsageException;

public class KolkoIKrzyzykCommand extends Command {

    private KolkoIKrzyzykManager kolkoIKrzyzykManager;

    public KolkoIKrzyzykCommand(KolkoIKrzyzykManager kolkoIKrzyzykManager) {
        name = "kolkoikrzyzyk";
        aliases.add("kolko");
        aliases.add("krzyzyk");
        cooldown = 15;

        this.kolkoIKrzyzykManager = kolkoIKrzyzykManager;
    }

    @Override
    public boolean execute(CommandContext context) {
        String arg = context.getArgs().get(0);
        if (arg == null) throw new UsageException();

        if (arg.toLowerCase().equals("akceptuj")) {
            Integer id = context.getParsed().getNumber(context.getArgs().get(1));
            if (id == null) {
                context.sendTranslate("kolkoikrzyzyk.emptyid").queue();
                return false;
            }

            Zaproszenie zapro = kolkoIKrzyzykManager.getZaproById(id);
            if (zapro == null || !zapro.getZapraszajaGo().equals(context.getUser().getId())) {
                context.sendTranslate("kolkoikrzyzyk.badid").queue();
                return false;
            }
            kolkoIKrzyzykManager.nowaGra(zapro);
            return true;
        } else {
            Member member = context.getParsed().getMember(context.getArgs().get(0));
            if (member == null) {
                context.sendTranslate("kolkoikrzyzyk.badmember").queue();
                return false;
            }
            if (member.getId().equals(context.getUser().getId())) {
                context.sendTranslate("kolkoikrzyzyk.nofriend").queue();
                return false;
            }

            if (member.getUser().isBot() || member.getUser().isFake()) {
                context.sendTranslate("kolkoikrzyzyk.bot").queue();
                return false;
            }

            KolkoIKrzyzykManager.ZaproszenieStatus zapro = kolkoIKrzyzykManager.zapros(context.getMember(), member, context.getChannel());
            if (!zapro.isError()) {
                Zaproszenie zapka = kolkoIKrzyzykManager.getZaproszenia().get(context.getUser().getId());
                if (zapka == null) throw new NullPointerException("zapka == null");
                context.send(String.format(zapro.getMsg(), zapka.getId())).queue();
            } else context.send(zapro.getMsg());

            return !zapro.isError();
        }
    }

}
