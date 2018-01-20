/*
 * MTPlayer Copyright (C) 2017 W. Xaver W.Xaver[at]googlemail.com
 * http://zdfmediathk.sourceforge.net/
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


package de.mtplayer.gui;

import de.mtplayer.controller.config.Config;
import de.mtplayer.controller.config.Const;
import de.mtplayer.controller.config.Daten;
import de.mtplayer.controller.data.Icons;
import de.mtplayer.mLib.tools.DirFileChooser;
import de.mtplayer.mLib.tools.FileUtils;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.ArrayList;

public class GuiPack extends AnchorPane {

    Daten daten;
    private final FilmPaneController filmPaneController;
    private final UrlPaneController urlPaneController;
    private final SenderPaneController senderPaneController;

    private final ComboBox<String> cbPath = new ComboBox<>();
    private final String[] storedPath = Config.SYSTEM_DEST_PATH.get().split(Const.DIR_FILMLIST_SEPARATOR);
    private final Button btnDest = new Button("");

    public GuiPack() {
        daten = Daten.getInstance();
        filmPaneController = new FilmPaneController();
        urlPaneController = new UrlPaneController();
        senderPaneController = new SenderPaneController();
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
        tab.setContent(senderPaneController);
        tabPane.getTabs().addAll(tab);

        tab = new Tab("Filmliste");
        tab.setClosable(false);
        tab.setContent(urlPaneController);
        tabPane.getTabs().addAll(tab);

        Button btnLoad = new Button("Filmliste jetzt laden");
        btnLoad.setOnAction(event -> {
            String fileDest = FileUtils.concatPaths(cbPath.getEditor().getText(), Const.JSON_DATEI_FILME);
            daten.loadFilmList.readWriteFilmlist(Config.SYSTEM_LOAD_FILME_MANUELL.get(),
                    fileDest,
                    daten.filmList, Config.SYSTEM_ANZ_TAGE_FILMLISTE.getInt());
        });

        HBox hBoxLoad = new HBox();
        hBoxLoad.setAlignment(Pos.TOP_RIGHT);
        hBoxLoad.getChildren().add(btnLoad);

        cbPath.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(cbPath, Priority.ALWAYS);

        HBox hBoxDest = new HBox();
        hBoxDest.setAlignment(Pos.CENTER_LEFT);
        hBoxDest.setSpacing(10);
        hBoxDest.getChildren().addAll(new Label("Speicherziel:"), cbPath, btnDest);

        VBox vBox = new VBox();
        vBox.setSpacing(10);
        vBox.setAlignment(Pos.TOP_CENTER);
        vBox.getChildren().addAll(hBoxLoad, hBoxDest, tabPane);

        getChildren().addAll(vBox);

        AnchorPane.setLeftAnchor(vBox, 10.0);
        AnchorPane.setBottomAnchor(vBox, 10.0);
        AnchorPane.setRightAnchor(vBox, 10.0);
        AnchorPane.setTopAnchor(vBox, 10.0);

        initPath();
    }

    private void initPath() {
        btnDest.setGraphic(new Icons().ICON_BUTTON_FILE_OPEN);
        btnDest.setText("");
        btnDest.setOnAction(event -> DirFileChooser.DirChooser(Daten.getInstance().primaryStage, cbPath));

        cbPath.setEditable(true);
        cbPath.getItems().addAll(storedPath);

        cbPath.getSelectionModel().selectFirst();
        cbPath.getEditor().textProperty().addListener((observable, oldValue, newValue) -> saveComboPfad());
    }

    private void saveComboPfad() {
        final ArrayList<String> pfade = new ArrayList<>(cbPath.getItems());

        final ArrayList<String> pfade2 = new ArrayList<>();
        String sel = cbPath.getEditor().getText();
        if (sel != null && !sel.isEmpty()) {
            System.out.println(sel);
            pfade2.add(sel);
        }

        pfade.stream().forEach(s1 -> {
            // um doppelte auszusortieren
            if (!pfade2.contains(s1)) {
                pfade2.add(s1);
            }
        });

        String s = "";
        if (!pfade2.isEmpty()) {
            s = pfade2.get(0);
            for (int i = 1; i < Const.MAX_PFADE_DIALOG_DOWNLOAD && i < pfade2.size(); ++i) {
                if (!pfade2.get(i).isEmpty()) {
                    s += Const.DIR_FILMLIST_SEPARATOR + pfade2.get(i);
                }
            }
        }

        Config.SYSTEM_DEST_PATH.setValue(s);
    }
}
