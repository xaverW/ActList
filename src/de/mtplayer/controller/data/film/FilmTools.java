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

package de.mtplayer.controller.data.film;

import de.mtplayer.gui.tools.MTOpen;
import de.mtplayer.mLib.tools.FileSize;
import de.mtplayer.mLib.tools.Log;
import javafx.collections.ObservableList;
import javafx.scene.control.Hyperlink;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class FilmTools {

    public static final String THEMA_LIVE = "Livestream";

    public static void getInfoText(Film film, ObservableList list) {

        list.clear();

        if (film == null) {
            return;
        }

        Text text1, text2;
        text1 = new Text(film.arr[FilmXml.FILM_SENDER] + "  -  " + film.arr[FilmXml.FILM_TITEL] + "\n\n");
        text1.setFont(Font.font(null, FontWeight.BOLD, -1));

        text2 = new Text(film.arr[FilmXml.FILM_BESCHREIBUNG]);
        text2.setWrappingWidth(20);

        list.addAll(text1, text2);

        if (!film.arr[FilmXml.FILM_WEBSEITE].isEmpty()) {
            Hyperlink hyperlink = new Hyperlink(film.arr[FilmXml.FILM_WEBSEITE]);
            list.addAll(new Text("\n\n zur Website: "), hyperlink);

            hyperlink.setOnAction(a -> {
                try {
                    MTOpen.openURL(film.arr[FilmXml.FILM_WEBSEITE]);
                } catch (Exception e) {
                    Log.errorLog(975421021, e);
                }
            });
        }
    }

    public static String getSizeFromWeb(Film film, String url) {
        if (url.equals(film.arr[FilmXml.FILM_URL])) {
            return film.arr[FilmXml.FILM_GROESSE];
        } else {
            return FileSize.getFileSizeFromUrl(url);
        }
    }

}
