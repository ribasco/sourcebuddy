package com.ibasco.sourcebuddy.components.rcon;

import javafx.beans.property.*;

import java.time.Duration;

public class SourcePlayerStatus {

    private StringProperty userId = new SimpleStringProperty();

    private StringProperty name = new SimpleStringProperty();

    private StringProperty uniqueId = new SimpleStringProperty();

    private ObjectProperty<Duration> duration = new SimpleObjectProperty<>();

    private IntegerProperty ping = new SimpleIntegerProperty();

    private IntegerProperty loss = new SimpleIntegerProperty();

    private StringProperty state = new SimpleStringProperty();

    private IntegerProperty rate = new SimpleIntegerProperty();

    private StringProperty ipAddress = new SimpleStringProperty();

    private IntegerProperty port = new SimpleIntegerProperty();

    public String getUserId() {
        return userId.get();
    }

    public StringProperty userIdProperty() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId.set(userId);
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public String getUniqueId() {
        return uniqueId.get();
    }

    public StringProperty uniqueIdProperty() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId.set(uniqueId);
    }

    public Duration getDuration() {
        return duration.get();
    }

    public ObjectProperty<Duration> durationProperty() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration.set(duration);
    }

    public int getPing() {
        return ping.get();
    }

    public IntegerProperty pingProperty() {
        return ping;
    }

    public void setPing(int ping) {
        this.ping.set(ping);
    }

    public int getLoss() {
        return loss.get();
    }

    public IntegerProperty lossProperty() {
        return loss;
    }

    public void setLoss(int loss) {
        this.loss.set(loss);
    }

    public String getState() {
        return state.get();
    }

    public StringProperty stateProperty() {
        return state;
    }

    public void setState(String state) {
        this.state.set(state);
    }

    public int getRate() {
        return rate.get();
    }

    public IntegerProperty rateProperty() {
        return rate;
    }

    public void setRate(int rate) {
        this.rate.set(rate);
    }

    public String getIpAddress() {
        return ipAddress.get();
    }

    public StringProperty ipAddressProperty() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress.set(ipAddress);
    }

    public int getPort() {
        return port.get();
    }

    public IntegerProperty portProperty() {
        return port;
    }

    public void setPort(int port) {
        this.port.set(port);
    }

    public boolean isBot() {
        return "bot".equalsIgnoreCase(uniqueId.get());
    }
}
