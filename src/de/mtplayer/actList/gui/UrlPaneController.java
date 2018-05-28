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

import de.mtplayer.actList.controller.config.ProgConfig;
import de.mtplayer.actList.controller.config.ProgData;
import de.mtplayer.actList.controller.data.Icons;
import de.mtplayer.actList.gui.tools.HelpText;
import de.p2tools.p2Lib.guiTools.PButton;
import de.p2tools.p2Lib.tools.log.PLog;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.ArrayList;

public class UrlPaneController extends AnchorPane {

    private final ProgData progData;
    private final GridPane gridPane = new GridPane();
    private final TextField txtUrl = new TextField("");

    StringProperty propUrl = ProgConfig.SYSTEM_LOAD_FILMS_MANUAL.getStringProperty();

    public UrlPaneController() {
        progData = ProgData.getInstance();

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
        progData.loadFilmlist.updateDownloadUrlsFilmlisten();
        lv.setOnMouseClicked(a -> {
            String str = lv.getSelectionModel().getSelectedItem();
            if (str != null && !str.isEmpty()) {
                txtUrl.setText(str);
            }
        });

        final Button btnGetUrls = new Button();
        btnGetUrls.setOnAction(event -> {
            progData.loadFilmlist.updateDownloadUrlsFilmlisten();
            ArrayList<String> al = progData.loadFilmlist.getDownloadUrlsFilmlisten_akt().getUrls();
            PLog.sysLog(al.size() + " URL’s eingetragen");
            lv.getItems().clear();
            lv.getItems().addAll(al);
        });
        btnGetUrls.setGraphic(new Icons().ICON_BUTTON_RESET);

        final Button btnHelpUrl = new PButton().helpButton("Filmliste laden",
                HelpText.LOAD_FILMLIST_URL);

        VBox vBox = new VBox();
        vBox.setSpacing(10);
        vBox.getChildren().addAll(btnGetUrls, btnHelpUrl);
        GridPane.setValignment(vBox, VPos.TOP);

        txtUrl.textProperty().bindBidirectional(propUrl);

        final Button btnHelp = new PButton().helpButton("Filmliste laden",
                HelpText.LOAD_FILMLIST_MANUAL);

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