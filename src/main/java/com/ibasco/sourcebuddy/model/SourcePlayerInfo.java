package com.ibasco.sourcebuddy.model;

import com.ibasco.agql.protocols.valve.source.query.pojos.SourcePlayer;
import javafx.beans.property.*;

public class SourcePlayerInfo {
    private IntegerProperty index = new SimpleIntegerProperty(-1);
    private StringProperty name = new SimpleStringProperty("N/A");
    private IntegerProperty score = new SimpleIntegerProperty(-1);
    private FloatProperty duration = new SimpleFloatProperty(0.0f);

    public SourcePlayerInfo(SourcePlayer player) {
        this(player.getIndex(), player.getName(), player.getScore(), player.getDuration());
    }

    public SourcePlayerInfo(int index, String name, int score, float duration) {
        setIndex(index);
        setName(name);
        setScore(score);
        setDuration(duration);
    }

    public int getIndex() {
        return index.get();
    }

    public IntegerProperty indexProperty() {
        return index;
    }

    public void setIndex(int index) {
        this.index.set(index);
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

    public int getScore() {
        return score.get();
    }

    public IntegerProperty scoreProperty() {
        return score;
    }

    public void setScore(int score) {
        this.score.set(score);
    }

    public float getDuration() {
        return duration.get();
    }

    public FloatProperty durationProperty() {
        return duration;
    }

    public void setDuration(float duration) {
        this.duration.set(duration);
    }
}
