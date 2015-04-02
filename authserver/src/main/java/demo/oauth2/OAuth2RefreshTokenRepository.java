package demo.oauth2;

import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Spring Data MongoDB repository for the OAuth2AuthenticationRefreshToken entity.
 */
public interface OAuth2RefreshTokenRepository extends MongoRepository<OAuth2AuthenticationRefreshToken, String> {

    public OAuth2AuthenticationRefreshToken findByTokenId(String tokenId);
}
