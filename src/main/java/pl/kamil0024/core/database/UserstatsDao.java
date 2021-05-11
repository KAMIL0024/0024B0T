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

package pl.kamil0024.core.database;

import gg.amy.pgorm.PgMapper;
import lombok.Getter;
import org.joda.time.DateTime;
import pl.kamil0024.core.database.config.Dao;
import pl.kamil0024.core.database.config.UserstatsConfig;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class UserstatsDao implements Dao<UserstatsConfig> {

    @Getter
    public final PgMapper<UserstatsConfig> mapper;

    public UserstatsDao(DatabaseManager databaseManager) {
        if (databaseManager == null) throw new IllegalStateException("databaseManager == null");
        mapper = databaseManager.getPgStore().mapSync(UserstatsConfig.class);
    }

    @Override
    public UserstatsConfig get(String date) {
        return mapper.load(date).orElse(null);
    }

    public List<UserstatsConfig> getFromMember(String id, int minusDays) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date(new DateTime().minusDays(minusDays).getMillis()));
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return mapper.loadRaw("SELECT * FROM %s WHERE data->'memberslist' ??| ARRAY[?] AND id::numeric >= ?::numeric", id, String.valueOf(cal.getTimeInMillis()));
    }

}
