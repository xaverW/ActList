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

package de.mtplayer.actList;

import de.mtplayer.actList.controller.config.ProgData;
import de.mtplayer.actList.gui.GuiPack;
import de.mtplayer.actList.gui.StatusBarController;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class ActListController extends BorderPane {

    private StatusBarController statusBarController;
    private GuiPack guiPack;

    private final ProgData progData;

    public ActListController() {
        progData = ProgData.getInstance();
        init();
    }

    private void init() {
        try {
            guiPack = new GuiPack();
            guiPack.pack();

            statusBarController = new StatusBarController(progData);
            VBox.setVgrow(statusBarController, Priority.NEVER);

            setCenter(guiPack);
            setBottom(statusBarController);
//            setPadding(new Insets(0));

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }


}
