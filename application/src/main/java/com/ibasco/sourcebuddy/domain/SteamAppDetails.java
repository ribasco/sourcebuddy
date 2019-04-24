package com.ibasco.sourcebuddy.domain;

import com.google.gson.annotations.Expose;
import javafx.beans.property.*;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = SteamAppDetails.TABLE_NAME)
public class SteamAppDetails extends AuditableEntity<String> {

    public static final String TABLE_NAME = "SB_STEAM_APP_DETAILS";

    public static final String DETAILS_ID = "details_id";

    public static final String NAME = "name";

    public static final String SHORT_DESCRIPTION = "short_description";

    public static final String DETAILED_DESCRIPTION = "detailed_description";

    public static final String HEADER_IMAGE_URL = "header_image_url";

    public static final String TYPE = "type";

    public static final String EMPTY_DETAILS = "empty_details";

    public static final String HEADER_IMAGE = "header_image";

    private IntegerProperty id = new SimpleIntegerProperty();

    @Expose
    private ObjectProperty<SteamApp> steamApp = new SimpleObjectProperty<>();

    @Expose
    private StringProperty name = new SimpleStringProperty();

    @Expose
    private StringProperty shortDescription = new SimpleStringProperty();

    @Expose
    private StringProperty detailedDescription = new SimpleStringProperty();

    @Expose
    private StringProperty headerImageUrl = new SimpleStringProperty();

    @Expose
    private StringProperty type = new SimpleStringProperty();

    private BooleanProperty emptyDetails = new SimpleBooleanProperty();

    @Expose
    private ObjectProperty<byte[]> headerImage = new SimpleObjectProperty<>();

    @Id
    @Column(name = DETAILS_ID)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer getId() {
        return id.getValue();
    }

    public void setId(Integer id) {
        this.id.setValue(id);
    }

    public IntegerProperty idProperty() {
        return id;
    }

    @OneToOne
    @JoinColumn(name = SteamApp.APP_ID, unique = true)
    public SteamApp getSteamApp() {
        return steamApp.get();
    }

    public void setSteamApp(SteamApp steamApp) {
        this.steamApp.set(steamApp);
    }

    public ObjectProperty<SteamApp> steamAppProperty() {
        return steamApp;
    }

    @Column(name = NAME)
    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public StringProperty nameProperty() {
        return name;
    }

    @Lob
    @Column(name = SHORT_DESCRIPTION)
    public String getShortDescription() {
        return shortDescription.get();
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription.set(shortDescription);
    }

    public StringProperty shortDescriptionProperty() {
        return shortDescription;
    }

    @Lob
    @Column(name = DETAILED_DESCRIPTION)
    public String getDetailedDescription() {
        return detailedDescription.get();
    }

    public void setDetailedDescription(String detailedDescription) {
        this.detailedDescription.set(detailedDescription);
    }

    public StringProperty detailedDescriptionProperty() {
        return detailedDescription;
    }

    @Column(name = HEADER_IMAGE_URL)
    public String getHeaderImageUrl() {
        return headerImageUrl.get();
    }

    public void setHeaderImageUrl(String headerImageUrl) {
        this.headerImageUrl.set(headerImageUrl);
    }

    public StringProperty headerImageUrlProperty() {
        return headerImageUrl;
    }

    @Column(name = TYPE)
    public String getType() {
        return type.get();
    }

    public void setType(String type) {
        this.type.set(type);
    }

    public StringProperty typeProperty() {
        return type;
    }

    @Column(name = EMPTY_DETAILS)
    public Boolean isEmptyDetails() {
        return emptyDetails.getValue();
    }

    public BooleanProperty emptyDetailsProperty() {
        return emptyDetails;
    }

    public void setEmptyDetails(Boolean emptyDetails) {
        this.emptyDetails.setValue(emptyDetails);
    }

    @Lob
    @Column(name = HEADER_IMAGE)
    public byte[] getHeaderImage() {
        return headerImage.get();
    }

    public ObjectProperty<byte[]> headerImageProperty() {
        return headerImage;
    }

    public void setHeaderImage(byte[] headerImage) {
        this.headerImage.set(headerImage);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SteamAppDetails details = (SteamAppDetails) o;
        return getId().equals(details.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    @Override
    public String toString() {
        SteamApp app = getSteamApp();
        if (app == null) {
            return "N/A";
        }
        return app.toString();
    }
}
