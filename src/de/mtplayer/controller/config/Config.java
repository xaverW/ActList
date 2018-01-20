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


package de.mtplayer.controller.config;

import de.mtplayer.mLib.tools.MLConfig;
import de.mtplayer.mLib.tools.MLConfigs;

public class Config extends MLConfig {

    public static final String SYSTEM = "system";

    // Fenstereinstellungen
    public static MLConfigs SYSTEM_GROESSE_GUI = addNewKey("Groesse-Gui", "1000:900");
    public static MLConfigs SYSTEM_GROESSE_DIALOG_FILMINFO = addNewKey("Groesse-Filminfo", "600:800");

    // Einstellungen Filmliste
    public static MLConfigs SYSTEM_LOAD_FILME_START = addNewKey("system-load-filme-start", Boolean.TRUE.toString());
    public static MLConfigs SYSTEM_LOAD_FILME_MANUELL = addNewKey("system-load-filme-manuell", "");
    public static MLConfigs SYSTEM_LOAD_NOT_SENDER = addNewKey("system-load-not-sender", "");
    public static MLConfigs SYSTEM_ANZ_TAGE_FILMLISTE = addNewKey("system-anz-tage-filmilste", "0"); //es werden nur die x letzten Tage geladen
    public static MLConfigs SYSTEM_DEST_PATH = addNewKey("system-dest-path", ProgInfos.getDirectory_mtplayer_mv());

    public static void loadSystemParameter() {
        // auch wenn leer, sonst sind die Configs nicht geladen
    }

}
