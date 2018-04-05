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

import de.mtplayer.actList.controller.config.Config;
import de.mtplayer.actList.controller.config.Daten;
import de.mtplayer.actList.gui.tools.GuiSize;
import de.p2tools.p2Lib.tools.Duration;
import de.p2tools.p2Lib.tools.Log;
import javafx.application.Platform;

public class ProgQuitt {
    final Daten daten;

    public ProgQuitt() {
        daten = Daten.getInstance();
    }

    private void writeWindowSizes() {
        // Hauptfenster
        GuiSize.getSizeScene(Config.SYSTEM_GROESSE_GUI, daten.primaryStage);
    }

    /**
     * Quit the MTPlayer application
     *
     * @param showOptionTerminate show options dialog when downloads are running
     * @param shutDown            try to shutdown the computer if requested
     */
    public void beenden(boolean showOptionTerminate, boolean shutDown) {
        if (beenden_(showOptionTerminate, shutDown)) {

            // dann jetzt beenden -> Thüss
            Platform.runLater(() -> {
                Platform.exit();
                System.exit(0);
            });

        }
    }

    private boolean beenden_(boolean showOptionTerminate, boolean shutDown) {
        writeWindowSizes();

        new ProgSave().allesSpeichern();

        Log.endMsg();
        Duration.printCounter();

        return true;
    }

}
