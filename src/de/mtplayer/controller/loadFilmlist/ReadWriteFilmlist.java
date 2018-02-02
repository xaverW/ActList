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


package de.mtplayer.controller.loadFilmlist;

import com.fasterxml.jackson.core.*;
import de.mtplayer.controller.config.Config;
import de.mtplayer.controller.config.Const;
import de.mtplayer.controller.config.Daten;
import de.mtplayer.controller.config.ProgInfos;
import de.mtplayer.controller.data.film.Film;
import de.mtplayer.controller.data.film.FilmList;
import de.mtplayer.controller.data.film.FilmListXml;
import de.mtplayer.controller.data.film.FilmXml;
import de.mtplayer.tools.*;
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

public class ReadWriteFilmlist {

    private final EventListenerList listeners = new EventListenerList();
    private double max = 0;
    private double progress = 0;
    private long milliseconds = 0;
    private String dest = "";
    private int countFoundFilms = 0;

    public void addAdListener(ListenerFilmListLoad listener) {
        listeners.add(ListenerFilmListLoad.class, listener);
    }

    public boolean readWriteFilmListe(String source, String dest, final FilmList filmList, int days) {
        this.dest = dest;

        if (dest.isEmpty()) {
            Log.sysLog("Ziel ist kein Verzeichnis!");
            return false;
        }

        File fileDestDir = new File(dest);
        File destDir = fileDestDir.getParentFile();
        if (!destDir.exists()) {
            destDir.mkdirs();
        }

        if (!destDir.isDirectory()) {
            Log.sysLog("Ziel ist kein Verzeichnis!");
            new MLAlert().showErrorAlert("Filmliste speichern", "Das Zielverzeichnis der Filmliste " +
                    "existiert nicht und lässt sich auch nicht anlegen.");
            return false;
        }


        Daten.getInstance().loadFilmList.setStop(false);
        new Thread(() -> {
            readWrite(source, filmList, days);
        }).start();
        return true;
    }

    private void readWrite(String source, final FilmList filmList, int days) {

        try {
            Log.sysLog("Liste Filme lesen von: " + source);
            filmList.clear();
            notifyStart(source, ListenerFilmListLoad.PROGRESS_MAX); // für die Progressanzeige

            countFoundFilms = 0;
            checkDays(days);

            if (source.isEmpty() || !source.startsWith("http")) {
                source = new SearchFilmListUrls().suchenAkt(new ArrayList<>());
            }
            if (source.isEmpty()) {
                return;
            }

            processFromWeb(new URL(source), filmList);

            if (Daten.getInstance().loadFilmList.getStop()) {
                Log.sysLog("--> Abbruch");
                filmList.clear();
            }
        } catch (final MalformedURLException ex) {
            ex.printStackTrace();
        }

        notifyFertig(source, filmList);
        System.out.println("--------> READFILMLIST --> fertig");
    }

    private InputStream selectDecompressor(String source, InputStream in) throws Exception {
        if (source.endsWith(Const.FORMAT_XZ)) {
            in = new XZInputStream(in);
        } else if (source.endsWith(Const.FORMAT_ZIP)) {
            final ZipInputStream zipInputStream = new ZipInputStream(in);
            zipInputStream.getNextEntry();
            in = zipInputStream;
        }
        return in;
    }

