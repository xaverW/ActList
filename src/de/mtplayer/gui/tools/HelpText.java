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

package de.mtplayer.gui.tools;

public class HelpText {


    public static final String FILTER_FELDER =
            "\n" +
                    "\"Sender\" und \"Thema\" können exakt verglichen werden. " +
                    "Das heißt, der im Feld \"Sender\\Thema\" angegebene Text muss " +
                    "genau dem \"Sender\\Thema\" des Films entsprechen. " +
                    "\n" +
                    "\n" +
                    "Bei den anderen Feldern (oder wenn exakt ausgeschaltet ist) " +
                    "muss die Eingabe im " +
                    "entsprechendem Feld des Films nur enthalten sein." +
                    "\n" +
                    "\n" +
                    "Beim Feld \"Thema/Titel\" muss der Filter im " +
                    "\"Thema\" ODER \"Titel\" enthalten sein." +
                    "\n";
    public static final String FILTER_EXAKT =
            "\n" +
                    "\"exakt\" bedeutet, dass z.B. \"Abend\" im Feld Thema nur die Filme " +
                    "findet, die genau das Thema \"Abend\" haben. " +
                    "Ist \"exakt\" ausgeschaltet und steht im Feld \"Sender\" z.B. \"a\" " +
                    "dann werden alle Sender die ein \"a\" enthalten gefunden!" +
                    "\n" +
                    "\n" +
                    "Groß- und Kleinschreibung wird beim Filtern " +
                    "nicht beachtet." +
                    "\n" +
                    "\n" +
                    "In allen Feldern (wenn nicht \"exakt\" eingestellt ist) " +
                    "kann auch nach mehreren Begriffen gesucht werden (diese " +
                    "werden durch \"Komma\" oder \"Doppelpunkt\" getrennt angegeben " +
                    "und können auch Leerzeichen enthalten)." +
                    "\n" +
                    "\"Sport,Fussball\" sucht nach Filmen die im jeweiligen Feld den " +
                    "Begriff \"Sport\" ODER \"Fussball\" haben." +
                    "\n" +
                    "\"Sport:Fussball\" sucht nach Filmen die im jeweiligen Feld den " +
                    "Begriff \"Sport\" UND \"Fussball\" haben." +
                    "\n" +
                    "\n" +
                    "In allen Feldern (wenn nicht \"exakt\" eingestellt ist) " +
                    "kann auch mit regulären Ausdrücken gesucht " +
                    "werden. Diese müssen mit \"#:\" eingeleitet werden. " +
                    "Auch bei den regulären Ausdrücken wird nicht zwischen " +
                    "Groß- und Kleinschreibung unterschieden. " +
                    "\n" +
                    "#:Abend.*\n" +
                    "Das bedeutet z.B.: Es werden alle Filme gefunden, die \n" +
                    "im jeweiligen Feld mit \"Abend\" beginnen.\n" +
                    "\n" +
                    "\n" +
                    "https://de.wikipedia.org/wiki/Regul%C3%A4rer_Ausdruck\n" +
                    "\n";
    public static final String LOAD_FILM_ONLY_DAYS = "Es werden nur Filme der letzten " +
            "xx Tage geladen. " +
            "Bei \"Alle\" werden alle Filme geladen. " +
            "Eine kleinere Filmliste kann bei Rechnern mit wenig " +
            "Speicher hilfreich sein.";

    public static final String LOAD_FILMLIST_MANUEL = "Die angegebene URL wird beim " +
            "Neuladen einer Filmliste verwendet. Ist nichts angegeben, wird die Filmliste auf herkömmliche " +
            "Art geladen und die URL dafür wird automatisch gewählt.";

    public static final String LOAD_FILMLIST_URL = "Download-URL’s für die Filmliste";

    public static final String LOAD_FILMLIST_SENDER = "Filme der markierten Sender " +
            "werden beim Neuladen der Filmliste nicht geladen.";

}
