package com.ibasco.sourcebuddy.model;

import com.ibasco.sourcebuddy.domain.SteamApp;
import javafx.beans.Observable;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SteamGamesModel {

    private ObservableList<SteamApp> blSteamApps = FXCollections.observableArrayList(param -> new Observable[] {param.bookmarkedProperty()});

    private FilteredList<SteamApp> blBookmarkedApps = new FilteredList<>(blSteamApps, SteamApp::isBookmarked);

    private ListProperty<SteamApp> steamAppList = new SimpleListProperty<>(blSteamApps);

    private ListProperty<SteamApp> bookmarkedAppList = new SimpleListProperty<>(blBookmarkedApps);

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
        this.steamAppList.clear();
        this.steamAppList.addAll(steamAppList);
    }

    public ObservableList<SteamApp> getBookmarkedAppList() {
        return bookmarkedAppList.get();
    }

    public ListProperty<SteamApp> bookmarkedAppListProperty() {
        return bookmarkedAppList;
    }

    public void setBookmarkedAppList(ObservableList<SteamApp> bookmarkedAppList) {
        this.bookmarkedAppList.set(bookmarkedAppList);
    }
}
