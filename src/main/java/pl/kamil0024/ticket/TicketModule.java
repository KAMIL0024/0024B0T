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

package pl.kamil0024.ticket;


import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.sharding.ShardManager;
import pl.kamil0024.core.database.TXTTicketDao;
import pl.kamil0024.core.database.TicketDao;
import pl.kamil0024.core.module.Modul;
import pl.kamil0024.core.redis.RedisManager;
import pl.kamil0024.core.util.EventWaiter;
import pl.kamil0024.ticket.components.ComponentListener;
import pl.kamil0024.ticket.config.TicketRedisManager;
import pl.kamil0024.ticket.listener.VoiceChatListener;

import java.util.ArrayList;
import java.util.List;

public class TicketModule implements Modul {

    private final TicketRedisManager ticketRedisManager;

    private final TicketDao ticketDao;
    private final ShardManager api;
    private final RedisManager redisManager;
    private final EventWaiter eventWaiter;
    private final TXTTicketDao txtTicketDao;

    @Getter
    private final String name = "ticket";

    @Getter
    @Setter
    private boolean start = false;

    private final List<ListenerAdapter> listeners = new ArrayList<>();

    public TicketModule(ShardManager api, TicketDao ticketDao, RedisManager redisManager, EventWaiter eventWaiter, TXTTicketDao txtTicketDao) {
        this.api = api;
        this.ticketDao = ticketDao;
        this.redisManager = redisManager;
        this.ticketRedisManager = new TicketRedisManager(redisManager);
        this.eventWaiter = eventWaiter;
        this.txtTicketDao = txtTicketDao;
    }

    @Override
    public boolean startUp() {
        listeners.add(new VoiceChatListener(ticketDao, ticketRedisManager, eventWaiter, redisManager));
        listeners.add(new ComponentListener(txtTicketDao, redisManager));
        api.addEventListener(listeners);
        return true;
    }

    @Override
    public boolean shutDown() {
        api.removeEventListener(listeners);
        listeners.clear();
        return true;
    }

}
