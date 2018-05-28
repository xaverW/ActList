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
import de.mtplayer.actList.controller.config.ProgConst;
import de.mtplayer.actList.controller.config.ProgData;
import de.mtplayer.actList.controller.config.ProgInfos;
import de.mtplayer.mtp.controller.filmlist.filmlistUrls.FilmlistUrlData;
import de.p2tools.p2Lib.tools.log.PLog;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class IoWriteXml implements AutoCloseable {

    private XMLStreamWriter writer = null;
    private OutputStreamWriter out = null;
    private Path xmlFilePath = null;
    private OutputStream os = null;
    private ProgData progData = null;

    public IoWriteXml(ProgData progData) {
        this.progData = progData;
    }

    public synchronized void writeData() {
        xmlFilePath = new ProgInfos().getSettingsFile();
        PLog.sysLog("Daten Schreiben nach: " + xmlFilePath.toString());
        xmlWriteData();
    }

    private void xmlWriteData() {
        try {
            xmlWriteStart();

            writer.writeCharacters("\n\n");
            writer.writeComment("Programmeinstellungen");
            writer.writeCharacters("\n");
            xmlWriteConfig(ProgConfig.SYSTEM, ProgConfig.getAll());
            writer.writeCharacters("\n");

            writer.writeCharacters("\n\n");
            writer.writeComment("Update Filmliste");
            writer.writeCharacters("\n");
            xmlWriteFilmUpdateServer();

            writer.writeCharacters("\n\n");
            xmlWriteEnd();
        } catch (final Exception ex) {
            PLog.errorLog(656328109, ex);
        }
    }

    private void xmlWriteStart() throws IOException, XMLStreamException {
        PLog.sysLog("Start Schreiben nach: " + xmlFilePath.toAbsolutePath());
        os = Files.newOutputStream(xmlFilePath);
        out = new OutputStreamWriter(os, StandardCharsets.UTF_8);

        final XMLOutputFactory outFactory = XMLOutputFactory.newInstance();
        writer = outFactory.createXMLStreamWriter(out);
        writer.writeStartDocument(StandardCharsets.UTF_8.name(), "1.0");
        writer.writeCharacters("\n");// neue Zeile
        writer.writeStartElement(ProgConst.XML_START);
        writer.writeCharacters("\n");// neue Zeile
    }

    private void xmlWriteFilmUpdateServer() throws XMLStreamException {
        // FilmUpdate schreiben
        writer.writeCharacters("\n");
        writer.writeComment("Akt-Filmliste");
        writer.writeCharacters("\n");

        for (final FilmlistUrlData filmlistUrlData : progData.loadFilmlist.getDownloadUrlsFilmlisten_akt()) {
            filmlistUrlData.arr[FilmlistUrlData.FILMLIST_UPDATE_SERVER_SORT_NR] = FilmlistUrlData.SERVER_ART_AKT;
            xmlWriteData(FilmlistUrlData.FILMLIST_UPDATE_SERVER,
                    FilmlistUrlData.FILMLIST_UPDATE_SERVER_COLUMN_NAMES,
                    filmlistUrlData.arr,
                    false);
        }

        writer.writeCharacters("\n");
        writer.writeComment("Diff-Filmliste");
        writer.writeCharacters("\n");
        for (final FilmlistUrlData filmlistUrlData : progData.loadFilmlist.getDownloadUrlsFilmlisten_diff()) {
            filmlistUrlData.arr[FilmlistUrlData.FILMLIST_UPDATE_SERVER_SORT_NR] = FilmlistUrlData.SERVER_ART_DIFF;
            xmlWriteData(FilmlistUrlData.FILMLIST_UPDATE_SERVER,
                    FilmlistUrlData.FILMLIST_UPDATE_SERVER_COLUMN_NAMES,
                    filmlistUrlData.arr,
                    false);
        }
    }

    private void xmlWriteData(String xmlName, String[] xmlArray, String[] dataArray, boolean newLine) {
        final int xmlMax = dataArray.length;
        try {
            writer.writeStartElement(xmlName);
            if (newLine) {
                writer.writeCharacters("\n"); // neue Zeile
            }
            for (int i = 0; i < xmlMax; ++i) {
                if (!dataArray[i].isEmpty()) {
                    if (newLine) {
                        writer.writeCharacters("\t"); // Tab
                    }
                    writer.writeStartElement(xmlArray[i]);
                    writer.writeCharacters(dataArray[i]);
                    writer.writeEndElement();
                    if (newLine) {
                        writer.writeCharacters("\n"); // neue Zeile
                    }
                }
            }
            writer.writeEndElement();
            writer.writeCharacters("\n"); // neue Zeile
        } catch (final Exception ex) {
            PLog.errorLog(198325017, ex);
        }
    }

    private void xmlWriteConfig(String xmlName, String[][] xmlArray) {
        try {
            writer.writeStartElement(xmlName);
            writer.writeCharacters("\n"); // neue Zeile

            for (final String[] xmlSpalte : xmlArray) {
                writer.writeCharacters("\t"); // Tab
                writer.writeStartElement(xmlSpalte[0]);
                writer.writeCharacters(xmlSpalte[1]);
                writer.writeEndElement();
                writer.writeCharacters("\n"); // neue Zeile
            }
            writer.writeEndElement();
            writer.writeCharacters("\n"); // neue Zeile
        } catch (final Exception ex) {
            PLog.errorLog(951230478, ex);
        }
    }

    private void xmlWriteEnd() throws Exception {
        writer.writeEndElement();
        writer.writeEndDocument();
        writer.flush();

        PLog.sysLog("geschrieben!");
    }

    @Override
    public void close() throws Exception {
        writer.close();
        out.close();
        os.close();
    }
}
