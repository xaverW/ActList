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
import de.mtplayer.actList.gui.tools.HelpText;
import de.p2tools.p2Lib.guiTools.PButton;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.layout.*;

public class FilmPaneController extends AnchorPane {

    private final int FILTER_DAYS_MAX = 150;
    private final ProgData progData;
    private final ScrollPane scrollPane = new ScrollPane();
    private final GridPane gridPane = new GridPane();

    private final Slider slDays = new Slider();
    private final Label lblDays = new Label("");

    IntegerProperty propDay = ProgConfig.SYSTEM_ANZ_TAGE_FILMLISTE.getIntegerProperty();
    BooleanProperty propLoad = ProgConfig.SYSTEM_LOAD_FILMS_START.getBooleanProperty();

    public FilmPaneController() {
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

        makeConfig();
    }

    private void makeConfig() {
        initDays();

        final Button btnHelpDays = new PButton().helpButton("nur Filme der letzten Tage laden",
                HelpText.LOAD_FILM_ONLY_DAYS);


        VBox vBox = new VBox();
        vBox.setSpacing(10);
        vBox.getChildren().addAll(new Label("nur aktuelle Filme laden:"), slDays);
        gridPane.add(vBox, 0, 1);
        GridPane.setValignment(lblDays, VPos.BOTTOM);
        gridPane.add(lblDays, 1, 1);
        GridPane.setHalignment(btnHelpDays, HPos.RIGHT);
        gridPane.add(btnHelpDays, 2, 1);


        final ColumnConstraints ccTxt = new ColumnConstraints();
        ccTxt.setFillWidth(true);
        ccTxt.setMinWidth(Region.USE_COMPUTED_SIZE);
        ccTxt.setHgrow(Priority.ALWAYS);
        gridPane.getColumnConstraints().addAll(new ColumnConstraints(), ccTxt);
    }


    private void initDays() {
        slDays.setMin(0);
        slDays.setMax(FILTER_DAYS_MAX);
        slDays.setShowTickLabels(false);
        slDays.setMajorTickUnit(10);
        slDays.setBlockIncrement(10);

        slDays.valueProperty().bindBidirectional(propDay);
        slDays.valueProperty().addListener((observable, oldValue, newValue) -> setValueSlider());

        setValueSlider();
    }

    private void setValueSlider() {
        int days = (int) slDays.getValue();
        lblDays.setText(days == 0 ? "alles laden" : "nur Filme der letzten " + days + " Tage laden");
    }

}