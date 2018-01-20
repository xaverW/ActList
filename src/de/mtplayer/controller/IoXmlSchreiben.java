/*
 * MTPlayer Copyright (C) 2017 W. Xaver W.Xaver[at]googlemail.com
 * https://sourceforge.net/projects/mtplayer/
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

package de.mtplayer.controller;

import de.mtplayer.controller.config.Config;
import de.mtplayer.controller.config.Const;
import de.mtplayer.controller.config.Daten;
import de.mtplayer.controller.config.ProgInfos;
import de.mtplayer.controller.loadFilmlist.FilmlistUrlData;
import de.mtplayer.tools.Log;
import de.mtplayer.tools.SysMsg;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class IoXmlSchreiben implements AutoCloseable {

    private XMLStreamWriter writer = null;
    private OutputStreamWriter out = null;
    private Path xmlFilePath = null;
    private OutputStream os = null;
    private Daten daten = null;

    public IoXmlSchreiben(Daten daten) {
        this.daten = daten;
    }

    public synchronized void datenSchreiben() {
        xmlFilePath = new ProgInfos().getXmlFilePath();
        SysMsg.sysMsg("Daten Schreiben nach: " + xmlFilePath.toString());
        xmlDatenSchreiben();
    }

    private void xmlDatenSchreiben() {
        try {
            xmlSchreibenStart();

            writer.writeCharacters("\n\n");
            writer.writeComment("Programmeinstellungen");
            writer.writeCharacters("\n");
            xmlSchreibenConfig(Config.SYSTEM, Config.getAll());
            writer.writeCharacters("\n");

            writer.writeCharacters("\n\n");
            writer.writeComment("Update Filmliste");
            writer.writeCharacters("\n");
            xmlSchreibenFilmUpdateServer();

            writer.writeCharacters("\n\n");
            xmlSchreibenEnde();
        } catch (final Exception ex) {
            Log.errorLog(656328109, ex);
        }
    }

    private void xmlSchreibenStart() throws IOException, XMLStreamException {
        SysMsg.sysMsg("Start Schreiben nach: " + xmlFilePath.toAbsolutePath());
        os = Files.newOutputStream(xmlFilePath);
        out = new OutputStreamWriter(os, StandardCharsets.UTF_8);

        final XMLOutputFactory outFactory = XMLOutputFactory.newInstance();
        writer = outFactory.createXMLStreamWriter(out);
        writer.writeStartDocument(StandardCharsets.UTF_8.name(), "1.0");
        writer.writeCharacters("\n");// neue Zeile
        writer.writeStartElement(Const.XML_START);
        writer.writeCharacters("\n");// neue Zeile
    }

    private void xmlSchreibenFilmUpdateServer() throws XMLStreamException {
        // FilmUpdate schreiben
        writer.writeCharacters("\n");
        writer.writeComment("Akt-Filmliste");
        writer.writeCharacters("\n");

        for (final FilmlistUrlData datenUrlFilmliste : daten.loadFilmList.getDownloadUrlsFilmlisten_akt()) {
            datenUrlFilmliste.arr[FilmlistUrlData.FILMLIST_UPDATE_SERVER_ART_NR] = FilmlistUrlData.SERVER_ART_AKT;
            xmlSchreibenDaten(FilmlistUrlData.FILMLIST_UPDATE_SERVER,
                    FilmlistUrlData.FILMLIST_UPDATE_SERVER_COLUMN_NAMES,
                    datenUrlFilmliste.arr,
                    false);
        }

        writer.writeCharacters("\n");
        writer.writeComment("Diff-Filmliste");
        writer.writeCharacters("\n");
        for (final FilmlistUrlData datenUrlFilmliste : daten.loadFilmList.getDownloadUrlsFilmlisten_diff()) {
            datenUrlFilmliste.arr[FilmlistUrlData.FILMLIST_UPDATE_SERVER_ART_NR] = FilmlistUrlData.SERVER_ART_DIFF;
            xmlSchreibenDaten(FilmlistUrlData.FILMLIST_UPDATE_SERVER,
                    FilmlistUrlData.FILMLIST_UPDATE_SERVER_COLUMN_NAMES,
                    datenUrlFilmliste.arr,
                    false);
        }
    }

    private void xmlSchreibenDaten(String xmlName, String[] xmlSpalten, String[] datenArray, boolean newLine) {
        final int xmlMax = datenArray.length;
        try {
            writer.writeStartElement(xmlName);
            if (newLine) {
                writer.writeCharacters("\n"); // neue Zeile
            }
            for (int i = 0; i < xmlMax; ++i) {
                if (!datenArray[i].isEmpty()) {
                    if (newLine) {
                        writer.writeCharacters("\t"); // Tab
                    }
                    writer.writeStartElement(xmlSpalten[i]);
                    writer.writeCharacters(datenArray[i]);
                    writer.writeEndElement();
                    if (newLine) {
                        writer.writeCharacters("\n"); // neue Zeile
                    }
                }
            }
            writer.writeEndElement();
            writer.writeCharacters("\n"); // neue Zeile
        } catch (final Exception ex) {
            Log.errorLog(198325017, ex);
        }
    }

    private void xmlSchreibenConfig(String xmlName, String[][] xmlSpalten) {
        try {
            writer.writeStartElement(xmlName);
            writer.writeCharacters("\n"); // neue Zeile

            for (final String[] xmlSpalte : xmlSpalten) {
//                if (!Config.find(xmlSpalte[0])) {
//                    continue; // nur Configs schreiben die es noch gibt
//                }
                writer.writeCharacters("\t"); // Tab
                writer.writeStartElement(xmlSpalte[0]);
                writer.writeCharacters(xmlSpalte[1]);
                writer.writeEndElement();
                writer.writeCharacters("\n"); // neue Zeile
            }
            writer.writeEndElement();
            writer.writeCharacters("\n"); // neue Zeile
        } catch (final Exception ex) {
            Log.errorLog(951230478, ex);
        }
    }

    private void xmlSchreibenEnde() throws Exception {
        writer.writeEndElement();
        writer.writeEndDocument();
        writer.flush();

        SysMsg.sysMsg("geschrieben!");
    }

    @Override
    public void close() throws Exception {
        writer.close();
        out.close();
        os.close();
    }
}
