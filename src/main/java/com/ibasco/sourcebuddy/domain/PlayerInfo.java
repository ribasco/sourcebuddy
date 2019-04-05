package com.ibasco.sourcebuddy.domain;

import javafx.beans.property.*;

public class PlayerInfo extends AuditableEntity<String> {

    private IntegerProperty index = new SimpleIntegerProperty(-1);

    private StringProperty name = new SimpleStringProperty("N/A");

    private IntegerProperty score = new SimpleIntegerProperty(-1);

    private FloatProperty duration = new SimpleFloatProperty(0.0f);

    public int getIndex() {
        return index.get();
    }

    public void setIndex(int index) {
        this.index.set(index);
    }

    public IntegerProperty indexProperty() {
        return index;
    }

    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public StringProperty nameProperty() {
        return name;
    }

    public int getScore() {
        return score.get();
    }

    public void setScore(int score) {
        this.score.set(score);
    }

    public IntegerProperty scoreProperty() {
        return score;
    }

    public float getDuration() {
        return duration.get();
    }

    public void setDuration(float duration) {
        this.duration.set(duration);
    }

    public FloatProperty durationProperty() {
        return duration;
    }
}
