package demo.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.bson.types.ObjectId;

public class UserConnection {

    @JsonProperty("_id")
    private ObjectId key;

    /* Primary key*/
    private String userId;
    private String providerId;
    private String providerUserId;

    /* Attributes */
    private int rank;
    private String displayName;
    private String profileUrl;
    private String imageUrl;
    private String accessToken;
    private String secret;
    private String refreshToken;
    private Long expireTime;


    /* Jackson */ UserConnection() {
    }

    public UserConnection(String userId, String providerId, String providerUserId) {
        this.userId = userId;
        this.providerId = providerId;
        this.providerUserId = providerUserId;
    }

    public UserConnection withRank(int rank) {
        this.rank = rank;
        return this;
    }

    public UserConnection withDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    public UserConnection withProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
        return this;
    }

    public UserConnection withImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
        return this;
    }

    public UserConnection withAccessToken(String accessToken) {
        this.accessToken = accessToken;
        return this;
    }

    public UserConnection withSecret(String secret) {
        this.secret = secret;
        return this;
    }

    public UserConnection withRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
        return this;
    }

    public UserConnection withExpireTime(Long expireTime) {
        this.expireTime = expireTime;
        return this;
    }

    public String getUserId() {
        return userId;
    }

    public String getProviderId() {
        return providerId;
    }

    public String getProviderUserId() {
        return providerUserId;
    }

    public int getRank() {
        return rank;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getSecret() {
        return secret;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public Long getExpireTime() {
        return expireTime;
    }
}
