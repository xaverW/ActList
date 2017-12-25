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

import javafx.application.Platform;
import javafx.beans.property.*;

import java.util.concurrent.CountDownLatch;

public class MLProperty {

    // todo
    // durch Platform.runLater kann das setzen/anschließende abrufen durcheinander kommen
    // geht vielleicht besser??

    public static void setProperty(DoubleProperty property, Double wert) {
        setProp(property, wert);
    }

    public static void setProperty(IntegerProperty property, Integer wert) {
        setProp(property, wert);
    }

    public static void setProperty(BooleanProperty property, Boolean wert) {
        setProp(property, wert);
    }

    public static void setProperty(StringProperty property, String wert) {
        setProp(property, wert);
    }

    private static void setProp(Property p, Object o) {
        final CountDownLatch latch = new CountDownLatch(1);

        if (Platform.isFxApplicationThread()) {
            p.setValue(o);
        } else {
            Platform.runLater(() -> {
                try {
                    p.setValue(o);
                } finally {
                    latch.countDown();
                }
            });

            try {
                latch.await();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }

        }
    }

}
