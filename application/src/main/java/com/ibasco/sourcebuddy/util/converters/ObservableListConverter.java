package com.ibasco.sourcebuddy.util.converters;

import com.ibasco.sourcebuddy.domain.DockLayout;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.ArrayList;
import java.util.List;

@Converter
public class ObservableListConverter implements AttributeConverter<ObservableList<DockLayout>, List<DockLayout>> {

    @Override
    public List<DockLayout> convertToDatabaseColumn(ObservableList<DockLayout> attribute) {
        if (attribute == null) {
            return null;
        }
        return new ArrayList<>(attribute);
    }

    @Override
    public ObservableList<DockLayout> convertToEntityAttribute(List<DockLayout> dbData) {
        if (dbData == null) {
            return null;
        }
        return FXCollections.observableArrayList(dbData);
    }
}
