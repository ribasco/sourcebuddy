package com.ibasco.sourcebuddy.domain;

import com.google.gson.annotations.Expose;
import static com.ibasco.sourcebuddy.domain.UpdateManifest.TABLE_NAME;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = TABLE_NAME)
public class UpdateManifest extends AuditableEntity<String> {

    public static final String TABLE_NAME = "SB_UPDATES";

    public static final String FILE = "file";

    public static final String HASH = "hash";

    public static final String LAST_UPDATED = "last_updated";

    @Expose
    private String file;

    @Expose
    private String hash;

    @Expose
    private LocalDateTime lastUpdated;

    @Id
    @Column(name = FILE)
    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    @Column(name = HASH)
    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    @Column(name = LAST_UPDATED)
    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    @Override
    public String toString() {
        return "UpdateManifest{" +
                "file='" + file + '\'' +
                ", hash='" + hash + '\'' +
                ", lastUpdated=" + lastUpdated +
                '}';
    }
}
