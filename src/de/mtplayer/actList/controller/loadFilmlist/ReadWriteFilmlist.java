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

import com.fasterxml.jackson.core.*;
import com.sun.xml.internal.fastinfoset.util.StringArray;
import de.mtplayer.actList.controller.config.ProgConfig;
import de.mtplayer.actList.controller.config.ProgConst;
import de.mtplayer.actList.controller.config.ProgData;
import de.mtplayer.actList.controller.config.ProgInfos;
import de.mtplayer.mLib.tools.InputStreamProgressMonitor;
import de.mtplayer.mLib.tools.MLAlert;
import de.mtplayer.mLib.tools.MLHttpClient;
import de.mtplayer.mLib.tools.ProgressMonitorInputStream;
import de.mtplayer.mtp.controller.data.film.Film;
import de.mtplayer.mtp.controller.data.film.FilmXml;
import de.mtplayer.mtp.controller.data.film.Filmlist;
import de.mtplayer.mtp.controller.data.film.FilmlistXml;
import de.mtplayer.mtp.controller.filmlist.filmlistUrls.SearchFilmListUrls;
import de.mtplayer.mtp.controller.filmlist.loadFilmlist.ListenerFilmlistLoad;
import de.mtplayer.mtp.controller.filmlist.loadFilmlist.ListenerFilmlistLoadEvent;
import de.p2tools.p2Lib.dialog.PAlert;
import de.p2tools.p2Lib.tools.log.PLog;
import javafx.application.Platform;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.commons.lang3.time.FastDateFormat;
import org.tukaani.xz.XZInputStream;

import javax.swing.event.EventListenerList;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipInputStream;

import static de.mtplayer.actList.controller.config.ProgConfig.*;

public class ReadWriteFilmlist {

    private final EventListenerList listeners = new EventListenerList();
    private double progress = 0;
    private long milliseconds = 0;
    private String dest = "";
    private int max = 0;
    private int countFoundFilms = 0;
    private String genDateLocalTime = "";
    private boolean wait = false;
    private PAlert.BUTTON ret;

    public void addAdListener(ListenerFilmlistLoad listener) {
        listeners.add(ListenerFilmlistLoad.class, listener);
    }

    public boolean readWriteFilmlist(String source, String dest, int days) {
        this.dest = dest;

        if (dest.isEmpty()) {
            PLog.sysLog("Ziel ist kein Verzeichnis!");
            return false;
        }

        File fileDestDir = new File(dest);
        File destDir = fileDestDir.getParentFile();
        if (!destDir.exists()) {
            destDir.mkdirs();
        }

        if (!destDir.isDirectory()) {
            PLog.sysLog("Ziel ist kein Verzeichnis!");
            new MLAlert().showErrorAlert("Filmliste speichern", "Das Zielverzeichnis der Filmliste " +
                    "existiert nicht und lässt sich auch nicht anlegen.");
            return false;
        }

        ProgData.getInstance().loadFilmlist.setStop(false);
        Thread th = new Thread(() -> {
            readWrite(source, days);
        });
        th.setName("readWriteFilmlist");
        th.start();
        return true;
    }

    private void readWrite(String source, int days) {
        ArrayList<String> list = new ArrayList<>();
        countFoundFilms = 0;
        max = 0;
        notifyStart(source, max); // für die Progressanzeige
        list.add("Liste Filme lesen von: " + source);

        try {
            checkDays(days);
            if (source.isEmpty() || !source.startsWith("http")) {
                source = new SearchFilmListUrls().searchCompleteListUrl(new ArrayList<>());
            }
            if (source.isEmpty()) {
                return;
            }

            if (startLoadProcess(source)) {
                // dann wurde geladen
                SYSTEM_FILMLIST_SIZE.setValue(max);
                SYSTEM_FILMLIST_USED.setValue(countFoundFilms);
                SYSTEM_FILMLIST_DATE_LOCAL_TIME.setValue(genDateLocalTime);
            }

            if (ProgData.getInstance().loadFilmlist.getStop()) {
                list.add("Filme lesen --> Abbruch");
            }
        } catch (final MalformedURLException ex) {
            ex.printStackTrace();
        }

        notifyFertig(source, max);
        list.add("Filme lesen --> fertig");
        PLog.sysLog(list);
    }

    private boolean startLoadProcess(String source) throws MalformedURLException {
        // lesen und schreiben der Filmliste
        if (processFromWeb(new URL(source), false)) {
            // alles OK
            return true;
        }

        // dann gabs noch keine aktuelle Filmliste
        wait = true;
        Platform.runLater(() -> {
                    ret = PAlert.showAlert_yes_no("Filme laden", "Filmliste ist noch aktuell",
                            "Es gibt noch keine aktuellere Filmliste, " +
                                    "soll trotzdem eine neue " +
                                    "Filmliste geladen werden?");
                    wait = false;
                }
        );

        while (wait) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }

