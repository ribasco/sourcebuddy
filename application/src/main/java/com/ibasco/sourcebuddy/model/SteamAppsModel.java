package com.ibasco.sourcebuddy.model;

import com.ibasco.sourcebuddy.domain.SteamApp;
import com.ibasco.sourcebuddy.gui.ObservableValueListProperty;
import javafx.beans.Observable;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SteamAppsModel {

    private ObservableValueListProperty<SteamApp> steamAppList = new ObservableValueListProperty<>(p -> new Observable[] {p.bookmarkedProperty()});

    private ObjectProperty<SteamApp> selectedGame = new SimpleObjectProperty<>();

    public SteamApp getSelectedGame() {
        return selectedGame.get();
    }

    public ObjectProperty<SteamApp> selectedGameProperty() {
        return selectedGame;
    }

    public void setSelectedGame(SteamApp selectedGame) {
        this.selectedGame.set(selectedGame);
    }

    public ObservableList<SteamApp> getSteamAppList() {
        return steamAppList.get();
    }

    public ListProperty<SteamApp> steamAppListProperty() {
        return steamAppList;
    }

    public void setSteamAppList(List<SteamApp> steamAppList) {
        this.steamAppList.set(steamAppList);
    }
}
