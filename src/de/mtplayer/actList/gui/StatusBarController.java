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
import de.mtplayer.actList.gui.tools.Listener;
import de.mtplayer.mtp.controller.filmlist.loadFilmlist.ListenerFilmlistLoad;
import de.mtplayer.mtp.controller.filmlist.loadFilmlist.ListenerFilmlistLoadEvent;
import de.p2tools.p2Lib.tools.PDate;
import de.p2tools.p2Lib.tools.log.PLog;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.SimpleTimeZone;

public class StatusBarController extends AnchorPane {

    StackPane stackPane = new StackPane();

    // loadPane
    Label lblLeftLoad = new Label("");
    Label lblProgress = new Label();
    ProgressBar progress = new ProgressBar();
    Button btnStop = new Button("");

    //nonePane
    Label lblLeft = new Label();
    Label lblRight = new Label();

    AnchorPane loadPane = new AnchorPane();
    AnchorPane nonePane = new AnchorPane();


    private boolean loadList = false;

    private final ProgData progData;
    private boolean stopTimer = false;

    private final static String DATUM_ZEIT_FORMAT = "dd.MM.yyyy, HH:mm";
    final SimpleDateFormat sdf = new SimpleDateFormat(DATUM_ZEIT_FORMAT);

    private PDate filmlistDate = new PDate();
    private int foundFilms = -1;
    private int maxFilms = -1;


    public StatusBarController(ProgData progData) {
        this.progData = progData;

        getChildren().addAll(stackPane);
        AnchorPane.setLeftAnchor(stackPane, 0.0);
        AnchorPane.setBottomAnchor(stackPane, 0.0);
        AnchorPane.setRightAnchor(stackPane, 0.0);
        AnchorPane.setTopAnchor(stackPane, 0.0);


        HBox hBox = getHbox();
        lblLeft.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(lblLeft, Priority.ALWAYS);
        hBox.getChildren().addAll(lblLeft, lblRight);
        nonePane.getChildren().add(hBox);
        nonePane.setStyle("-fx-background-color: -fx-background ;");

        hBox = getHbox();
        lblLeftLoad.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(lblLeftLoad, Priority.ALWAYS);
        btnStop.setGraphic(new Icons().ICON_BUTTON_STOP);
        hBox.getChildren().addAll(lblLeftLoad, lblProgress, progress, btnStop);
        progress.setPrefWidth(200);
        loadPane.getChildren().add(hBox);
        loadPane.setStyle("-fx-background-color: -fx-background ;");

        make();
    }

    private HBox getHbox() {
        HBox hBox = new HBox();
        hBox.setPadding(new Insets(2, 5, 2, 5));
        hBox.setSpacing(10);
        hBox.setAlignment(Pos.CENTER_RIGHT);
        AnchorPane.setLeftAnchor(hBox, 0.0);
        AnchorPane.setBottomAnchor(hBox, 0.0);
        AnchorPane.setRightAnchor(hBox, 0.0);
        AnchorPane.setTopAnchor(hBox, 0.0);
        return hBox;
    }


    private void make() {
        stackPane.getChildren().addAll(nonePane, loadPane);
        nonePane.toFront();

        sdf.setTimeZone(new SimpleTimeZone(SimpleTimeZone.UTC_TIME, "UTC"));
        setGenTimeFilmlist();

        progData.loadFilmlist.addAdListener(new ListenerFilmlistLoad() {
            @Override
            public void start(ListenerFilmlistLoadEvent event) {
                loadList = true;
                foundFilms = -1;
                maxFilms = -1;
                setStatusbar();
            }

            @Override
            public void progress(ListenerFilmlistLoadEvent event) {
                foundFilms = event.count;
                maxFilms = (int) event.max;
                updateProgressBar(event);
            }

            @Override
            public void fertig(ListenerFilmlistLoadEvent event) {
                stopTimer = false;
                loadList = false;
                foundFilms = event.count;
                maxFilms = (int) event.max;
                setGenTimeFilmlist();
                setStatusbar();
            }
        });
        Listener.addListener(new Listener(Listener.EREIGNIS_TIMER, StatusBarController.class.getSimpleName()) {
            @Override
            public void ping() {
                try {
                    if (!stopTimer) {
                        setStatusbar();
                    }
                } catch (final Exception ex) {
                    PLog.errorLog(936251087, ex);
                }
            }
        });
        btnStop.setOnAction(a -> progData.loadFilmlist.setStop(true));
    }

    private void setGenTimeFilmlist() {
        foundFilms = ProgConfig.SYSTEM_OLD_FILMLIST_USED.getInt();
        maxFilms = ProgConfig.SYSTEM_OLD_FILMLIST_SIZE.getInt();

        try {
            final String filmDateStr = ProgConfig.SYSTEM_OLD_FILMLIST_DATE.get();
            filmlistDate = new PDate(sdf.parse(filmDateStr).getTime());
        } catch (Exception ex) {
            filmlistDate = new PDate();
        }
    }

    private void setStatusbar() {
        if (loadList) {
            loadPane.toFront();
            return;
        }

        nonePane.toFront();
        setTextLeft();
        setTextRight();
    }

    private void updateProgressBar(ListenerFilmlistLoadEvent event) {
        stopTimer = true;
        progress.setProgress(event.progress);
        lblProgress.setText(event.text);

        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.GERMANY);
        String count = numberFormat.format(event.count);
        String max = numberFormat.format((int) event.max);

        if (event.count >= 0) {
            if (event.count != (int) event.max) {
                lblLeftLoad.setText("Filme gefunden: " + count + " von insgesamt: " + max);
            } else {
                lblLeftLoad.setText("Filme gefunden: " + count);
            }
        }
    }


    private void setTextLeft() {
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.GERMANY);
        String found = numberFormat.format(foundFilms);
        String max = numberFormat.format(maxFilms);

        if (foundFilms >= 0 && maxFilms == foundFilms) {
            lblLeft.setText("Anzahl Filme: " + found);

        } else if (foundFilms >= 0) {
            lblLeft.setText("Anzahl Filme: " + found + " von insgesamt: " + max);
        }
    }

    private void setTextRight() {
        if (filmlistDate.getTime() == 0) {
            lblRight.setText("");
            return;
        }

        int filmlistAge = filmlistDate.diffInSeconds();

        String strText = "Filmliste erstellt: ";
        strText += filmlistDate.toString();
        strText += " Uhr  ||  Alter: ";

        final int minuten = filmlistAge / 60;
        String strSekunde = String.valueOf(filmlistAge % 60);
        String strMinute = String.valueOf(minuten % 60);
        String strStunde = String.valueOf(minuten / 60);
        if (strSekunde.length() < 2) {
            strSekunde = '0' + strSekunde;
        }
        if (strMinute.length() < 2) {
            strMinute = '0' + strMinute;
        }
        if (strStunde.length() < 2) {
            strStunde = '0' + strStunde;
        }
        strText += strStunde + ':' + strMinute + ':' + strSekunde + ' ';

        // Infopanel setzen
        lblRight.setText(strText);
    }


}
