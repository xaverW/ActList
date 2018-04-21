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

import de.mtplayer.actList.controller.config.ProgConfig;
import de.mtplayer.actList.controller.config.ProgData;
import de.p2tools.p2Lib.guiTools.GuiSize;
import de.p2tools.p2Lib.tools.log.LogMsg;
import javafx.application.Platform;

public class ProgQuitt {
    final ProgData progData;

    public ProgQuitt() {
        progData = ProgData.getInstance();
    }

    /**
     * Quit the MTPlayer application
     */
    public void quitt() {

        GuiSize.getSizeScene(ProgConfig.SYSTEM_GROESSE_GUI.getStringProperty(), progData.primaryStage);
        new ProgSave().saveAll();
        LogMsg.endMsg();

        // dann jetzt beenden -> Thüss
        Platform.runLater(() -> {
            Platform.exit();
            System.exit(0);
        });

    }

}
