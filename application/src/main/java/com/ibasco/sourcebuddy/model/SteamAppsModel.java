package com.ibasco.sourcebuddy.model;

import com.ibasco.sourcebuddy.domain.SteamApp;
import javafx.beans.Observable;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Callback;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SteamAppsModel {

    private ListProperty<SteamApp> steamAppList = new SimpleListProperty<>() {
        private boolean updating = false;

        @Override
        protected void invalidated() {
            if (updating)
                return;
            try {
                updating = true;
                ObservableList<SteamApp> lst = get();
                if (lst == null)
                    return;
                Callback<SteamApp, Observable[]> extractor = param -> new Observable[] {param.bookmarkedProperty()};
                set(FXCollections.observableList(lst, extractor));
            } finally {
                updating = false;
            }

        }
    };

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
        if (steamAppList == null) {
            this.steamAppList.set(null);
            return;
        }
        Callback<SteamApp, Observable[]> extractor = param -> new Observable[] {param.bookmarkedProperty()};
        this.steamAppList.set(FXCollections.observableList(steamAppList, extractor));
    }
}
