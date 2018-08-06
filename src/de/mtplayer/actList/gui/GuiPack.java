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
import de.mtplayer.actList.controller.data.Icons;
import de.mtplayer.mLib.tools.DirFileChooser;
import de.mtplayer.mLib.tools.FileUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.ArrayList;

public class GuiPack extends AnchorPane {

    ProgData progData;
    private final FilmPaneController filmPaneController;
    private final UrlPaneController urlPaneController;
    private final ChannelPaneController channelPaneController;

    private final ComboBox<String> cbPath = new ComboBox<>();
    private final String[] storedPath = ProgConfig.SYSTEM_DEST_PATH.get().split(ProgConst.DIR_FILMLIST_SEPARATOR);
    private final Button btnDest = new Button("");

    public GuiPack() {
        progData = ProgData.getInstance();
        filmPaneController = new FilmPaneController();
        urlPaneController = new UrlPaneController();
        channelPaneController = new ChannelPaneController();
    }


    public void pack() {
        final TabPane tabPane = new TabPane();
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        Tab tab = new Tab("Filme");
        tab.setClosable(false);
        tab.setContent(filmPaneController);
        tabPane.getTabs().addAll(tab);

        tab = new Tab("Sender");
        tab.setClosable(false);
        tab.setContent(channelPaneController);
        tabPane.getTabs().addAll(tab);

        tab = new Tab("Filmliste");
        tab.setClosable(false);
        tab.setContent(urlPaneController);
        tabPane.getTabs().addAll(tab);


        Button btnLoad = new Button("Filmliste jetzt laden");
        btnLoad.getStyleClass().add("btnFilmlist");
        btnLoad.setPadding(new Insets(10));
        btnLoad.setOnAction(event -> {
            String fileDest = FileUtils.concatPaths(cbPath.getEditor().getText(), ProgConst.JSON_DATEI_FILME);
            progData.loadFilmlist.readWriteFilmlist(ProgConfig.SYSTEM_LOAD_FILMS_MANUAL.get(),
                    fileDest, ProgConfig.SYSTEM_ANZ_TAGE_FILMLISTE.getInt());
        });

        HBox hBoxLoad = new HBox();
        hBoxLoad.getStyleClass().add("dialog-title-border");
        hBoxLoad.getChildren().add(btnLoad);


        cbPath.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(cbPath, Priority.ALWAYS);

        Label lblDest = new Label("Speicherziel:");
        lblDest.setMinWidth(Region.USE_PREF_SIZE);

        HBox hBoxDest = new HBox();
        hBoxDest.setAlignment(Pos.CENTER_LEFT);
        hBoxDest.setSpacing(10);
        hBoxDest.getChildren().addAll(lblDest, cbPath, btnDest);


        VBox vBox = new VBox(10);
        VBox.setVgrow(vBox, Priority.ALWAYS);
        vBox.getStyleClass().add("dialog-border");
        vBox.getChildren().addAll(hBoxDest, tabPane);


        VBox vBoxAll = new VBox(10);
        vBoxAll.getChildren().addAll(hBoxLoad, vBox);


        getChildren().addAll(vBoxAll);
        AnchorPane.setLeftAnchor(vBoxAll, 10.0);
        AnchorPane.setBottomAnchor(vBoxAll, 10.0);
        AnchorPane.setRightAnchor(vBoxAll, 10.0);
        AnchorPane.setTopAnchor(vBoxAll, 10.0);

        initPath();
    }

    private void initPath() {
        btnDest.setGraphic(new Icons().ICON_BUTTON_FILE_OPEN);
        btnDest.setText("");
        btnDest.setTooltip(new Tooltip("Einen Pfad zum Speichern der Filmliste auswählen."));
        btnDest.setOnAction(event -> DirFileChooser.DirChooser(ProgData.getInstance().primaryStage, cbPath));

        cbPath.setEditable(true);
        cbPath.getItems().addAll(storedPath);

        cbPath.getSelectionModel().selectFirst();
        cbPath.getEditor().textProperty().addListener((observable, oldValue, newValue) -> saveComboPath());
    }

    private void saveComboPath() {
        final ArrayList<String> path = new ArrayList<>(cbPath.getItems());

        final ArrayList<String> path2 = new ArrayList<>();
        String sel = cbPath.getEditor().getText();
        if (sel != null && !sel.isEmpty()) {
            path2.add(sel);
        }

        path.stream().forEach(s1 -> {
            // um doppelte auszusortieren
            if (!path2.contains(s1)) {
                path2.add(s1);
            }
        });

        String s = "";
        if (!path2.isEmpty()) {
            s = path2.get(0);
            for (int i = 1; i < ProgConst.MAX_PATH_DIALOG_DOWNLOAD && i < path2.size(); ++i) {
                if (!path2.get(i).isEmpty()) {
                    s += ProgConst.DIR_FILMLIST_SEPARATOR + path2.get(i);
                }
            }
        }

        ProgConfig.SYSTEM_DEST_PATH.setValue(s);
    }
}
