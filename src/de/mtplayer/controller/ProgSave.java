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

import de.mtplayer.controller.config.Daten;
import de.mtplayer.controller.config.ProgInfos;
import de.mtplayer.controller.loadFilmlist.WriteFilmlistJson;

public class ProgSave {
    final Daten daten;

    public ProgSave() {
        daten = Daten.getInstance();
    }

    public void filmlisteSpeichern() {
        new WriteFilmlistJson().filmlisteSchreibenJson(ProgInfos.getFilmListFile(), daten.filmList);
    }

    public void allesSpeichern() {
        try (IoXmlSchreiben writer = new IoXmlSchreiben(daten)) {
            writer.datenSchreiben();
        } catch (final Exception ex) {
            ex.printStackTrace();
        }

    }

}
