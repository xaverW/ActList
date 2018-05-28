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

package de.mtplayer.actList.controller.loadFilmlist;

import de.mtplayer.actList.controller.config.ProgData;
import de.mtplayer.mtp.controller.filmlist.NotifyProgress;
import de.mtplayer.mtp.controller.filmlist.filmlistUrls.FilmlistUrlList;
import de.mtplayer.mtp.controller.filmlist.filmlistUrls.SearchFilmListUrls;
import de.mtplayer.mtp.controller.filmlist.loadFilmlist.ListenerFilmlistLoad;
import de.mtplayer.mtp.controller.filmlist.loadFilmlist.ListenerFilmlistLoadEvent;
import de.mtplayer.mtp.gui.dialog.MTAlert;
import de.p2tools.p2Lib.tools.log.Duration;
import de.p2tools.p2Lib.tools.log.PLog;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.util.concurrent.atomic.AtomicBoolean;

public class LoadFilmlist {

    private final ProgData progData;

    private final SearchFilmListUrls searchFilmListUrls;
    private final ReadWriteFilmlist readWriteFilmlist;

    private BooleanProperty propListSearching = new SimpleBooleanProperty(false);
    private final NotifyProgress notifyProgress = new NotifyProgress();
    private static final AtomicBoolean stop = new AtomicBoolean(false); // damit kannn das Laden gestoppt werden

    public LoadFilmlist(ProgData progData) {
        this.progData = progData;
        searchFilmListUrls = new SearchFilmListUrls();

        readWriteFilmlist = new ReadWriteFilmlist();
        readWriteFilmlist.addAdListener(new ListenerFilmlistLoad() {
            @Override
            public synchronized void start(ListenerFilmlistLoadEvent event) {
                notifyProgress.notifyEvent(NotifyProgress.NOTIFY.START, event);
            }

            @Override
            public synchronized void progress(ListenerFilmlistLoadEvent event) {
                notifyProgress.notifyEvent(NotifyProgress.NOTIFY.PROGRESS, event);
            }

            @Override
            public synchronized void finished(ListenerFilmlistLoadEvent event) {
                // Ergebnisliste listeFilme eintragen -> Feierabend!
                Duration.staticPing("Filme laden, ende");
                andEnd(event);
            }
        });
    }

    public boolean getPropListSearching() {
        return propListSearching.get();
    }

    public BooleanProperty propListSearchingProperty() {
        return propListSearching;
    }

    public void setPropListSearching(boolean propListSearching) {
        this.propListSearching.set(propListSearching);
    }

    public void addAdListener(ListenerFilmlistLoad listener) {
        notifyProgress.listeners.add(ListenerFilmlistLoad.class, listener);
    }

    public synchronized void setStop(boolean set) {
        stop.set(set);
    }

    public synchronized boolean getStop() {
        return stop.get();
    }

    public void updateDownloadUrlsFilmlisten() {
        searchFilmListUrls.updateURLsFilmlists(true);
        searchFilmListUrls.updateURLsFilmlists(false);
    }

    public FilmlistUrlList getDownloadUrlsFilmlisten_akt() {
        return searchFilmListUrls.filmlistUrlList_akt;
    }

    public FilmlistUrlList getDownloadUrlsFilmlisten_diff() {
        return searchFilmListUrls.filmlistUrlList_diff;
    }

    public void readWriteFilmlist(String source, String dest, int days) {
        // damit wird die Filmliste geladen UND sofort gespeichert

        Duration.staticPing("Filme laden, start");

        if (!getPropListSearching()) {
            // nicht doppelt starten
            setPropListSearching(true);

            if (!readWriteFilmlist.readWriteFilmlist(source, dest, days)) {
                // konnte dann nicht richtig gestartet werden
                setPropListSearching(false);
            }
        }
    }

    private void andEnd(ListenerFilmlistLoadEvent event) {
        if (event.error) {
            PLog.sysLog("");
            PLog.sysLog("Filmliste laden war fehlerhaft, alte Liste wird wieder geladen");
            Platform.runLater(() -> new MTAlert().showErrorAlert("Filmliste laden", "Das Laden der Filmliste hat nicht geklappt!"));
        }

        setPropListSearching(false);
        notifyProgress.notifyEvent(NotifyProgress.NOTIFY.FINISHED, event);
    }

}
