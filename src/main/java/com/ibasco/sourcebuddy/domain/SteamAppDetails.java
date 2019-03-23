package com.ibasco.sourcebuddy.domain;

import com.ibasco.sourcebuddy.data.converters.ImageDataConverter;
import com.ibasco.sourcebuddy.data.types.ImageData;
import javafx.beans.property.*;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = SteamAppDetails.TABLE_NAME)
public class SteamAppDetails extends AuditableEntity<SteamAppDetails> implements Serializable {

    public static final String TABLE_NAME = "SB_STEAM_APP_DETAILS";

    public static final String DETAILS_ID = "details_id";

    public static final String NAME = "name";

    public static final String SHORT_DESCRIPTION = "short_description";

    public static final String DETAILED_DESCRIPTION = "detailed_description";

    public static final String HEADER_IMAGE_URL = "header_image_url";

    public static final String TYPE = "type";

    public static final String HEADER_IMAGE = "header_image";

    private IntegerProperty id = new SimpleIntegerProperty();

    private ObjectProperty<SteamApp> steamApp = new SimpleObjectProperty<>();

    private StringProperty name = new SimpleStringProperty();

    private StringProperty shortDescription = new SimpleStringProperty();

    private StringProperty detailedDescription = new SimpleStringProperty();

    private StringProperty headerImageUrl = new SimpleStringProperty();

    private StringProperty type = new SimpleStringProperty();

    private ObjectProperty<ImageData> image = new SimpleObjectProperty<>();

    @Id
    @Column(name = DETAILS_ID)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public int getId() {
        return id.get();
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public IntegerProperty idProperty() {
        return id;
    }

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = SteamApp.APP_ID, referencedColumnName = SteamApp.APP_ID)
    public SteamApp getSteamApp() {
        return steamApp.get();
    }

    public void setSteamApp(SteamApp steamApp) {
        this.steamApp.set(steamApp);
    }

    public ObjectProperty<SteamApp> steamAppProperty() {
        return steamApp;
    }

    @Column(name = NAME, nullable = true, updatable = true)
    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public StringProperty nameProperty() {
        return name;
    }

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

    @Lob
    @Column(name = HEADER_IMAGE)
    @Convert(converter = ImageDataConverter.class)
    public ImageData getImage() {
        return image.get();
    }

    public void setImage(ImageData image) {
        this.image.set(image);
    }

    public ObjectProperty<ImageData> imageProperty() {
        return image;
    }
}
