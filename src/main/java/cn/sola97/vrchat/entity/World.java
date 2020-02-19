package cn.sola97.vrchat.entity;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Date;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class World {
    private String id;
    private String name;
    private String description;
    private Boolean featured;
    private String authorId;
    private String authorName;
    private Integer capacity;
    private List<String> tags;
    private String releaseStatus;
    private String imageUrl;
    private String thumbnailImageUrl;
    private String assetUrl;
    private Object assetUrlObject;
    private String pluginUrl;
    private Object pluginUrlObject;
    private String unityPackageUrl;
    private Object unityPackageUrlObject;
    private String namespace;
    private Boolean unityPackageUpdated;
    private Integer version;
    private String organization;
    private String previewYoutubeId;
    private Integer favorites;
    //@JsonDeserialize(converter = MyDateConverter.class)
    private Date created_at;
    //@JsonDeserialize(converter = MyDateConverter.class)
    private Date updated_at;
    //@JsonDeserialize(converter = MyDateConverter.class)
//    private Date publicationDate;
//    //@JsonDeserialize(converter = MyDateConverter.class)
//    private Date labsPublicationDate;
    private Integer visits;
    private Integer popularity;
    private Integer heat;
    private Integer publicOccupants;
    private Integer privateOccupants;
    private Integer occupants;
    private List<List> instances;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getFeatured() {
        return featured;
    }

    public void setFeatured(Boolean featured) {
        this.featured = featured;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getReleaseStatus() {
        return releaseStatus;
    }

    public void setReleaseStatus(String releaseStatus) {
        this.releaseStatus = releaseStatus;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getThumbnailImageUrl() {
        return thumbnailImageUrl;
    }

    public void setThumbnailImageUrl(String thumbnailImageUrl) {
        this.thumbnailImageUrl = thumbnailImageUrl;
    }

    public String getAssetUrl() {
        return assetUrl;
    }

    public void setAssetUrl(String assetUrl) {
        this.assetUrl = assetUrl;
    }

    public Object getAssetUrlObject() {
        return assetUrlObject;
    }

    public void setAssetUrlObject(Object assetUrlObject) {
        this.assetUrlObject = assetUrlObject;
    }

    public String getPluginUrl() {
        return pluginUrl;
    }

    public void setPluginUrl(String pluginUrl) {
        this.pluginUrl = pluginUrl;
    }

    public Object getPluginUrlObject() {
        return pluginUrlObject;
    }

    public void setPluginUrlObject(Object pluginUrlObject) {
        this.pluginUrlObject = pluginUrlObject;
    }

    public String getUnityPackageUrl() {
        return unityPackageUrl;
    }

    public void setUnityPackageUrl(String unityPackageUrl) {
        this.unityPackageUrl = unityPackageUrl;
    }

    public Object getUnityPackageUrlObject() {
        return unityPackageUrlObject;
    }

    public void setUnityPackageUrlObject(Object unityPackageUrlObject) {
        this.unityPackageUrlObject = unityPackageUrlObject;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public Boolean getUnityPackageUpdated() {
        return unityPackageUpdated;
    }

    public void setUnityPackageUpdated(Boolean unityPackageUpdated) {
        this.unityPackageUpdated = unityPackageUpdated;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getPreviewYoutubeId() {
        return previewYoutubeId;
    }

    public void setPreviewYoutubeId(String previewYoutubeId) {
        this.previewYoutubeId = previewYoutubeId;
    }

    public Integer getFavorites() {
        return favorites;
    }

    public void setFavorites(Integer favorites) {
        this.favorites = favorites;
    }

    public Date getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Date created_at) {
        this.created_at = created_at;
    }

    public Date getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(Date updated_at) {
        this.updated_at = updated_at;
    }
//
//    public Date getPublicationDate() {
//        return publicationDate;
//    }
//
//    public void setPublicationDate(Date publicationDate) {
//        this.publicationDate = publicationDate;
//    }
//
//    public Date getLabsPublicationDate() {
//        return labsPublicationDate;
//    }
//
//    public void setLabsPublicationDate(Date labsPublicationDate) {
//        this.labsPublicationDate = labsPublicationDate;
//    }

    public Integer getVisits() {
        return visits;
    }

    public void setVisits(Integer visits) {
        this.visits = visits;
    }

    public Integer getPopularity() {
        return popularity;
    }

    public void setPopularity(Integer popularity) {
        this.popularity = popularity;
    }

    public Integer getHeat() {
        return heat;
    }

    public void setHeat(Integer heat) {
        this.heat = heat;
    }

    public Integer getPublicOccupants() {
        return publicOccupants;
    }

    public void setPublicOccupants(Integer publicOccupants) {
        this.publicOccupants = publicOccupants;
    }

    public Integer getPrivateOccupants() {
        return privateOccupants;
    }

    public void setPrivateOccupants(Integer privateOccupants) {
        this.privateOccupants = privateOccupants;
    }

    public Integer getOccupants() {
        return occupants;
    }

    public void setOccupants(Integer occupants) {
        this.occupants = occupants;
    }

    public List<List> getInstances() {
        return instances;
    }

    public void setInstances(List<List> instances) {
        this.instances = instances;
    }

    @Override
    public String toString() {
        return "World{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", featured=" + featured +
                ", authorId='" + authorId + '\'' +
                ", authorName='" + authorName + '\'' +
                ", capacity=" + capacity +
                ", tags=" + tags +
                ", releaseStatus='" + releaseStatus + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", thumbnailImageUrl='" + thumbnailImageUrl + '\'' +
                ", assetUrl='" + assetUrl + '\'' +
                ", assetUrlObject=" + assetUrlObject +
                ", pluginUrl='" + pluginUrl + '\'' +
                ", pluginUrlObject=" + pluginUrlObject +
                ", unityPackageUrl='" + unityPackageUrl + '\'' +
                ", unityPackageUrlObject=" + unityPackageUrlObject +
                ", namespace='" + namespace + '\'' +
                ", unityPackageUpdated=" + unityPackageUpdated +
                ", version=" + version +
                ", organization='" + organization + '\'' +
                ", previewYoutubeId='" + previewYoutubeId + '\'' +
                ", favorites=" + favorites +
                ", created_at=" + created_at +
                ", updated_at=" + updated_at +
//                ", publicationDate=" + publicationDate +
//                ", labsPublicationDate=" + labsPublicationDate +
                ", visits=" + visits +
                ", popularity=" + popularity +
                ", heat=" + heat +
                ", publicOccupants=" + publicOccupants +
                ", privateOccupants=" + privateOccupants +
                ", occupants=" + occupants +
                ", instances=" + instances +
                '}';
    }
}
@JsonIgnoreProperties(ignoreUnknown = true)
class UnityPackages {
    private String id;
    private String assetUrl;
    private Object assetUrlObject;
    private String pluginUrl;
    private String pluginUrlObject;
    private String unityVersion;
    private Integer unitySortNumber;
    private Integer assetVersion;
    private String platform;
    private String Date;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAssetUrl() {
        return assetUrl;
    }

    public void setAssetUrl(String assetUrl) {
        this.assetUrl = assetUrl;
    }

    public Object getAssetUrlObject() {
        return assetUrlObject;
    }

    public void setAssetUrlObject(Object assetUrlObject) {
        this.assetUrlObject = assetUrlObject;
    }

    public String getPluginUrl() {
        return pluginUrl;
    }

    public void setPluginUrl(String pluginUrl) {
        this.pluginUrl = pluginUrl;
    }

    public String getPluginUrlObject() {
        return pluginUrlObject;
    }

    public void setPluginUrlObject(String pluginUrlObject) {
        this.pluginUrlObject = pluginUrlObject;
    }

    public String getUnityVersion() {
        return unityVersion;
    }

    public void setUnityVersion(String unityVersion) {
        this.unityVersion = unityVersion;
    }

    public Integer getUnitySortNumber() {
        return unitySortNumber;
    }

    public void setUnitySortNumber(Integer unitySortNumber) {
        this.unitySortNumber = unitySortNumber;
    }

    public Integer getAssetVersion() {
        return assetVersion;
    }

    public void setAssetVersion(Integer assetVersion) {
        this.assetVersion = assetVersion;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    @Override
    public String toString() {
        return "UnityPackages{" +
                "id='" + id + '\'' +
                ", assetUrl='" + assetUrl + '\'' +
                ", assetUrlObject=" + assetUrlObject +
                ", pluginUrl='" + pluginUrl + '\'' +
                ", pluginUrlObject='" + pluginUrlObject + '\'' +
                ", unityVersion='" + unityVersion + '\'' +
                ", unitySortNumber=" + unitySortNumber +
                ", assetVersion=" + assetVersion +
                ", platform='" + platform + '\'' +
                ", Date='" + Date + '\'' +
                '}';
    }
}