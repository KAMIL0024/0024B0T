/*
 *
 *    Copyright 2020 P2WB0T
 *
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package pl.kamil0024.rekrutacyjny;

import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.sharding.ShardManager;
import pl.kamil0024.core.command.Command;
import pl.kamil0024.core.command.CommandManager;
import pl.kamil0024.core.module.Modul;
import pl.kamil0024.rekrutacyjny.commands.OgloszenieCommand;
import pl.kamil0024.rekrutacyjny.listeners.SyncListener;

import java.util.ArrayList;

public class RekruModule implements Modul {

    private ArrayList<Command> cmd;

    private final CommandManager commandManager;
    private final ShardManager api;

    @Getter @Setter
    private boolean start = false;

    private SyncListener listener;

    public RekruModule(ShardManager api, CommandManager commandManager) {
        this.api = api;
        this.commandManager = commandManager;
    }

    @Override
    public boolean startUp() {
        cmd = new ArrayList<>();
        cmd.add(new OgloszenieCommand());
        cmd.forEach(commandManager::registerCommand);

        listener = new SyncListener();
        api.addEventListener(listener);
        setStart(true);
        return true;
    }

    @Override
    public boolean shutDown() {
        api.removeEventListener(listener);
        commandManager.unregisterCommands(cmd);
        setStart(false);
        return true;
    }

    @Override
    public String getName() {
        return "rekrutacyjny";
    }

}
