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

package de.mtplayer.actList.gui;

import de.mtplayer.actList.controller.config.Config;
import de.mtplayer.actList.controller.config.Daten;
import de.mtplayer.actList.controller.data.Icons;
import de.mtplayer.actList.gui.dialog.MTAlert;
import de.mtplayer.actList.gui.tools.HelpText;
import de.mtplayer.mLib.tools.Log;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.ArrayList;

public class UrlPaneController extends AnchorPane {

    private final Daten daten;
    private final GridPane gridPane = new GridPane();
    private final TextField txtUrl = new TextField("");

    StringProperty propUrl = Config.SYSTEM_LOAD_FILME_MANUELL.getStringProperty();

    public UrlPaneController() {
        daten = Daten.getInstance();

        final ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);
        AnchorPane.setLeftAnchor(scrollPane, 10.0);
        AnchorPane.setBottomAnchor(scrollPane, 10.0);
        AnchorPane.setRightAnchor(scrollPane, 10.0);
        AnchorPane.setTopAnchor(scrollPane, 10.0);

        gridPane.setHgap(15);
        gridPane.setVgap(15);
        gridPane.setPadding(new Insets(20, 20, 20, 20));

        scrollPane.setContent(gridPane);
        getChildren().addAll(scrollPane);

        makeLoadManuel();
    }

    private void makeLoadManuel() {
        final ListView<String> lv = new ListView<>();
        GridPane.setVgrow(lv, Priority.ALWAYS);
        daten.loadFilmList.updateDownloadUrlsFilmlisten();
        lv.setOnMouseClicked(a -> {
            String str = lv.getSelectionModel().getSelectedItem();
            if (str != null && !str.isEmpty()) {
                txtUrl.setText(str);
            }
        });

        final Button btnGetUrls = new Button();
        btnGetUrls.setOnAction(event -> {
            daten.loadFilmList.updateDownloadUrlsFilmlisten();
            ArrayList<String> al = daten.loadFilmList.getDownloadUrlsFilmlisten_akt().getUrls();
            Log.sysLog(al.size() + " URL’s eingetragen");
            lv.getItems().clear();
            lv.getItems().addAll(al);
        });
        btnGetUrls.setGraphic(new Icons().ICON_BUTTON_RESET);

        final Button btnHelpUrl = new Button("");
        btnHelpUrl.setGraphic(new Icons().ICON_BUTTON_HELP);
        btnHelpUrl.setOnAction(a -> new MTAlert().showHelpAlert("Filmliste laden",
                HelpText.LOAD_FILMLIST_URL));

        VBox vBox = new VBox();
        vBox.setSpacing(10);
        vBox.getChildren().addAll(btnGetUrls, btnHelpUrl);
        GridPane.setValignment(vBox, VPos.TOP);

        txtUrl.textProperty().bindBidirectional(propUrl);

        final Button btnHelp = new Button("");
        btnHelp.setGraphic(new Icons().ICON_BUTTON_HELP);
        btnHelp.setOnAction(a -> new MTAlert().showHelpAlert("Filmliste laden",
                HelpText.LOAD_FILMLIST_MANUEL));

        Label lblTxt = new Label("URL’s:");
        GridPane.setValignment(lblTxt, VPos.TOP);
        gridPane.add(lblTxt, 0, 0);
        gridPane.add(lv, 1, 0);
        gridPane.add(vBox, 2, 0);

        gridPane.add(txtUrl, 1, 1);
        gridPane.add(btnHelp, 2, 1);


        final ColumnConstraints ccTxt = new ColumnConstraints();
        ccTxt.setFillWidth(true);
        ccTxt.setMinWidth(Region.USE_COMPUTED_SIZE);
        ccTxt.setHgrow(Priority.ALWAYS);
        gridPane.getColumnConstraints().addAll(new ColumnConstraints(), ccTxt);
    }

}