package com.ibasco.sourcebuddy.model;

import com.ibasco.sourcebuddy.domain.Country;
import com.ibasco.sourcebuddy.enums.MiscFilters;
import com.ibasco.sourcebuddy.enums.OperatingSystem;
import com.ibasco.sourcebuddy.enums.ServerStatus;
import javafx.beans.property.SetProperty;
import javafx.beans.property.SimpleSetProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.HashSet;
import java.util.TreeSet;

@Component
public class ServerFilterModel {

    private Comparator<Country> countryComparator = Comparator.comparing(Country::getCountryName);

    private SetProperty<Country> countries = new SimpleSetProperty<>(FXCollections.observableSet(new TreeSet<>(countryComparator)));

    private SetProperty<String> maps = new SimpleSetProperty<>(FXCollections.observableSet(new TreeSet<>(Comparator.comparing(String::toLowerCase))));

    private SetProperty<Country> selectedCountries = new SimpleSetProperty<>(FXCollections.observableSet(new HashSet<>()));

    private SetProperty<String> serverTags = new SimpleSetProperty<>(FXCollections.observableSet(new TreeSet<>()));

    private SetProperty<String> selectedServerTags = new SimpleSetProperty<>(FXCollections.observableSet(new HashSet<>()));

    private SetProperty<String> selectedMaps = new SimpleSetProperty<>(FXCollections.observableSet(new TreeSet<>()));

    private SetProperty<ServerStatus> selectedStatus = new SimpleSetProperty<>(FXCollections.observableSet(new HashSet<>()));

    private SetProperty<OperatingSystem> selectedOs = new SimpleSetProperty<>(FXCollections.observableSet(new HashSet<>()));

    private SetProperty<MiscFilters> selectedMiscFilters = new SimpleSetProperty<>(FXCollections.observableSet(new HashSet<>()));

    public ObservableSet<Country> getCountries() {
        return countries.get();
    }

    public SetProperty<Country> countriesProperty() {
        return countries;
    }

    public void setCountries(ObservableSet<Country> countries) {
        this.countries.set(countries);
    }

    public ObservableSet<Country> getSelectedCountries() {
        return selectedCountries.get();
    }

    public SetProperty<Country> selectedCountriesProperty() {
        return selectedCountries;
    }

    public void setSelectedCountries(ObservableSet<Country> selectedCountries) {
        this.selectedCountries.set(selectedCountries);
    }

    public ObservableSet<String> getServerTags() {
        return serverTags.get();
    }

    public SetProperty<String> serverTagsProperty() {
        return serverTags;
    }

    public void setServerTags(ObservableSet<String> serverTags) {
        this.serverTags.set(serverTags);
    }

    public ObservableSet<String> getSelectedServerTags() {
        return selectedServerTags.get();
    }

    public SetProperty<String> selectedServerTagsProperty() {
        return selectedServerTags;
    }

    public void setSelectedServerTags(ObservableSet<String> selectedServerTags) {
        this.selectedServerTags.set(selectedServerTags);
    }

    public ObservableSet<String> getSelectedMaps() {
        return selectedMaps.get();
    }

    public SetProperty<String> selectedMapsProperty() {
        return selectedMaps;
    }

    public void setSelectedMaps(ObservableSet<String> selectedMaps) {
        this.selectedMaps.set(selectedMaps);
    }

    public ObservableSet<String> getMaps() {
        return maps.get();
    }

    public SetProperty<String> mapsProperty() {
        return maps;
    }

    public void setMaps(ObservableSet<String> maps) {
        this.maps.set(maps);
    }

    public ObservableSet<ServerStatus> getSelectedStatus() {
        return selectedStatus.get();
    }

    public SetProperty<ServerStatus> selectedStatusProperty() {
        return selectedStatus;
    }

    public void setSelectedStatus(ObservableSet<ServerStatus> selectedStatus) {
        this.selectedStatus.set(selectedStatus);
    }

    public ObservableSet<OperatingSystem> getSelectedOs() {
        return selectedOs.get();
    }

    public SetProperty<OperatingSystem> selectedOsProperty() {
        return selectedOs;
    }

    public void setSelectedOs(ObservableSet<OperatingSystem> selectedOs) {
        this.selectedOs.set(selectedOs);
    }

    public ObservableSet<MiscFilters> getSelectedMiscFilters() {
        return selectedMiscFilters.get();
    }

    public SetProperty<MiscFilters> selectedMiscFiltersProperty() {
        return selectedMiscFilters;
    }

    public void setSelectedMiscFilters(ObservableSet<MiscFilters> selectedMiscFilters) {
        this.selectedMiscFilters.set(selectedMiscFilters);
    }
}
