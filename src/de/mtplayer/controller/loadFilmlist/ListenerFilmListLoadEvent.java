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

package de.mtplayer.controller.loadFilmlist;

public class ListenerFilmListLoadEvent {

    private String senderUrl = "";
    private String text = "";
    private int max = 0;
    private double progress = 0;
    private boolean fehler = false;
    private int count = 0;

    public ListenerFilmListLoadEvent(String ssender, String ttext, int mmax, double pprogress, int ccount, boolean ffehler) {
        senderUrl = ssender;
        text = ttext;
        max = mmax;
        progress = pprogress;
        count = ccount;
        fehler = ffehler;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getSenderUrl() {
        return senderUrl;
    }

    public void setSenderUrl(String senderUrl) {
        this.senderUrl = senderUrl;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public double getProgress() {
        return progress;
    }

    public void setProgress(double progress) {
        this.progress = progress;
    }

    public boolean isFehler() {
        return fehler;
    }

    public void setFehler(boolean fehler) {
        this.fehler = fehler;
    }
}
