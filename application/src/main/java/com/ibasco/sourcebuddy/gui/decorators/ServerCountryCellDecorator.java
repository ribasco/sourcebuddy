package com.ibasco.sourcebuddy.gui.decorators;

import com.ibasco.sourcebuddy.domain.Country;
import com.ibasco.sourcebuddy.domain.ServerDetails;
import com.ibasco.sourcebuddy.util.ResourceUtil;
import javafx.geometry.Pos;
import javafx.scene.control.IndexedCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.TextAlignment;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ServerCountryCellDecorator implements CellDecorator<ServerDetails, Country> {

    private Map<String, Image> iconCache = new HashMap<>();

    @Override
    public void decorate(Country country, IndexedCell<ServerDetails> cell) {
        cell.setGraphic(createIconFromCountryCode(country.getCountryCode()));
        cell.setText(country.getCountryName());
        cell.setAlignment(Pos.CENTER_LEFT);
        cell.setTextAlignment(TextAlignment.RIGHT);
    }

    private ImageView createIconFromCountryCode(String countryCode) {
        String key = countryCode.toLowerCase();
        Image iconImage = iconCache.computeIfAbsent(key, s -> ResourceUtil.loadIcon(String.format("/country/%s", key)));
        ImageView icon = new ImageView(iconImage);
        icon.setPreserveRatio(true);
        icon.setFitWidth(16);
        return icon;
    }
}