        if (ret.equals(PAlert.BUTTON.YES)) {
            processFromWeb(new URL(source), true);
            return true;
        }

        return false;
    }

    private boolean processFromWeb(URL source, boolean loadActList) {
        boolean ret = true;
        final Request.Builder builder = new Request.Builder().url(source);
        builder.addHeader("User-Agent", ProgInfos.getUserAgent());

        // our progress monitor callback
        final InputStreamProgressMonitor monitor = new InputStreamProgressMonitor() {
            private int oldProgress = 0;

            @Override
            public void progress(long bytesRead, long size) {
                final int iProgress = (int) (bytesRead * 100/* zum Runden */ / size);
                if (iProgress != oldProgress) {
                    oldProgress = iProgress;
                    notifyProgress(source.toString(), 1.0 * iProgress / 100, max);
                }
            }
        };

        try (Response response = MLHttpClient.getInstance().getHttpClient().newCall(builder.build()).execute();
             ResponseBody body = response.body()) {
            if (response.isSuccessful()) {

                try (InputStream input = new ProgressMonitorInputStream(body.byteStream(), body.contentLength(), monitor)) {

                    try (InputStream is = selectDecompressor(source.toString(), input);
                         JsonParser jp = new JsonFactory().createParser(is)) {

                        ret = startReadingData(jp, loadActList);

                    }
                }
            }
        } catch (final Exception ex) {
            PLog.errorLog(945123641, ex, "Filmliste: " + source);
            Platform.runLater(() -> new MLAlert().showErrorAlert("Filmliste speichern",
                    "Die Filmliste konnte nicht geladen werden: \n\n" +
                            ex.getMessage()));
        }

        return ret;
    }

    private boolean startReadingData(JsonParser jp, boolean loadActList) throws IOException {
        JsonToken jsonToken;
        StringArray metaDaten = new StringArray();

        if (jp.nextToken() != JsonToken.START_OBJECT) {
            throw new IllegalStateException("Expected data to start with an Object");
        }

        while ((jsonToken = jp.nextToken()) != null) {
            if (jsonToken == JsonToken.END_OBJECT) {
                break;
            }
            if (jp.isExpectedStartArrayToken()) {
                for (int k = 0; k < FilmlistXml.MAX_ELEM; ++k) {
                    metaDaten.add(jp.nextTextValue());
                }
                break;
            }
        }

        while ((jsonToken = jp.nextToken()) != null) {
            if (jsonToken == JsonToken.END_OBJECT) {
                break;
            }
            if (jp.isExpectedStartArrayToken()) {
                // sind nur die Feldbeschreibungen, brauch mer nicht
                jp.nextToken();
                break;
            }
        }


        // jetzt ist das Datum der Filmliste gesetzt und kann geschrieben werden
        genDateLocalTime = Filmlist.genDate(metaDaten.getArray());
        if (!loadActList && !checkDate()) {
            return false;
        }


        // Filme lesen und schreiben
        try (FileOutputStream fos = new FileOutputStream(dest);
             JsonGenerator jg = getJsonGenerator(fos)) {
            if (readingData(jp, jg, metaDaten)) {
                // nur wenn geschrieben wird
                endWrite(jg);
            }
        }

        return true;
    }

    private boolean checkDate() {
        if (SYSTEM_FILMLIST_DATE_LOCAL_TIME.get().equals(genDateLocalTime)) {
            // dann gibts nur die gleiche Liste
            PLog.sysLog("Gibt noch keine aktuellere Filmliste: " + genDateLocalTime);
            return false;
        }

        return true;
    }

    private boolean readingData(JsonParser jp, JsonGenerator jg, StringArray metaDaten) throws IOException {
        JsonToken jsonToken;
        String sender = "", theme = "";
        final Film film = new Film();
        ArrayList listChannel = new ArrayList(Arrays.asList(ProgConfig.SYSTEM_LOAD_NOT_SENDER.getStringProperty().getValue().split(",")));

        startWrite(jg, metaDaten);

        while (!ProgData.getInstance().loadFilmlist.getStop() && (jsonToken = jp.nextToken()) != null) {
            if (jsonToken == JsonToken.END_OBJECT) {
                break;
            }
            if (jp.isExpectedStartArrayToken()) {

                for (int i = 0; i < FilmXml.JSON_NAMES.length; ++i) {
                    film.arr[FilmXml.JSON_NAMES[i]] = jp.nextTextValue();
                }

                if (film.arr[FilmXml.FILM_CHANNEL].isEmpty()) {
                    film.arr[FilmXml.FILM_CHANNEL] = sender;
                } else if (!sender.equals(film.arr[FilmXml.FILM_CHANNEL])) {
                    sender = film.arr[FilmXml.FILM_CHANNEL];
                }

                if (film.arr[FilmXml.FILM_THEME].isEmpty()) {
                    film.arr[FilmXml.FILM_THEME] = theme;
                } else if (!theme.equals(film.arr[FilmXml.FILM_THEME])) {
                    theme = film.arr[FilmXml.FILM_THEME];
                }

                ++max;
                if (listChannel.isEmpty() || !listChannel.contains(film.arr[FilmXml.FILM_CHANNEL])) {
                    // Filme wieder schreiben
                    film.initDate();
                    if (checkDate(film)) {
                        ++countFoundFilms;
                        writeFilm(jg, film);
                    }
                }

            }
        }

        return true;
    }

    private InputStream selectDecompressor(String source, InputStream in) throws Exception {
        if (source.endsWith(ProgConst.FORMAT_XZ)) {
            in = new XZInputStream(in);
        } else if (source.endsWith(ProgConst.FORMAT_ZIP)) {
            final ZipInputStream zipInputStream = new ZipInputStream(in);
            zipInputStream.getNextEntry();
            in = zipInputStream;
        }
        return in;
    }

    private void checkDays(long days) {
        if (days > 0) {
            milliseconds = System.currentTimeMillis() - TimeUnit.MILLISECONDS.convert(days, TimeUnit.DAYS);
        } else {
            milliseconds = 0;
        }
    }

    private void writeFilm(JsonGenerator jg, Film film) throws IOException {
        jg.writeArrayFieldStart(FilmXml.TAG_JSON_LIST);
        for (int i = 0; i < FilmXml.JSON_NAMES.length; ++i) {
            int m = FilmXml.JSON_NAMES[i];
            jg.writeString(film.arr[m]);
        }
        jg.writeEndArray();
    }


    private void startWrite(JsonGenerator jg, StringArray metaDaten) throws IOException {
        PLog.sysLog("Filmeliste laden und schreiben");
        PLog.sysLog("   --> Schreiben nach: " + dest);
        jg.writeStartObject();
        // Infos zur Filmliste
        jg.writeArrayFieldStart(FilmlistXml.FILMLISTE);
        for (int i = 0; i < metaDaten.getSize(); ++i) {
            jg.writeString(metaDaten.get(i));
        }
        jg.writeEndArray();
        // Infos der Felder in der Filmliste
        jg.writeArrayFieldStart(FilmlistXml.FILMLISTE);
        for (int i = 0; i < FilmXml.JSON_NAMES.length; ++i) {
            jg.writeString(FilmXml.COLUMN_NAMES[FilmXml.JSON_NAMES[i]]);
        }
        jg.writeEndArray();
    }

    private void endWrite(JsonGenerator jg) throws IOException {
        jg.writeEndObject();
        PLog.sysLog("   --> geschrieben!");

    }

    private boolean checkDate(Film film) {
        // true wenn der Film angezeigt werden kann!
        try {
            if (film.filmDate.getTime() != 0) {
                if (film.filmDate.getTime() < milliseconds) {
                    return false;
                }
            }
        } catch (final Exception ex) {
            PLog.errorLog(495623014, ex);
        }
        return true;
    }

    private void notifyStart(String url, int max) {
        progress = 0;
        for (final ListenerFilmlistLoad l : listeners.getListeners(ListenerFilmlistLoad.class)) {
            l.start(new ListenerFilmlistLoadEvent(url, "", max, 0, 0, false));
        }
    }

    private void notifyProgress(String url, double iProgress, int max) {
        progress = iProgress;
        if (progress > ListenerFilmlistLoad.PROGRESS_MAX) {
            progress = ListenerFilmlistLoad.PROGRESS_MAX;
        }
        for (final ListenerFilmlistLoad l : listeners.getListeners(ListenerFilmlistLoad.class)) {
            l.progress(new ListenerFilmlistLoadEvent(url, "Download", max, progress, countFoundFilms, false));
        }
    }

    private void notifyFertig(String url, int max) {
        ArrayList<String> list = new ArrayList<>();
        list.add(PLog.LILNE3);
        list.add("Liste Filme gelesen am  : " + FastDateFormat.getInstance("dd.MM.yyyy, HH:mm").format(new Date()));
        list.add("  erstellt am           : " + genDateLocalTime);
        list.add("  Anzahl Filme gesamt   : " + max);
        list.add("  Anzahl Filme gefunden : " + countFoundFilms);
        for (final ListenerFilmlistLoad l : listeners.getListeners(ListenerFilmlistLoad.class)) {
            l.finished(new ListenerFilmlistLoadEvent(url, "", max, progress, countFoundFilms, false));
        }
        list.add(PLog.LILNE3);
        PLog.sysLog(list);
    }

    protected JsonGenerator getJsonGenerator(OutputStream os) throws IOException {
        JsonFactory jsonF = new JsonFactory();
        JsonGenerator jg = jsonF.createGenerator(os, JsonEncoding.UTF8);
        jg.useDefaultPrettyPrinter(); // enable indentation just to make debug/testing easier

        return jg;
    }

}
