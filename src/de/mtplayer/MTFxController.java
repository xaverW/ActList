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

package de.mtplayer;

import de.mtplayer.controller.config.Daten;
import de.mtplayer.gui.FilmPaneController;
import de.mtplayer.gui.StatusBarController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.*;
import org.controlsfx.control.MaskerPane;

public class MTFxController extends StackPane {

    BorderPane borderPane = new BorderPane();
    StackPane stackPaneCont = new StackPane();
    private MaskerPane maskerPane = new MaskerPane();
    private StatusBarController statusBarController;
    private FilmPaneController filmPaneController;

    private final Daten daten;


    public MTFxController() {
        daten = Daten.getInstance();
        init();
    }

    private void init() {
        try {
            // Top
            this.setPadding(new Insets(0));

            HBox hBoxTop = new HBox();
            hBoxTop.setPadding(new Insets(10));
            hBoxTop.setSpacing(20);
            hBoxTop.setAlignment(Pos.CENTER);
            HBox.setHgrow(hBoxTop, Priority.ALWAYS);

            filmPaneController = new FilmPaneController();
            stackPaneCont.getChildren().addAll(filmPaneController);

            statusBarController = new StatusBarController(daten);

            VBox.setVgrow(hBoxTop, Priority.NEVER);
            VBox.setVgrow(statusBarController, Priority.NEVER);

            borderPane.setTop(hBoxTop);
            borderPane.setCenter(stackPaneCont);
            borderPane.setBottom(statusBarController);

            this.setPadding(new Insets(0));
            maskerPane.setPadding(new Insets(3, 1, 1, 1));
            this.getChildren().addAll(borderPane, maskerPane);
            StackPane.setAlignment(maskerPane, Pos.CENTER);
            maskerPane.toFront();
            maskerPane.setVisible(false);


            filmPaneController.toFront();

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }


}
