/*
 * MTPlayer Copyright (C) 2017 W. Xaver W.Xaver[at]googlemail.com
 * https://www.p2tools.de
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

import de.mtplayer.actList.controller.config.ProgConst;
import de.mtplayer.actList.controller.config.ProgData;
import de.mtplayer.actList.controller.config.ProgInfos;
import de.mtplayer.mLib.MLInit;
import de.p2tools.p2Lib.tools.log.LogMessage;
import de.p2tools.p2Lib.tools.log.PLog;
import de.p2tools.p2Lib.tools.log.PLogger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class ProgStart {
    ProgData progData;

    public ProgStart(ProgData progData) {
        this.progData = progData;
    }

    // #########################################################
    // Filmliste beim Programmstart!! laden
    // #########################################################
//    public void loadDataProgStart() {
//        // Gui startet ein wenig flüssiger
////        Thread th = new Thread(new loadFilmlistProgStart_());
////        th.setName("loadDataProgStart");
////        th.start();
//    }

    public static void startMsg() {

        ArrayList<String> list = new ArrayList<>();
        list.add("Verzeichnisse:");
        list.add("Programmpfad: " + ProgInfos.getPathJar());
        list.add("Verzeichnis Einstellungen: " + ProgInfos.getSettingsDirectory_String());

        LogMessage.startMsg(ProgConst.PROGRAMMNAME, list);
    }

//    private class loadFilmlistProgStart_ implements Runnable {
//
//        @Override
//        public synchronized void run() {
//            PDuration.onlyPing("Programmstart Daten laden");
//
//            final ProgData progData = ProgData.getInstance();
//
////            new ReadFilmlistMeta().readFilmlist(ProgInfos.getFilmlistFile(),
////                    progData.filmlist, ProgConfig.SYSTEM_ANZ_TAGE_FILMLISTE.getInt());
//
////            ArrayList<String> list = new ArrayList<>();
////            list.add(PLog.LILNE3);
////            list.add("Liste Filme gelesen am: " + StringFormatters.FORMATTER_ddMMyyyyHHmm.format(new Date()));
////            list.add("  erstellt am: " + progData.filmlist.genDate());
////            list.add("  Anzahl Filme: " + progData.filmlist.size());
////            list.add(PLog.LILNE3);
////            PLog.sysLog(list);
//        }
//
//    }


    /**
     * ProgConfig beim  Programmstart laden
     *
     * @return
     */
    public boolean startAll() {
        boolean load = load();
        if (ProgData.debug) {
            PLogger.setFileHandler(ProgInfos.getLogDirectory_String());
        }

        if (!load) {
            PLog.sysLog("Weder Konfig noch Backup konnte geladen werden!");
            return false;
        }
        PLog.sysLog("Konfig wurde gelesen!");
        MLInit.initLib(ProgData.debug, ProgConst.PROGRAMMNAME, ProgInfos.getUserAgent());
        return true;
    }

    private boolean load() {
        ProgData progData = ProgData.getInstance();

        boolean ret = false;
        final Path xmlFilePath = new ProgInfos().getSettingsFile();

        try (IoReadXml reader = new IoReadXml(progData)) {
            if (Files.exists(xmlFilePath)) {
                if (reader.readConfiguration(xmlFilePath)) {
                    return true;
                } else {
                    // dann hat das Laden nicht geklappt
                    PLog.sysLog("Konfig konnte nicht gelesen werden!");
                }
            } else {
                // dann hat das Laden nicht geklappt
                PLog.sysLog("Konfig existiert nicht!");
            }
        } catch (final Exception ex) {
            ex.printStackTrace();
        }

        return ret;
    }

}
