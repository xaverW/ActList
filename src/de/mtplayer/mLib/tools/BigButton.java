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


import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.text.TextFlow;

public class BigButton extends Button {

    public BigButton(ImageView img, String header, String text) {
        setMaxWidth(Double.MAX_VALUE);

        GridPane graphicGrid = new GridPane();
        graphicGrid.setMaxWidth(Double.MAX_VALUE);
        graphicGrid.setHgap(5);
        graphicGrid.setVgap(5);

        Label btnHeaderLabel = new Label(header);
        btnHeaderLabel.setStyle("-fx-font-size: 1.5em;");

        Label detailsLabel = new Label(text);

        graphicGrid.add(img, 0, 0);
        graphicGrid.add(btnHeaderLabel, 1, 0);
        if (!text.isEmpty()) {
            graphicGrid.add(detailsLabel, 1, 1);
        }

        setGraphic(graphicGrid);
    }

    public BigButton(ImageView img, String header, TextFlow text) {
        setMaxWidth(Double.MAX_VALUE);

        GridPane graphicGrid = new GridPane();
        graphicGrid.setMaxWidth(Double.MAX_VALUE);
        graphicGrid.setHgap(5);
        graphicGrid.setVgap(5);

        Label btnHeaderLabel = new Label(header);
        btnHeaderLabel.setStyle("-fx-font-size: 1.5em;");
        graphicGrid.add(img, 0, 0);
        graphicGrid.add(btnHeaderLabel, 1, 0);
        graphicGrid.add(text, 1, 1);

        setGraphic(graphicGrid);
    }
}
