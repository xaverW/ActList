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

package de.mtplayer.mLib.tools;

import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;

public class FileSize {

    public static String getFileSizeFromUrl(String url) {
        // liefert die Dateigröße einer URL in MB!!
        // Anzeige der Größe in MiB und deshalb: Faktor 1000
        String groesseStr = "";

        long l = get(url);
        if (l > 1_000_000) {
            // größer als 1MiB sonst kann ich mirs sparen
            groesseStr = String.valueOf(l / 1_000_000);
        } else if (l > 0) {
            groesseStr = "1";
        }
        return groesseStr;
    }

    /**
     * Return the size of a URL in bytes.
     *
     * @param url URL as String to query.
     * @return size in bytes or -1.
     */
    private static long get(String url) {
        if (!url.toLowerCase().startsWith("http")) {
            return -1;
        }

        final Request request = new Request.Builder().url(url).head().build();
        long respLength = -1;
        try (Response response = MLHttpClient.getInstance().getReducedTimeOutClient().newCall(request).execute();
             ResponseBody body = response.body()) {
            if (response.isSuccessful())
                respLength = body.contentLength();
        } catch (IOException ignored) {
            respLength = -1;
        }

        if (respLength < 1_000_000) {
            // alles unter 1MB sind Playlisten, ORF: Trailer bei im Ausland gesperrten Filmen, ...
            // dann wars nix
            respLength = -1;
        }
        return respLength;
    }

}
