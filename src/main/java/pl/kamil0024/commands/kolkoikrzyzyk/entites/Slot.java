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

package pl.kamil0024.commands.kolkoikrzyzyk.entites;

import lombok.Getter;
import net.dv8tion.jda.api.entities.Member;
import pl.kamil0024.commands.kolkoikrzyzyk.Gra;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Slot {

    @Getter public HashMap<Integer, String> sloty;

    public Slot() {
        this.sloty = new HashMap<>();

        for (int i = 1; i < 10; i++) {
            sloty.put(i, Gra.PUSTE);
        }

    }

    public ReturnType check(String s, Gra gra, Member osoba) {
        String[] slot = s.split("");

        int jeden;
        String tak = slot[1].toLowerCase();

        try {
            jeden = Integer.parseInt(slot[0]);
        } catch (NumberFormatException e) {
            return ReturnType.BAD_FORMAT;
        }

        if (jeden < 1 || jeden > 3) return ReturnType.BAD_FORMAT;
        if (!tak.equals("a") && !tak.equals("b") && !tak.equals("c")) return ReturnType.BAD_FORMAT;

        String kekw = sloty.get(format(slot));
        if (!kekw.equals(Gra.PUSTE)) return ReturnType.BAD_FORMAT;

        sloty.put(format(slot), gra.getEmote(osoba));

        if (checkWin(gra.getEmote(osoba))) return ReturnType.WIN;

        boolean pelnaMapa = true;
        for (Map.Entry<Integer, String> entry : getSloty().entrySet()) {
            if (entry.getValue().equals(Gra.PUSTE)) {
                pelnaMapa = false;
                break;
            }
        }
        if (pelnaMapa) return ReturnType.FULL_MAP;

        return ReturnType.SUCCES;
    }

    private int format(String[] s) {
        int slot = Integer.parseInt(s[0]);

        if (s[1].equals("a")) return jebacMatme(slot, 1, 4, 7);
        if (s[1].equals("b")) return jebacMatme(slot, 2, 5, 8);
        if (s[1].equals("c")) return jebacMatme(slot, 3, 6, 9);

        throw new UnsupportedOperationException("co jest");
    }

    private int jebacMatme(int slot, int moze1, int moze2, int moze3) {
        if (slot == 1) return moze1;
        if (slot == 2) return moze2;
        if (slot == 3) return moze3;
        throw new UnsupportedOperationException("co jest");
    }

    public enum ReturnType {
        BAD_FORMAT, FULL_MAP, WIN, SUCCES
    }

    @SuppressWarnings("RedundantIfStatement")
    private boolean checkWin(String emote) {

        if (tak(emote, 1, 2, 3)) return true;
        if (tak(emote, 4, 5, 6)) return true;
        if (tak(emote, 7, 8, 9)) return true;

        if (tak(emote, 1, 4, 7)) return true;
        if (tak(emote, 2, 5, 8)) return true;
        if (tak(emote, 3, 6, 9)) return true;

        if (tak(emote, 7, 5, 3)) return true;
        if (tak(emote, 1, 5, 9)) return true;

        return false;
    }

    private boolean tak(String equals, Integer... id) {
        List<Boolean> tak = new ArrayList<>();
        for (Integer s : id) {
            try {
                if (getSloty().get(s).equals(equals)) tak.add(true);
            } catch (Exception ignored) {}
        }
        return tak.size() == 3;
    }

}
