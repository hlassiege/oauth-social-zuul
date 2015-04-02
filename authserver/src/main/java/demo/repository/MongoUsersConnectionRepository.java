package demo.repository;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import demo.model.UserConnection;
import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.social.connect.*;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;


@Component
public class MongoUsersConnectionRepository implements UsersConnectionRepository, InitializingBean {

    static final String COLLECTION_NAME = "UserConnection";

    @Autowired
    ConnectionFactoryLocator connectionFactoryLocator;
    @Autowired
    TextEncryptor textEncryptor;
    @Autowired
    ConnectionSignUp connectionSignUp;
    @Autowired
    Jongo jongo;

    @Override
    public void afterPropertiesSet() throws Exception {
        userConnectionCollection().ensureIndex("{userId:1, providerId:1, providerUserId:1 }", "{unique:true}");
        userConnectionCollection().ensureIndex("{providerId:1, rank:1 }");
        userConnectionCollection().ensureIndex("{providerId:1, providerUserId:1 }");
    }

    public List<String> findUserIdsWithConnection(Connection<?> connection) {
        Iterable<String> localUserIds = findUserIdsFrom(connection.getKey());

        if (canAutomaticSignup(localUserIds)) {
            return newlySignUpUser(connection, localUserIds);
        }
        return newArrayList(localUserIds);
    }

    public Set<String> findUserIdsConnectedTo(String providerId, Set<String> providerUserIds) {
        List<UserConnection> usersConnections = from(userConnectionCollection()
                .find("{providerId:#, providerUserId:{ $in:# } }", providerId, providerUserIds)
                .as(UserConnection.class)).toList();
        return newHashSet(Lists.transform(usersConnections, toUserIdsList()));
    }

    private MongoCollection userConnectionCollection() {
        return jongo.getCollection(COLLECTION_NAME);
    }

    public ConnectionRepository createConnectionRepository(String userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId cannot be null");
        }
        return new MongoConnectionRepository(userId, userConnectionCollection(), connectionFactoryLocator, textEncryptor);
    }

    private List<String> newlySignUpUser(Connection<?> connection, Iterable<String> localUserIds) {
        String newUserId = connectionSignUp.execute(connection);
        if (newUserId != null) {
            createConnectionRepository(newUserId).addConnection(connection);
            return newArrayList(newUserId);
        } else {
            return newArrayList(localUserIds);
        }
    }

    private boolean canAutomaticSignup(Iterable<String> localUserIds) {
        return !localUserIds.iterator().hasNext() && connectionSignUp != null;
    }

    private Iterable<String> findUserIdsFrom(ConnectionKey key) {
        List<UserConnection> usersConnections = from(userConnectionCollection()
                .find("{providerId:#, providerUserId:#}", key.getProviderId(), key.getProviderUserId())
                .as(UserConnection.class)).toList();
        return newHashSet(Lists.transform(usersConnections, toUserIdsList()));
    }

    private Function<UserConnection, String> toUserIdsList() {
        return userConnection -> userConnection != null ? userConnection.getUserId() : null;
    }

    public void removeAllConnection(String userId) {
        userConnectionCollection().remove("{userId:#}", userId);
    }

}