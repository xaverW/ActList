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

import de.mtplayer.actList.controller.ProgSave;
import de.mtplayer.actList.controller.config.Config;
import de.mtplayer.actList.controller.config.Daten;
import de.mtplayer.actList.controller.config.ProgInfos;
import de.mtplayer.actList.gui.dialog.MTAlert;
import de.mtplayer.mLib.tools.StringFormatters;
import de.mtplayer.mtp.controller.data.film.Filmlist;
import de.mtplayer.mtp.controller.filmlist.NotifyProgress;
import de.mtplayer.mtp.controller.filmlist.filmlistUrls.FilmlistUrlList;
import de.mtplayer.mtp.controller.filmlist.loadFilmlist.ImportNewFilmlist;
import de.mtplayer.mtp.controller.filmlist.loadFilmlist.ListenerFilmlistLoad;
import de.mtplayer.mtp.controller.filmlist.loadFilmlist.ListenerFilmlistLoadEvent;
import de.mtplayer.mtp.controller.filmlist.loadFilmlist.ReadFilmlist;
import de.p2tools.p2Lib.tools.log.Duration;
import de.p2tools.p2Lib.tools.log.PLog;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

public class LoadFilmlist {

    private final Daten daten;
    private final Filmlist diffList;

    private final ImportNewFilmlist importFilmlist;
    private final ReadWriteFilmlist readWriteFilmlist;

    private BooleanProperty propListSearching = new SimpleBooleanProperty(false);
    private final NotifyProgress notifyProgress = new NotifyProgress();
    private static final AtomicBoolean stop = new AtomicBoolean(false); // damit kannn das Laden gestoppt werden

    public LoadFilmlist(Daten daten) {
        this.daten = daten;
        diffList = new Filmlist();
        importFilmlist = new ImportNewFilmlist();
        importFilmlist.addAdListener(new ListenerFilmlistLoad() {
            @Override
            public synchronized void start(ListenerFilmlistLoadEvent event) {
                notifyProgress.notifyEvent(NotifyProgress.NOTIFY.START, event);
            }

            @Override
            public synchronized void progress(ListenerFilmlistLoadEvent event) {
                notifyProgress.notifyEvent(NotifyProgress.NOTIFY.PROGRESS, event);
            }

            @Override
            public synchronized void fertig(ListenerFilmlistLoadEvent event) {
                // Ergebnisliste listeFilme eintragen -> Feierabend!
                Duration.staticPing("Filme laden, ende");
                undEnde(event);
            }
        });
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
            public synchronized void fertig(ListenerFilmlistLoadEvent event) {
                // Ergebnisliste listeFilme eintragen -> Feierabend!
                Duration.staticPing("Filme laden, ende");
                undEnde(event);
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
        importFilmlist.searchFilmListUrls.updateURLsFilmlisten(true);
        importFilmlist.searchFilmListUrls.updateURLsFilmlisten(false);
    }

    public FilmlistUrlList getDownloadUrlsFilmlisten_akt() {
        return importFilmlist.searchFilmListUrls.filmlistUrlList_akt;
    }

    public FilmlistUrlList getDownloadUrlsFilmlisten_diff() {
        return importFilmlist.searchFilmListUrls.filmlistUrlList_diff;
    }

    public void readWriteFilmlist(String source, String dest, final Filmlist filmlist, int days) {
        // damit wird die Filmliste geladen UND sofort gespeichert

        Duration.staticPing("Filme laden, start");

        if (!getPropListSearching()) {
            // nicht doppelt starten
            setPropListSearching(true);

            if (!readWriteFilmlist.readWriteFilmlist(source, dest, filmlist, days)) {
                // konnte dann nicht richtig gestartet werden
                setPropListSearching(false);
            }
        }
    }

    private void undEnde(ListenerFilmlistLoadEvent event) {
        // Abos eintragen in der gesamten Liste vor Blacklist da das nur beim Ändern der Filmliste oder
        // beim Ändern von Abos gemacht wird

        PLog.sysLog("");

        // wenn nur ein Update
        if (!diffList.isEmpty()) {
            PLog.sysLog("Liste Diff gelesen am: " + StringFormatters.FORMATTER_ddMMyyyyHHmm.format(new Date()));
            PLog.sysLog("  Liste Diff erstellt am: " + diffList.genDate());
            PLog.sysLog("  Anzahl Filme: " + diffList.size());

            daten.filmlist.updateListe(diffList, true/* Vergleich über Index, sonst nur URL */, true /* ersetzen */);
            daten.filmlist.metaDaten = diffList.metaDaten;
            daten.filmlist.sort(); // jetzt sollte alles passen
            diffList.clear();
        } else {
            PLog.sysLog("Liste Kompl. gelesen am: " + StringFormatters.FORMATTER_ddMMyyyyHHmm.format(new Date()));
            PLog.sysLog("  Liste Kompl erstellt am: " + daten.filmlist.genDate());
            PLog.sysLog("  Anzahl Filme: " + daten.filmlist.size());
        }

        if (event.fehler) {
            PLog.sysLog("");
            PLog.sysLog("Filmliste laden war fehlerhaft, alte Liste wird wieder geladen");
            Platform.runLater(() -> new MTAlert().showErrorAlert("Filmliste laden", "Das Laden der Filmliste hat nicht geklappt!"));

            // dann die alte Liste wieder laden
            daten.filmlist.clear();
            setStop(false);
            new ReadFilmlist().readFilmlist(ProgInfos.getFilmlistFile(),
                    daten.filmlist, Config.SYSTEM_ANZ_TAGE_FILMLISTE.getInt());
            PLog.sysLog("");
        } else {
            new ProgSave().filmlisteSpeichern();
        }
        PLog.sysLog("");
        PLog.sysLog("Jetzige Liste erstellt am: " + daten.filmlist.genDate());
        PLog.sysLog("  Anzahl Filme: " + daten.filmlist.size());
        PLog.sysLog("");

        notifyProgress.notifyEvent(NotifyProgress.NOTIFY.PROGRESS, new ListenerFilmlistLoadEvent("", "Themen suchen",
                ListenerFilmlistLoad.PROGRESS_MAX, 0, false/* Fehler */));
        PLog.sysLog("Themen suchen");
        daten.filmlist.themenLaden();

        setPropListSearching(false);
        notifyProgress.notifyEvent(NotifyProgress.NOTIFY.FINISHED, event);
    }

}
