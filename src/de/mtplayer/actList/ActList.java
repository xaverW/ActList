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

import de.mtplayer.actList.controller.ProgQuitt;
import de.mtplayer.actList.controller.ProgSave;
import de.mtplayer.actList.controller.ProgStart;
import de.mtplayer.actList.controller.config.ProgConfig;
import de.mtplayer.actList.controller.config.ProgConst;
import de.mtplayer.actList.controller.config.ProgData;
import de.mtplayer.actList.res.GetIcon;
import de.mtplayer.mLib.tools.Functions;
import de.mtplayer.mtp.controller.filmlist.loadFilmlist.ListenerFilmlistLoad;
import de.mtplayer.mtp.controller.filmlist.loadFilmlist.ListenerFilmlistLoadEvent;
import de.p2tools.p2Lib.guiTools.GuiSize;
import de.p2tools.p2Lib.guiTools.PButton;
import de.p2tools.p2Lib.tools.log.Duration;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ActList extends Application {

    private Stage primaryStage;
    private ActListController root;

    private static final String ICON_NAME = "Icon.png";
    private static final String ICON_PATH = "/de/mtplayer/actList/res/";
    private static final int ICON_WIDTH = 58;
    private static final int ICON_HEIGHT = 58;

    private static final String LOG_TEXT_PROGRAMMSTART = "***Programmstart***";

    protected ProgData progData;
    ProgStart progStart;
    Scene scene = null;

    @Override
    public void init() throws Exception {
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        Duration.counterStart(LOG_TEXT_PROGRAMMSTART);
        progData = ProgData.getInstance();
        progData.primaryStage = primaryStage;
        progStart = new ProgStart(progData);

        initP2();
        loadData();
        initRootLayout();
        losGehts();
    }

    private void initP2() {
        PButton.setHlpImage(GetIcon.getImage("button-help.png", 16, 16));
    }

    private void loadData() {
        Duration.staticPing("Start");
        ProgConfig.loadSystemParameter();
        progStart.startAll();
    }

    private void initRootLayout() {
        try {
            root = new ActListController();
            progData.actListController = root;
            scene = new Scene(root,
                    GuiSize.getWidth(ProgConfig.SYSTEM_GROESSE_GUI.getStringProperty()),
                    GuiSize.getHeight(ProgConfig.SYSTEM_GROESSE_GUI.getStringProperty()));

            String css = this.getClass().getResource(ProgConst.CSS_FILE).toExternalForm();
            scene.getStylesheets().add(css);

            primaryStage.setScene(scene);
            primaryStage.setOnCloseRequest(e -> {
                e.consume();
                new ProgQuitt().quitt();
            });

            GuiSize.setPos(ProgConfig.SYSTEM_GROESSE_GUI.getStringProperty(), primaryStage);
            primaryStage.show();

        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    public void losGehts() {
        Duration.counterStop(LOG_TEXT_PROGRAMMSTART);
        primaryStage.getIcons().add(GetIcon.getImage(ICON_NAME, ICON_PATH, ICON_WIDTH, ICON_HEIGHT));

        progStart.startMsg();

        Duration.staticPing("Erster Start");
        setOrgTitel();
        initProg();

        Duration.staticPing("Gui steht!");
        progStart.loadDataProgStart();
    }

    private void setOrgTitel() {
        primaryStage.setTitle(ProgConst.PROGRAMMNAME + " " + Functions.getProgVersion());
    }

    private void initProg() {
        progData.loadFilmlist.addAdListener(new ListenerFilmlistLoad() {
            @Override
            public void fertig(ListenerFilmlistLoadEvent event) {
                new ProgSave().saveAll(); // damit nichts verlorengeht
            }
        });

    }
}
