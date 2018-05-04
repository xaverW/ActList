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

    public void addAdListener(ListenerFilmlistLoad listener) {
        listeners.add(ListenerFilmlistLoad.class, listener);
    }

    public boolean readWriteFilmlist(String source, String dest, final Filmlist filmlist, int days) {
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
            readWrite(source, filmlist, days);
        });
        th.setName("readWriteFilmlist");
        th.start();
        return true;
    }

    private void readWrite(String source, final Filmlist filmlist, int days) {
        ArrayList<String> list = new ArrayList<>();
        try {
            list.add("Liste Filme lesen von: " + source);
            filmlist.clear();

            countFoundFilms = 0;
            max = 0;

            notifyStart(source, max); // für die Progressanzeige
            checkDays(days);

            if (source.isEmpty() || !source.startsWith("http")) {
                source = new SearchFilmListUrls().searchCompleteListUrl(new ArrayList<>());
            }
            if (source.isEmpty()) {
                return;
            }

            // lesen und schreiben der Filmliste
            processFromWeb(new URL(source), filmlist);

            if (ProgData.getInstance().loadFilmlist.getStop()) {
                list.add("Filme lesen --> Abbruch");
                filmlist.clear();
            }
        } catch (final MalformedURLException ex) {
            ex.printStackTrace();
        }

        SYSTEM_OLD_FILMLIST_SIZE.setValue(max);
        SYSTEM_OLD_FILMLIST_USED.setValue(countFoundFilms);
        SYSTEM_OLD_FILMLIST_DATE.setValue(filmlist.genDate());

        notifyFertig(source, filmlist, max);
        list.add("Filme lesen --> fertig");
        PLog.sysLog(list);
    }

    private void processFromWeb(URL source, Filmlist filmlist) {
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
                        try (FileOutputStream fos = new FileOutputStream(dest);
                             JsonGenerator jg = getJsonGenerator(fos)) {

                            readData(jp, jg, filmlist);
                            endWrite(jg);

                        }
                    }
                }
            }
        } catch (final Exception ex) {
            PLog.errorLog(945123641, ex, "Filmliste: " + source);
            Platform.runLater(() -> new MLAlert().showErrorAlert("Filmliste speichern",
                    "Die Filmliste konnte nicht geladen werden: \n\n" +
                            ex.getMessage()));
            filmlist.clear();
        }
    }

    private void readData(JsonParser jp, JsonGenerator jg, Filmlist filmlist) throws IOException {
        JsonToken jsonToken;
        String sender = "", thema = "";
        final Film film = new Film();
        ArrayList aListSender = new ArrayList(Arrays.asList(ProgConfig.SYSTEM_LOAD_NOT_SENDER.getStringProperty().getValue().split(",")));

        if (jp.nextToken() != JsonToken.START_OBJECT) {
            throw new IllegalStateException("Expected data to start with an Object");
        }

        while ((jsonToken = jp.nextToken()) != null) {
            if (jsonToken == JsonToken.END_OBJECT) {
                break;
            }
            if (jp.isExpectedStartArrayToken()) {
                for (int k = 0; k < FilmlistXml.MAX_ELEM; ++k) {
                    filmlist.metaDaten[k] = jp.nextTextValue();
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
        startWrite(jg, filmlist);

        while (!ProgData.getInstance().loadFilmlist.getStop() && (jsonToken = jp.nextToken()) != null) {
            if (jsonToken == JsonToken.END_OBJECT) {
                break;
            }
            if (jp.isExpectedStartArrayToken()) {

                for (int i = 0; i < FilmXml.JSON_NAMES.length; ++i) {
                    film.arr[FilmXml.JSON_NAMES[i]] = jp.nextTextValue();
                }

                if (film.arr[FilmXml.FILM_SENDER].isEmpty()) {
                    film.arr[FilmXml.FILM_SENDER] = sender;
                } else if (!sender.equals(film.arr[FilmXml.FILM_SENDER])) {
                    // spart ein paar Byte
                    sender = film.arr[FilmXml.FILM_SENDER];
                }

                if (film.arr[FilmXml.FILM_THEMA].isEmpty()) {
                    film.arr[FilmXml.FILM_THEMA] = thema;
                } else if (!thema.equals(film.arr[FilmXml.FILM_THEMA])) {
                    thema = film.arr[FilmXml.FILM_THEMA];
                }

                ++max;
                if (aListSender.isEmpty() || !aListSender.contains(film.arr[FilmXml.FILM_SENDER])) {
                    // Filme wieder schreiben
                    film.initDate();
                    if (checkDate(film)) {
                        ++countFoundFilms;
                        writeFilm(jg, film);
                    }
                }

            }
        }


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


    private void startWrite(JsonGenerator jg, Filmlist filmlist) throws IOException {
        PLog.sysLog("Filme schreiben (" + filmlist.size() + " Filme) :");
        PLog.sysLog("   --> Start Schreiben nach: " + dest);
        jg.writeStartObject();
        // Infos zur Filmliste
        jg.writeArrayFieldStart(FilmlistXml.FILMLISTE);
        for (int i = 0; i < FilmlistXml.MAX_ELEM; ++i) {
            jg.writeString(filmlist.metaDaten[i]);
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

    private void notifyFertig(String url, Filmlist liste, int max) {
        ArrayList<String> list = new ArrayList<>();
        list.add(PLog.LILNE3);
        list.add("Liste Filme gelesen am: " + FastDateFormat.getInstance("dd.MM.yyyy, HH:mm").format(new Date()));
        list.add("  erstellt am: " + liste.genDate());
        list.add("  Anzahl Filme: " + liste.size());
        for (final ListenerFilmlistLoad l : listeners.getListeners(ListenerFilmlistLoad.class)) {
            l.fertig(new ListenerFilmlistLoadEvent(url, "", max, progress, countFoundFilms, false));
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
