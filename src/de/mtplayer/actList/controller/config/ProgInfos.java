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

package de.mtplayer.actList.controller.config;

import de.mtplayer.actList.Main;
import de.p2tools.p2Lib.PConst;
import de.p2tools.p2Lib.configFile.SettingsDirectory;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;

public class ProgInfos {

    private static final String ERROR_CANT_CREATE_FOLDER = "Der Ordner konnte nicht angelegt werden." + PConst.LINE_SEPARATOR +
            "Bitte prüfen Sie die Dateirechte.";

    public static String getUserAgent() {
        return "";
    }


    /**
     * Retrieve the path to the program jar file.
     *
     * @return The program jar file path with a separator added.
     */
    public static String getPathJar() {
        // macht Probleme bei Win und Netzwerkpfaden, liefert dann Absolute Pfade zB. \\VBOXSVR\share\Mediathek\...
        final String pFilePath = "pFile";
        File propFile = new File(pFilePath);
        if (!propFile.exists()) {
            try {
                final CodeSource cS = Main.class.getProtectionDomain().getCodeSource();
                final File jarFile = new File(cS.getLocation().toURI().getPath());
                final String jarDir = jarFile.getParentFile().getPath();
                propFile = new File(jarDir + File.separator + pFilePath);
            } catch (final Exception ignored) {
            }
        }
        String s = propFile.getAbsolutePath().replace(pFilePath, "");
        if (!s.endsWith(File.separator)) {
            s = s + File.separator;
        }
        return s;
    }

    /**
     * Liefert den Pfad zur Filmliste
     *
     * @return Den Pfad als String
     */
    public static String getFilmlistFile() {
        return ProgInfos.getSettingsDirectory_String() + File.separator + ProgConst.JSON_DATEI_FILME;
    }

    public static String getLogDirectory_String() {
        final String logDir;
        if (ProgConfig.SYSTEM_LOG_DIR.get().isEmpty()) {
            logDir = Paths.get(getSettingsDirectory_String(), ProgConst.LOG_DIR).toString();
        } else {
            logDir = ProgConfig.SYSTEM_LOG_DIR.get();
        }
        return logDir;
    }

    public static Path getSettingsFile() {
        return ProgInfos.getSettingsDirectory().resolve(ProgConst.CONFIG_FILE);
    }

    public static Path getSettingsDirectory() throws IllegalStateException {
        return SettingsDirectory.getSettingsDirectory(ProgData.configDir,
                ProgConst.CONFIG_DIRECTORY);
    }

    public static String getSettingsDirectory_String() {
        return getSettingsDirectory().toString();
    }

    public static String getDirectory_mtplayer_mv() {
        Path baseDirMV = Paths.get(System.getProperty("user.home"), ProgConst.DIR_MEDIATHEKVIEW);

        Path baseDirMtplayer;
        if (SystemUtils.IS_OS_WINDOWS) {
            baseDirMtplayer = Paths.get(System.getProperty("user.home"), ProgConst.DIR_MTPLAYER_WIN);
        } else {
            baseDirMtplayer = Paths.get(System.getProperty("user.home"), ProgConst.DIR_MTPLAYER_X);
        }

        return baseDirMtplayer.toString() + ProgConst.DIR_FILMLIST_SEPARATOR + baseDirMV.toString();
    }

}
