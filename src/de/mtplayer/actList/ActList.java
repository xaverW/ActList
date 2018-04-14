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
import de.mtplayer.actList.controller.config.Config;
import de.mtplayer.actList.controller.config.Const;
import de.mtplayer.actList.controller.config.Daten;
import de.mtplayer.actList.controller.loadFilmlist.ListenerFilmlistLoad;
import de.mtplayer.actList.controller.loadFilmlist.ListenerFilmlistLoadEvent;
import de.mtplayer.actList.gui.tools.GuiSize;
import de.mtplayer.actList.gui.tools.Listener;
import de.mtplayer.actList.res.GetIcon;
import de.mtplayer.mLib.tools.Functions;
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
    private static final String TITLE_TEXT_PROGRAMMVERSION_IST_AKTUELL = "Programmversion ist aktuell";
    private static final String TITLE_TEXT_EIN_PROGRAMMUPDATE_IST_VERFUEGBAR = "Ein Programmupdate ist verfügbar";

    protected Daten daten;
    ProgStart progStart;
    Scene scene = null;

    @Override
    public void init() throws Exception {
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        Duration.counterStart(LOG_TEXT_PROGRAMMSTART);
        daten = Daten.getInstance();
        daten.primaryStage = primaryStage;
        progStart = new ProgStart(daten);

        loadData();
        initRootLayout();
        losGehts();
    }

    private void initRootLayout() {
        try {
            root = new ActListController();
            daten.actListController = root;
            scene = new Scene(root,
                    GuiSize.getWidth(Config.SYSTEM_GROESSE_GUI),
                    GuiSize.getHeight(Config.SYSTEM_GROESSE_GUI));

            String css = this.getClass().getResource(Const.CSS_FILE).toExternalForm();
            scene.getStylesheets().add(css);

            primaryStage.setScene(scene);
            primaryStage.setOnCloseRequest(e -> {
                e.consume();
                new ProgQuitt().beenden(true, false);
            });

            GuiSize.setPos(Config.SYSTEM_GROESSE_GUI, primaryStage);
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

        addListener();

        Duration.staticPing("Gui steht!");
        progStart.loadDataProgStart();
    }

    private void loadData() {
        Duration.staticPing("Start");
        Config.loadSystemParameter();
        progStart.allesLaden();
    }

    private void addListener() {
        Listener.addListener(new Listener(Listener.EREIGNIS_GUI_ORG_TITEL, ActList.class.getSimpleName()) {
            @Override
            public void ping() {
                setOrgTitel();
            }
        });
        Listener.addListener(new Listener(Listener.EREIGNIS_GUI_PROGRAMM_AKTUELL, ActList.class.getSimpleName()) {
            @Override
            public void ping() {
                primaryStage.setTitle(TITLE_TEXT_PROGRAMMVERSION_IST_AKTUELL);
            }
        });
        Listener.addListener(new Listener(Listener.EREIGNIS_GUI_UPDATE_VERFUEGBAR, ActList.class.getSimpleName()) {
            @Override
            public void ping() {
                primaryStage.setTitle(TITLE_TEXT_EIN_PROGRAMMUPDATE_IST_VERFUEGBAR);
            }
        });
    }

    private void setOrgTitel() {
        primaryStage.setTitle(Const.PROGRAMMNAME + " " + Functions.getProgVersion());
    }

    private void initProg() {
        daten.loadFilmlist.addAdListener(new ListenerFilmlistLoad() {
            @Override
            public void start(ListenerFilmlistLoadEvent event) {

            }

            @Override
            public void fertig(ListenerFilmlistLoadEvent event) {
                new ProgSave().allesSpeichern(); // damit nichts verlorengeht
            }

            @Override
            public void fertigOnlyOne(ListenerFilmlistLoadEvent event) {
            }
        });

    }
}
