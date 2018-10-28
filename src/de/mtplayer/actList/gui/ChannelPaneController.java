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
import de.mtplayer.actList.controller.config.ProgConst;
import de.mtplayer.actList.controller.config.ProgData;
import de.mtplayer.actList.gui.tools.HelpText;
import de.p2tools.p2Lib.guiTools.PButton;
import javafx.beans.property.StringProperty;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.ArrayList;
import java.util.Arrays;

public class ChannelPaneController extends AnchorPane {

    private final ProgData progData;
    private final GridPane gridPane = new GridPane();

    StringProperty propChannel = ProgConfig.SYSTEM_LOAD_NOT_SENDER.getStringProperty();


    public ChannelPaneController() {
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
        final ListView<CheckBox> lv = new ListView<>();
        GridPane.setVgrow(lv, Priority.ALWAYS);
//        progData.loadFilmlist.updateDownloadUrlsFilmlisten();

        ArrayList aListChannel = new ArrayList(Arrays.asList(propChannel.getValue().split(",")));
        ArrayList<CheckBox> aListCb = new ArrayList<>();

        for (String s : ProgConst.SENDER) {
            final CheckBox cb = new CheckBox(s);
            aListCb.add(cb);

            lv.getItems().add(cb);
            cb.setSelected(aListChannel.contains(s));
            cb.setOnAction(a -> {
                makeProp(aListCb);
            });
        }

        final Button btnHelp = new PButton().helpButton("Filmliste laden",
                HelpText.LOAD_FILMLIST_SENDER);

        gridPane.add(new Label("Sender nicht laden"), 0, 0);
        gridPane.add(lv, 0, 1);
        GridPane.setValignment(btnHelp, VPos.TOP);
        GridPane.setHalignment(btnHelp, HPos.RIGHT);
        gridPane.add(btnHelp, 1, 1);

        final ColumnConstraints ccTxt = new ColumnConstraints();
        ccTxt.setFillWidth(true);
        ccTxt.setMinWidth(Region.USE_COMPUTED_SIZE);
        ccTxt.setHgrow(Priority.ALWAYS);
        gridPane.getColumnConstraints().addAll(new ColumnConstraints(), ccTxt);
    }

    private void makeProp(ArrayList<CheckBox> aListCb) {
        String str = "";
        for (CheckBox cb : aListCb) {
            if (!cb.isSelected()) {
                continue;
            }

            String s = cb.getText();
            str = str.isEmpty() ? s : str + "," + s;
        }
        propChannel.setValue(str);
    }
}