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


package de.mtplayer.actList.controller;

import de.mtplayer.actList.controller.config.ProgConfig;
import de.mtplayer.actList.controller.config.ProgData;
import de.mtplayer.mLib.tools.MLConfigs;
import de.mtplayer.mtp.controller.filmlist.filmlistUrls.FilmlistUrlData;
import de.p2tools.p2Lib.tools.log.Duration;
import de.p2tools.p2Lib.tools.log.PLog;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class IoReadXml implements AutoCloseable {

    private XMLInputFactory inFactory = null;
    private ProgData progData = null;

    public IoReadXml(ProgData progData) {
        this.progData = progData;

        inFactory = XMLInputFactory.newInstance();
        inFactory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.FALSE);
    }

    public boolean readConfiguration(Path xmlFilePath) {
        Duration.counterStart("Konfig lesen");
        boolean ret = false;

        if (Files.exists(xmlFilePath)) {
            XMLStreamReader parser = null;
            try (InputStream is = Files.newInputStream(xmlFilePath);
                 InputStreamReader in = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                parser = inFactory.createXMLStreamReader(in);
                while (parser.hasNext()) {
                    final int event = parser.next();
                    if (event == XMLStreamConstants.START_ELEMENT) {
                        switch (parser.getLocalName()) {
                            case ProgConfig.SYSTEM:
                                // System
                                getConfig(parser, ProgConfig.SYSTEM);
                                break;
                            case FilmlistUrlData.FILMLIST_UPDATE_SERVER:
                                // Urls Filmlisten
                                final FilmlistUrlData filmlistUrlData = new FilmlistUrlData();
                                if (get(parser,
                                        FilmlistUrlData.FILMLIST_UPDATE_SERVER,
                                        FilmlistUrlData.FILMLIST_UPDATE_SERVER_COLUMN_NAMES,
                                        filmlistUrlData.arr)) {
                                    switch (filmlistUrlData.arr[FilmlistUrlData.FILMLIST_UPDATE_SERVER_ART_NR]) {
                                        case FilmlistUrlData.SERVER_ART_AKT:
                                            progData.loadFilmlist.getDownloadUrlsFilmlisten_akt().addWithCheck(filmlistUrlData);
                                            break;
                                        case FilmlistUrlData.SERVER_ART_DIFF:
                                            progData.loadFilmlist.getDownloadUrlsFilmlisten_diff().addWithCheck(filmlistUrlData);
                                            break;
                                    }
                                }
                                break;
                        }
                    }
                }
                ret = true;
            } catch (final Exception ex) {
                ret = false;
                PLog.errorLog(392840096, ex);
            } finally {
                try {
                    if (parser != null) {
                        parser.close();
                    }
                } catch (final Exception ignored) {
                }
            }
            progData.loadFilmlist.getDownloadUrlsFilmlisten_akt().sort();
            progData.loadFilmlist.getDownloadUrlsFilmlisten_diff().sort();
            ProgConfig.loadSystemParameter();
        }

        Duration.counterStop("Konfig lesen");
        return ret;
    }


    private boolean get(XMLStreamReader parser, String xmlElem, String[] xmlNames, String[] strRet) {
        boolean ret = true;
        final int maxElem = strRet.length;
        for (int i = 0; i < maxElem; ++i) {
            if (strRet[i] == null) {
                // damit Vorgaben nicht verschwinden!
                strRet[i] = "";
            }
        }
        try {
            while (parser.hasNext()) {
                final int event = parser.next();
                if (event == XMLStreamConstants.END_ELEMENT) {
                    if (parser.getLocalName().equals(xmlElem)) {
                        break;
                    }
                }
                if (event == XMLStreamConstants.START_ELEMENT) {
                    for (int i = 0; i < maxElem; ++i) {
                        if (parser.getLocalName().equals(xmlNames[i])) {
                            strRet[i] = parser.getElementText();
                            break;
                        }
                    }
                }
            }
        } catch (final Exception ex) {
            ret = false;
            PLog.errorLog(739530149, ex);
        }
        return ret;
    }

    private boolean getConfig(XMLStreamReader parser, String xmlElem) {
        boolean ret = true;
        try {
            while (parser.hasNext()) {
                final int event = parser.next();
                if (event == XMLStreamConstants.END_ELEMENT) {
                    if (parser.getLocalName().equals(xmlElem)) {
                        break;
                    }
                }
                if (event == XMLStreamConstants.START_ELEMENT) {
                    final String s = parser.getLocalName();
                    final String n = parser.getElementText();
                    MLConfigs mlConfigs = ProgConfig.get(s);
                    if (mlConfigs != null) {
                        mlConfigs.setValue(n);
                    }
                }
            }
        } catch (final Exception ex) {
            ret = false;
            PLog.errorLog(945120369, ex);
        }
        return ret;
    }

    @Override
    public void close() throws Exception {

    }

}
