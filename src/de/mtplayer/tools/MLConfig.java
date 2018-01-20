/*
 * MTPlayer Copyright (C) 2017 W. Xaver W.Xaver[at]googlemail.com
 * https://sourceforge.net/projects/mtplayer/
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see <http://www.gnu.org/licenses/>.
 */


package de.mtplayer.tools;

import java.util.HashMap;
import java.util.LinkedList;

public class MLConfig {

    private static final HashMap<String, MLConfigs> HASHMAP = new HashMap<>();

    public static MLConfigs get(String key) {
        return HASHMAP.get(key);
    }

    public static synchronized void check(MLConfigs mlConfigs, int min, int max) {
        final int v = mlConfigs.getInt();
        if (v < min || v > max) {
            mlConfigs.setValue(mlConfigs.getInitValue());
        }
    }

    public static synchronized MLConfigs addNewKey(String key) {
        MLConfigs c = new MLConfigs(key);
        HASHMAP.put(key, c);
        return c;
    }

    public static synchronized MLConfigs addNewKey(String key, String value) {
        MLConfigs c = new MLConfigs(key, value == null ? "" : value);
        HASHMAP.put(key, c);
        return c;
    }

    public static synchronized MLConfigs addNewKey(String key, int value) {
        MLConfigs c = new MLConfigs(key, value);
        HASHMAP.put(key, c);
        return c;
    }

    public static synchronized String[][] getAll() {
        final LinkedList<String[]> liste = new LinkedList<>();
        for (MLConfigs c : HASHMAP.values()) {
            liste.add(new String[]{c.getKey(), c.getAktValue().getValueSafe()});
        }

        listeSort(liste, 0);
        return liste.toArray(new String[][]{});
    }

    private static void listeSort(LinkedList<String[]> liste, int stelle) {
        // Stringliste alphabetisch sortieren
        final GermanStringSorter sorter = GermanStringSorter.getInstance();
        if (liste == null) {
            return;
        }

        for (int i = 1; i < liste.size(); ++i) {
            for (int k = i; k > 0; --k) {
                final String str1 = liste.get(k - 1)[stelle];
                final String str2 = liste.get(k)[stelle];
                if (sorter.compare(str1, str2) > 0) {
                    liste.add(k - 1, liste.remove(k));
                } else {
                    break;
                }
            }
        }
    }
}
