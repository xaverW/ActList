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

package de.mtplayer.tools;


import org.apache.commons.lang3.time.FastDateFormat;

/**
 * Central collection class for used string formatters.
 * Since {@link FastDateFormat} is threadsafe we can use it this way.
 */

public final class StringFormatters {
    public static final FastDateFormat FORMATTER_ddMMyyyy = FastDateFormat.getInstance("dd.MM.yyyy");
    public static final FastDateFormat FORMATTER_yyyyMMdd = FastDateFormat.getInstance("yyyyMMdd");
    public static final FastDateFormat FORMATTER_HHmmss = FastDateFormat.getInstance("HH:mm:ss");
    public static final FastDateFormat FORMATTER_ddMMyyyyHHmm = FastDateFormat.getInstance("dd.MM.yyyy, HH:mm");
    public static final FastDateFormat FORMATTER_ddMMyyyyHHmmss = FastDateFormat.getInstance("dd.MM.yyyyHH:mm:ss");
}