    private void readData(JsonParser jp, JsonGenerator jg, FilmList filmList) throws IOException {
        System.out.println("Start read");
        JsonToken jsonToken;
        String sender = "", thema = "";
        final Film film = new Film();
        ArrayList aListSender = new ArrayList(Arrays.asList(Config.SYSTEM_LOAD_NOT_SENDER.getStringProperty().getValue().split(",")));

        if (jp.nextToken() != JsonToken.START_OBJECT) {
            throw new IllegalStateException("Expected data to start with an Object");
        }

        while ((jsonToken = jp.nextToken()) != null) {
            if (jsonToken == JsonToken.END_OBJECT) {
                break;
            }
            if (jp.isExpectedStartArrayToken()) {
                for (int k = 0; k < FilmListXml.MAX_ELEM; ++k) {
                    filmList.metaDaten[k] = jp.nextTextValue();
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
        startWrite(jg, filmList);

        while (!Daten.getInstance().loadFilmList.getStop() && (jsonToken = jp.nextToken()) != null) {
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

                if (aListSender.isEmpty() || !aListSender.contains(film.arr[FilmXml.FILM_SENDER])) {
                    // Filme wieder schreiben
                    film.initDate();
                    if (checkDate(film)) {
                        ++countFoundFilms;
//                        if (countFoundFilms > 1000 && countFoundFilms / 1000 == 0) {
//                            System.out.println("Write: " + countFoundFilms);
//                        }
                        writeFilm(jg, film);
                    }
                }

            }
        }


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


    private void startWrite(JsonGenerator jg, FilmList filmList) throws IOException {
        Log.sysLog("Filme schreiben (" + filmList.size() + " Filme) :");
        Log.sysLog("   --> Start Schreiben nach: " + dest);
        jg.writeStartObject();
        // Infos zur Filmliste
        jg.writeArrayFieldStart(FilmListXml.FILMLISTE);
        for (int i = 0; i < FilmListXml.MAX_ELEM; ++i) {
            jg.writeString(filmList.metaDaten[i]);
        }
        jg.writeEndArray();
        // Infos der Felder in der Filmliste
        jg.writeArrayFieldStart(FilmListXml.FILMLISTE);
        for (int i = 0; i < FilmXml.JSON_NAMES.length; ++i) {
            jg.writeString(FilmXml.COLUMN_NAMES[FilmXml.JSON_NAMES[i]]);
        }
        jg.writeEndArray();
    }

    private void endWrite(JsonGenerator jg) throws IOException {
        jg.writeEndObject();
        Log.sysLog("   --> geschrieben!");

    }

    /**
     * Download a process a filmliste from the web.
     *
     * @param source   source url as string
     * @param filmList the list to read to
     */
    private void processFromWeb(URL source, FilmList filmList) {
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
//                    System.out.println("Progress " + iProgress + " " + countFoundFilms);
                    notifyProgress(source.toString(), 1.0 * iProgress / 100);
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

                            readData(jp, jg, filmList);
                            endWrite(jg);

                        }
                    }
                }
            }
        } catch (final Exception ex) {
            Log.errorLog(945123641, ex, "FilmListe: " + source);
            Platform.runLater(() -> new MLAlert().showErrorAlert("Filmliste speichern",
                    "Die Filmliste konnte nicht geladen werden: \n\n" +
                            ex.getMessage()));
            filmList.clear();
        }
    }

    private boolean checkDate(Film film) {
        // true wenn der Film angezeigt werden kann!
        try {
            if (film.datumFilm.getTime() != 0) {
                if (film.datumFilm.getTime() < milliseconds) {
                    return false;
                }
            }
        } catch (final Exception ex) {
            Log.errorLog(495623014, ex);
        }
        return true;
    }

    private void notifyStart(String url, double mmax) {
        max = mmax;
        progress = 0;
        for (final ListenerFilmListLoad l : listeners.getListeners(ListenerFilmListLoad.class)) {
            l.start(new ListenerFilmListLoadEvent(url, "", max, 0, 0, false));
        }
    }

    private void notifyProgress(String url, double iProgress) {
        progress = iProgress;
        if (progress > max) {
            progress = max;
        }
        for (final ListenerFilmListLoad l : listeners.getListeners(ListenerFilmListLoad.class)) {
            l.progress(new ListenerFilmListLoadEvent(url, "Download", max, progress, countFoundFilms, false));
        }
    }

    private void notifyFertig(String url, FilmList liste) {
        Log.sysLog("Liste Filme gelesen am: " + FastDateFormat.getInstance("dd.MM.yyyy, HH:mm").format(new Date()));
        Log.sysLog("  erstellt am: " + liste.genDate());
        Log.sysLog("  Anzahl Filme: " + liste.size());
        for (final ListenerFilmListLoad l : listeners.getListeners(ListenerFilmListLoad.class)) {
            l.fertig(new ListenerFilmListLoadEvent(url, "", max, progress, countFoundFilms, false));
        }
    }


    protected JsonGenerator getJsonGenerator(OutputStream os) throws IOException {
        JsonFactory jsonF = new JsonFactory();
        JsonGenerator jg = jsonF.createGenerator(os, JsonEncoding.UTF8);
        jg.useDefaultPrettyPrinter(); // enable indentation just to make debug/testing easier

        return jg;
    }

}
