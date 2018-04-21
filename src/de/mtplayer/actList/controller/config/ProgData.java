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


package de.mtplayer.actList.controller.config;

import de.mtplayer.actList.ActListController;
import de.mtplayer.actList.controller.loadFilmlist.LoadFilmlist;
import de.mtplayer.actList.gui.tools.Listener;
import de.mtplayer.mtp.controller.data.film.Filmlist;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.stage.Stage;
import javafx.util.Duration;


public class ProgData {

    private static ProgData instance;

    // flags
    public static boolean debug = false; // Debugmodus

    // Infos
    public static String configDir = ""; // Verzeichnis zum Speichern der Programmeinstellungen

    // zentrale Klassen
    public LoadFilmlist loadFilmlist; // erledigt das updaten der Filmliste

    // Gui
    public Stage primaryStage = null;
    public ActListController actListController = null;

    // Programmdaten
    public Filmlist filmlist; // ist die komplette Filmliste


    private ProgData() {
        filmlist = new Filmlist();
        loadFilmlist = new LoadFilmlist(this);

        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(1000), ae ->
                Listener.notify(Listener.EREIGNIS_TIMER, ProgData.class.getName())));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.setDelay(Duration.seconds(5));
        timeline.play();
    }

    public synchronized static final ProgData getInstance(String dir) {
        if (!dir.isEmpty()) {
            configDir = dir;
        }
        return getInstance();
    }

    public synchronized static final ProgData getInstance() {
        return instance == null ? instance = new ProgData() : instance;
    }

}