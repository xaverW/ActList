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

import de.mtplayer.actList.controller.config.Daten;
import de.mtplayer.actList.gui.GuiPack;
import de.mtplayer.actList.gui.StatusBarController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.controlsfx.control.MaskerPane;

public class ActListController extends StackPane {

    BorderPane borderPane = new BorderPane();
    StackPane stackPaneCont = new StackPane();
    private MaskerPane maskerPane = new MaskerPane();
    private StatusBarController statusBarController;
    private GuiPack guiPack;

    private final Daten daten;


    public ActListController() {
        daten = Daten.getInstance();
        init();
    }

    private void init() {
        try {
            guiPack = new GuiPack();
            guiPack.pack();
            stackPaneCont.getChildren().addAll(guiPack);

            statusBarController = new StatusBarController(daten);
            VBox.setVgrow(statusBarController, Priority.NEVER);

            borderPane.setCenter(stackPaneCont);
            borderPane.setBottom(statusBarController);

            this.setPadding(new Insets(0));
            this.getChildren().addAll(borderPane, maskerPane);

            StackPane.setAlignment(maskerPane, Pos.CENTER);
            maskerPane.setPadding(new Insets(3, 1, 1, 1));
            maskerPane.toFront();
            maskerPane.setVisible(false);

            guiPack.toFront();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }


}
