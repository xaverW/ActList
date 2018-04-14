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
import de.mtplayer.actList.controller.data.film.Filmlist;
import de.mtplayer.actList.gui.dialog.MTAlert;
import de.mtplayer.mLib.tools.StringFormatters;
import de.p2tools.p2Lib.tools.log.Duration;
import de.p2tools.p2Lib.tools.log.PLog;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import javax.swing.event.EventListenerList;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

public class LoadFilmlist {

    //    private final HashSet<String> hashSet = new HashSet<>();
    private final Filmlist diffListe;

    // private
    private final Daten daten;
    private final ImportFilmlist importFilmliste;
    private final ReadWriteFilmlist readWriteFilmlist;
    private final EventListenerList listeners = new EventListenerList();
    private BooleanProperty propListSearching = new SimpleBooleanProperty(false);
    private boolean onlyOne = false;

    private static final AtomicBoolean stop = new AtomicBoolean(false); // damit kannn das Laden
    // gestoppt werden

    public LoadFilmlist(Daten daten) {
        this.daten = daten;
        diffListe = new Filmlist();
        importFilmliste = new ImportFilmlist();
        importFilmliste.addAdListener(new ListenerFilmlistLoad() {
            @Override
            public synchronized void start(ListenerFilmlistLoadEvent event) {
                notifyStart(event);
            }

            @Override
            public synchronized void progress(ListenerFilmlistLoadEvent event) {
                notifyProgress(event);
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
                notifyStart(event);
            }

            @Override
            public synchronized void progress(ListenerFilmlistLoadEvent event) {
                notifyProgress(event);
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

//    public void loadFilmlist(String dateiUrl) {
//        loadFilmlist(dateiUrl, false);
//    }

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

    public void afterFilmlistLoad() {
        notifyProgress(new ListenerFilmlistLoadEvent("", "Themen suchen",
                (int) ListenerFilmlistLoad.PROGRESS_MAX, ListenerFilmlistLoad.PROGRESS_MAX, 0, false/* Fehler */));
        PLog.sysLog("Themen suchen");
        daten.filmlist.themenLaden();
    }

    // #######################################
    // #######################################
    public void addAdListener(ListenerFilmlistLoad listener) {
        listeners.add(ListenerFilmlistLoad.class, listener);
    }

    public synchronized void setStop(boolean set) {
        stop.set(set);
    }

    public synchronized boolean getStop() {
        return stop.get();
    }

    public void updateDownloadUrlsFilmlisten() {
        importFilmliste.searchFilmlistUrls.updateURLsFilmlisten(true);
        importFilmliste.searchFilmlistUrls.updateURLsFilmlisten(false);
    }

    public FilmlistUrlList getDownloadUrlsFilmlisten_akt() {
        return importFilmliste.searchFilmlistUrls.filmlistUrlList_akt;
    }

    public FilmlistUrlList getDownloadUrlsFilmlisten_diff() {
        return importFilmliste.searchFilmlistUrls.filmlistUrlList_diff;
    }

    // #######################################
    // #######################################
    private void undEnde(ListenerFilmlistLoadEvent event) {
        // Abos eintragen in der gesamten Liste vor Blacklist da das nur beim Ändern der Filmliste oder
        // beim Ändern von Abos gemacht wird

        PLog.sysLog("");

        // wenn nur ein Update
        if (!diffListe.isEmpty()) {
            PLog.sysLog("Liste Diff gelesen am: " + StringFormatters.FORMATTER_ddMMyyyyHHmm.format(new Date()));
            PLog.sysLog("  Liste Diff erstellt am: " + diffListe.genDate());
            PLog.sysLog("  Anzahl Filme: " + diffListe.size());

            daten.filmlist.updateListe(diffListe, true/* Vergleich über Index, sonst nur URL */, true /* ersetzen */);
            daten.filmlist.metaDaten = diffListe.metaDaten;
            daten.filmlist.sort(); // jetzt sollte alles passen
            diffListe.clear();
        } else {
            PLog.sysLog("Liste Kompl. gelesen am: " + StringFormatters.FORMATTER_ddMMyyyyHHmm.format(new Date()));
            PLog.sysLog("  Liste Kompl erstellt am: " + daten.filmlist.genDate());
            PLog.sysLog("  Anzahl Filme: " + daten.filmlist.size());
        }

        if (event.isFehler()) {
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

        afterFilmlistLoad();

        setPropListSearching(false);
        notifyFertig(event);
    }

//    private void fillHash(Filmlist filmlist) {
//        hashSet.addAll(filmlist.stream().map(Film::getUrlHistory).collect(Collectors.toList()));
//    }

    public void notifyStart(ListenerFilmlistLoadEvent event) {
        final ListenerFilmlistLoadEvent e = event;
        try {
            Platform.runLater(() -> {
                for (final ListenerFilmlistLoad l : listeners.getListeners(ListenerFilmlistLoad.class)) {
                    l.start(e);
                }

            });
        } catch (final Exception ex) {
            PLog.errorLog(765213654, ex);
        }
    }

    public void notifyProgress(ListenerFilmlistLoadEvent event) {
        try {
            Platform.runLater(() -> {
                for (final ListenerFilmlistLoad l : listeners.getListeners(ListenerFilmlistLoad.class)) {
                    l.progress(event);
                }

            });
        } catch (final Exception ex) {
            PLog.errorLog(201020369, ex);
        }
    }

    public void notifyFertig(ListenerFilmlistLoadEvent event) {
        try {
            Platform.runLater(() -> {
                for (final ListenerFilmlistLoad l : listeners.getListeners(ListenerFilmlistLoad.class)) {
                    l.fertig(event);
                }
                for (final ListenerFilmlistLoad l : listeners.getListeners(ListenerFilmlistLoad.class)) {
                    if (!onlyOne) {
                        l.fertigOnlyOne(event);
                    }
                }
                onlyOne = true;
            });

        } catch (final Exception ex) {
            PLog.errorLog(912045120, ex);
        }
    }

    public BooleanProperty getIstAmLaufen() {
        return propListSearching;
    }

    public void setIstAmLaufen(BooleanProperty istAmLaufen) {
        this.propListSearching = istAmLaufen;
    }
}
