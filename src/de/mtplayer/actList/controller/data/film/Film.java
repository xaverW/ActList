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

package de.mtplayer.actList.controller.data.film;

import de.mtplayer.actList.controller.config.Const;
import de.mtplayer.mLib.tools.DatumFilm;
import de.mtplayer.mLib.tools.Log;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Film extends FilmProps {

    public Film() {
        filmSize = new FilmSize(0); // Dateigröße in MByte
    }

    public void init() {
        setHd(!arr[FILM_URL_HD].isEmpty() || !arr[FILM_URL_RTMP_HD].isEmpty());
        setSmall(!arr[FILM_URL_KLEIN].isEmpty() || !arr[FILM_URL_RTMP_KLEIN].isEmpty());
        setUt(!arr[FILM_URL_SUBTITLE].isEmpty());
        preserveMemory();

        // ================================
        // Dateigröße
        filmSize = new FilmSize(this);

        // ================================
        // Filmdauer
        setFilmdauer();

        // ================================
        // Datum
        setDatum();

        //=================================
        // Filmzeit
        setFilmTime();
    }

    public void initDate() {
        // ================================
        // Datum
        setDatum();
    }

    private void setFilmTime() {
        if (!arr[FILM_ZEIT].isEmpty()) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            LocalTime time = LocalTime.parse(arr[FILM_ZEIT], formatter);
            setFilmtime(time.toSecondOfDay());
        } else {
            setFilmtime(FILMTIME_EMPTY);
        }
    }

    public String getUrlFuerAufloesung(String aufloesung) {
        if (aufloesung.equals(AUFLOESUNG_KLEIN)) {
            return getUrlNormalKlein();
        }
        if (aufloesung.equals(AUFLOESUNG_HD)) {
            return getUrlNormalHd();
        }
        return arr[FILM_URL];
    }

    public String getIndex() {
        // liefert einen eindeutigen Index für die Filmliste (update der Filmliste mit Diff-Liste)
        // URL beim KiKa und ORF ändern sich laufend!
        return (arr[FILM_SENDER] + arr[FILM_THEMA]).toLowerCase() + getUrlForHash();
    }


    public String getUrlForHash() {
        // liefert die URL zum VERGLEICHEN!!
        String url = "";
        if (arr[FILM_SENDER].equals(Const.ORF)) {
            final String uurl = arr[FILM_URL];
            try {
                final String online = "/online/";
                url = uurl.substring(uurl.indexOf(online) + online.length());
                if (!url.contains("/")) {
                    Log.errorLog(915230478, "Url: " + uurl);
                    return "";
                }
                url = url.substring(url.indexOf('/') + 1);
                if (!url.contains("/")) {
                    Log.errorLog(915230478, "Url: " + uurl);
                    return "";
                }
                url = url.substring(url.indexOf('/') + 1);
                if (url.isEmpty()) {
                    Log.errorLog(915230478, "Url: " + uurl);
                    return "";
                }
            } catch (final Exception ex) {
                Log.errorLog(915230478, ex, "Url: " + uurl);
            }
            return Const.ORF + "----" + url;
        } else {
            return arr[FILM_URL];
        }

    }

    private void preserveMemory() {
        // ================================
        // Speicher sparen
        if (arr[FILM_GROESSE].length() < 3) { //todo brauchts das überhaupt??
            arr[FILM_GROESSE] = arr[FILM_GROESSE].intern();
        }
        if (arr[FILM_URL_KLEIN].length() < 15) {
            arr[FILM_URL_KLEIN] = arr[FILM_URL_KLEIN].intern();
        }

        arr[FILM_DATUM] = arr[FILM_DATUM].intern();
        arr[FILM_ZEIT] = arr[FILM_ZEIT].intern();
    }

    private String fuellen(int anz, String s) {
        while (s.length() < anz) {
            s = '0' + s;
        }
        return s;
    }

    private void setFilmdauer() {
        try {
            if (!arr[FILM_DAUER].contains(":") && !arr[FILM_DAUER].isEmpty()) {
                // nur als Übergang bis die Liste umgestellt ist
                long l = Long.parseLong(arr[FILM_DAUER]);
                dauerL = l;
                if (l > 0) {
                    final long hours = l / 3600;
                    l = l - (hours * 3600);
                    final long min = l / 60;
                    l = l - (min * 60);
                    final long seconds = l;
                    arr[FILM_DAUER] = fuellen(2, String.valueOf(hours)) + ':'
                            + fuellen(2, String.valueOf(min))
                            + ':'
                            + fuellen(2, String.valueOf(seconds));
                } else {
                    arr[FILM_DAUER] = "";
                }
            } else {
                dauerL = 0;
                if (!arr[FILM_DAUER].isEmpty()) {
                    final String[] parts = arr[FILM_DAUER].split(":");
                    long power = 1;
                    for (int i = parts.length - 1; i >= 0; i--) {
                        dauerL += Long.parseLong(parts[i]) * power;
                        power *= 60;
                    }
                }
            }
        } catch (final Exception ex) {
            dauerL = 0;
            Log.errorLog(468912049, "Dauer: " + arr[FILM_DAUER]);
        }
    }

    private void setDatum() {
        if (!arr[FILM_DATUM].isEmpty()) {
            // nur dann gibts ein Datum
            try {
                if (arr[FILM_DATUM_LONG].isEmpty()) {
                    if (arr[FILM_ZEIT].isEmpty()) {
                        datumFilm = new DatumFilm(sdf_datum.parse(arr[FILM_DATUM]).getTime());
                    } else {
                        datumFilm = new DatumFilm(sdf_datum_zeit.parse(arr[FILM_DATUM] + arr[FILM_ZEIT]).getTime());
                    }
                    arr[FILM_DATUM_LONG] = String.valueOf(datumFilm.getTime() / 1000);
                } else {
                    final long l = Long.parseLong(arr[FILM_DATUM_LONG]);
                    datumFilm = new DatumFilm(l * 1000 /* sind SEKUNDEN!! */);
                }
            } catch (final Exception ex) {
                Log.errorLog(915236701, ex, new String[]{"Datum: " + arr[FILM_DATUM], "Zeit: " + arr[FILM_ZEIT]});
                datumFilm = new DatumFilm(0);
                arr[FILM_DATUM] = "";
                arr[FILM_ZEIT] = "";
            }
        }
    }


    private String getUrlNormalKlein() {
        // liefert die kleine normale URL
        if (!arr[FILM_URL_KLEIN].isEmpty()) {
            try {
                final int i = Integer.parseInt(arr[FILM_URL_KLEIN].substring(0, arr[FILM_URL_KLEIN].indexOf('|')));
                return arr[FILM_URL].substring(0, i)
                        + arr[FILM_URL_KLEIN].substring(arr[FILM_URL_KLEIN].indexOf('|') + 1);
            } catch (final Exception ignored) {
            }
        }
        return arr[FILM_URL];
    }

    private String getUrlNormalHd() {
        // liefert die HD normale URL
        if (!arr[FILM_URL_HD].isEmpty()) {
            try {
                final int i = Integer.parseInt(arr[FILM_URL_HD].substring(0, arr[FILM_URL_HD].indexOf('|')));
                return arr[FILM_URL].substring(0, i)
                        + arr[FILM_URL_HD].substring(arr[FILM_URL_HD].indexOf('|') + 1);
            } catch (final Exception ignored) {
            }
        }
        return arr[FILM_URL];
    }


    public Film getCopy() {
        final Film ret = new Film();
        System.arraycopy(arr, 0, ret.arr, 0, arr.length);
        ret.datumFilm = datumFilm;
        ret.nr = nr;
        ret.filmSize = filmSize;
        ret.dauerL = dauerL;
        ret.setHd(isHd());
        ret.setSmall(isSmall());
        ret.setUt(isUt());
        return ret;
    }


}
