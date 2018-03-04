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

package de.mtplayer.actList.controller;

import de.mtplayer.actList.controller.config.Config;
import de.mtplayer.actList.controller.config.Const;
import de.mtplayer.actList.controller.config.Daten;
import de.mtplayer.actList.controller.config.ProgInfos;
import de.mtplayer.actList.controller.loadFilmlist.ReadFilmlist;
import de.mtplayer.mLib.MLInit;
import de.mtplayer.mLib.tools.Duration;
import de.mtplayer.mLib.tools.Log;
import de.mtplayer.mLib.tools.StringFormatters;
import de.mtplayer.mLib.tools.SysMsg;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;

public class ProgStart {
    Daten daten;

    public ProgStart(Daten daten) {
        this.daten = daten;
    }

    // #########################################################
    // Filmliste beim Programmstart!! laden
    // #########################################################
    public void loadDataProgStart() {
        // Gui startet ein wenig flüssiger
        new Thread(new loadFilmlistProgStart_()).start();
    }

    public static void startMeldungen() {
        Log.versionMsg(Const.PROGRAMMNAME);
        SysMsg.sysMsg("Programmpfad: " + ProgInfos.getPathJar());
        SysMsg.sysMsg("Verzeichnis Einstellungen: " + ProgInfos.getSettingsDirectory_String());
        SysMsg.sysMsg("");
        SysMsg.sysMsg(Log.LILNE);
        SysMsg.sysMsg("");
        SysMsg.sysMsg("");
    }

    private class loadFilmlistProgStart_ implements Runnable {

        @Override
        public synchronized void run() {
            Duration.staticPing("Programmstart Daten laden");

            final Daten daten = Daten.getInstance();

            new ReadFilmlist().readFilmListe(ProgInfos.getFilmListFile(),
                    daten.filmList, Config.SYSTEM_ANZ_TAGE_FILMLISTE.getInt());

            SysMsg.sysMsg("Liste Filme gelesen am: " + StringFormatters.FORMATTER_ddMMyyyyHHmm.format(new Date()));
            SysMsg.sysMsg("  erstellt am: " + daten.filmList.genDate());
            SysMsg.sysMsg("  Anzahl Filme: " + daten.filmList.size());

//            if (daten.filmList.isTooOld() && Config.SYSTEM_LOAD_FILME_START.getBool()) {
//                SysMsg.sysMsg("Filmliste zu alt, neue Filmliste laden");
//                daten.loadFilmList.loadFilmlist("", false);
//
//            } else {
//                // beim Neuladen wird es dann erst gemacht
//                daten.loadFilmList.notifyStart(new ListenerFilmListLoadEvent("", "", 0, 0, 0, false/* Fehler */));
//                daten.loadFilmList.afterFilmlistLoad();
//                daten.loadFilmList.notifyFertig(new ListenerFilmListLoadEvent("", "", 0, 0, 0, false/* Fehler */));
//            }
        }

    }


    /**
     * Config beim  Programmstart laden
     *
     * @return
     */
    public boolean allesLaden() {
        if (!load()) {
            SysMsg.sysMsg("Weder Konfig noch Backup konnte geladen werden!");
            // teils geladene Reste entfernen
            clearKonfig();
            return false;
        }
        SysMsg.sysMsg("Konfig wurde gelesen!");
        MLInit.initLib(Daten.debug, Const.PROGRAMMNAME, ProgInfos.getUserAgent());
        return true;
    }

    private void clearKonfig() {
        Daten daten = Daten.getInstance();
    }

    private boolean load() {
        Daten daten = Daten.getInstance();

        boolean ret = false;
        final Path xmlFilePath = new ProgInfos().getXmlFilePath();

        try (IoXmlLesen reader = new IoXmlLesen(daten)) {
            if (Files.exists(xmlFilePath)) {
                if (reader.readConfiguration(xmlFilePath)) {
                    return true;
                } else {
                    // dann hat das Laden nicht geklappt
                    SysMsg.sysMsg("Konfig konnte nicht gelesen werden!");
                }
            } else {
                // dann hat das Laden nicht geklappt
                SysMsg.sysMsg("Konfig existiert nicht!");
            }
        } catch (final Exception ex) {
            ex.printStackTrace();
        }

        return ret;
    }

}
