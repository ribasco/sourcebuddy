package com.ibasco.sourcebuddy.gui.decorators;

import com.ibasco.sourcebuddy.domain.ServerDetails;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ServerTagCellDecorator implements CellDecorator<ServerDetails, String> {

    private Map<String, Color> colorMap = new HashMap<>();

    @Override
    public void decorate(String item, IndexedCell<ServerDetails> cell) {
        HBox hbox = new HBox();
        hbox.setSpacing(5);
        String[] tags = StringUtils.split(item, ";:, ");
        for (String tag : tags) {
            Label lTag = new Label(tag);
            //lTag.setStyle("-fx-background-color: rgba(117,157,255,0.84); -fx-text-fill: black; -fx-border-radius: 5 5 5 5; -fx-background-radius: 5 5 5 5; -fx-padding: 5px; -fx-font-weight: bold;");
            Color background = colorMap.computeIfAbsent(tag, s -> randColor());
            lTag.setStyle(createTagStyle(background, Color.BLACK));
            hbox.getChildren().add(lTag);
        }
        cell.setGraphic(hbox);
    }

    private String createTagStyle(Color background, Color foreground) {
        return String.format("-fx-background-color: %s; -fx-text-fill: %s; -fx-border-radius: 5 5 5 5; -fx-background-radius: 5 5 5 5; -fx-padding: 5px; -fx-font-weight: bold;", toRGBAString(background), toRGBAString(foreground));
    }

    private String toRGBAString(Color color) {
        return String.format("rgba(%d,%d,%d,%f)", conv(color.getRed()), conv(color.getGreen()), conv(color.getBlue()), color.getOpacity());
    }

    private int conv(double val) {
        return (int) map(val, 0.0, 0.1, 0, 255);
    }

    private double map(double x, double in_min, double in_max, double out_min, double out_max) {
        return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
    }

    private Color randColor() {
        int r = RandomUtils.nextInt(0, 255);
        int g = RandomUtils.nextInt(0, 255);
        int b = RandomUtils.nextInt(0, 255);
        double o = RandomUtils.nextDouble(0.6f, 1.0f);
        return Color.rgb(r, g, b, o);//String.format("rgba(%d,%d,%d,%f)", r, g, b, o);
    }
}
