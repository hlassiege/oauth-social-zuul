package demo;

import demo.repository.AccountRepository;
import demo.repository.MongoUsersConnectionRepository;
import demo.security.SecurityUtils;
import demo.social.SimpleSignInAdapter;
import org.bson.types.ObjectId;
import org.jongo.Jongo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.NullRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.social.UserIdSource;
import org.springframework.social.config.annotation.ConnectionFactoryConfigurer;
import org.springframework.social.config.annotation.EnableSocial;
import org.springframework.social.config.annotation.SocialConfigurer;
import org.springframework.social.connect.*;
import org.springframework.social.connect.support.ConnectionFactoryRegistry;
import org.springframework.social.connect.web.ConnectController;
import org.springframework.social.connect.web.ProviderSignInController;
import org.springframework.social.facebook.api.Facebook;
import org.springframework.social.facebook.api.impl.FacebookTemplate;
import org.springframework.social.facebook.connect.FacebookConnectionFactory;
import org.springframework.social.google.api.Google;
import org.springframework.social.google.api.impl.GoogleTemplate;
import org.springframework.social.google.connect.GoogleConnectionFactory;

import javax.inject.Inject;

/**
* Spring Social Configuration.
*/
@Configuration
@EnableSocial
public class SocialConfig implements SocialConfigurer {
    @Inject
    private MongoUsersConnectionRepository usersConnectionRepository;
    @Inject
    private RememberMeServices rememberMeServices;
    @Inject
    private AccountRepository accountRepository;


    @Override
    public void addConnectionFactories(ConnectionFactoryConfigurer cfConfig, Environment env) {
        cfConfig.addConnectionFactory(new FacebookConnectionFactory(env.getProperty("facebook.appId"), env.getProperty("facebook.appSecret")));
    }

    @Override
    public UserIdSource getUserIdSource() {
        return new UserIdSource() {
            @Override
            public String getUserId() {
                String id = SecurityUtils.connectedAccountId();
                if (id == null) {
                    throw new IllegalStateException("Unable to get a user id: no user signed in");
                }
                return id;
            }
        };
    }

    @Override
    public UsersConnectionRepository getUsersConnectionRepository(ConnectionFactoryLocator connectionFactoryLocator) {
        return usersConnectionRepository;
    }


    @Bean
    @Scope(value = "request", proxyMode = ScopedProxyMode.INTERFACES)
    public Facebook facebook(ConnectionRepository repository) {
        Connection<Facebook> facebook = repository.findPrimaryConnection(Facebook.class);
        return facebook != null ? facebook.getApi() : new FacebookTemplate();
    }

    @Bean
    @Scope(value="request", proxyMode=ScopedProxyMode.INTERFACES)
    public Google google(ConnectionRepository repository) {
        Connection<Google> google = repository.findPrimaryConnection(Google.class);
        return google != null ? google.getApi() : new GoogleTemplate();
    }

    @Bean
    public ProviderSignInController providerSignInController(ConnectionFactoryLocator connectionFactoryLocator) {
        RequestCache requestCache = new HttpSessionRequestCache();

        ProviderSignInController signInController = new ProviderSignInController(
                connectionFactoryLocator,
                usersConnectionRepository,
                new SimpleSignInAdapter(requestCache, accountRepository,  rememberMeServices)
        );
        return signInController;
    }

}