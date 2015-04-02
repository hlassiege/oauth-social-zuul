package demo.repository;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.mongodb.WriteConcern;
import com.sun.istack.internal.Nullable;
import demo.model.UserConnection;
import org.jongo.MongoCollection;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.social.connect.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.FluentIterable.from;

class MongoConnectionRepository implements ConnectionRepository {

    private final String userId;
    private final ConnectionFactoryLocator connectionFactoryLocator;
    private final TextEncryptor textEncryptor;
    private final MongoCollection collection;

    public MongoConnectionRepository(String userId, MongoCollection collection, ConnectionFactoryLocator connectionFactoryLocator, TextEncryptor textEncryptor) {
        this.userId = userId;
        this.collection = collection;
        this.connectionFactoryLocator = connectionFactoryLocator;
        this.textEncryptor = textEncryptor;
    }

    @Override
    public MultiValueMap<String, Connection<?>> findAllConnections() {
        List<UserConnection> userConnections = from(collection.find("{userId:#}", userId).sort("{providerId:1, rank:1}").as(UserConnection.class)).toList();
        List<Connection<?>> resultList = Lists.transform(userConnections, toConnection());
        MultiValueMap<String, Connection<?>> connections = new LinkedMultiValueMap<String, Connection<?>>();
        Set<String> registeredProviderIds = connectionFactoryLocator.registeredProviderIds();
        for (String registeredProviderId : registeredProviderIds) {
            connections.put(registeredProviderId, Collections.<Connection<?>>emptyList());
        }
        for (Connection<?> connection : resultList) {
            String providerId = connection.getKey().getProviderId();
            if (connections.get(providerId).size() == 0) {
                connections.put(providerId, new LinkedList<Connection<?>>());
            }
            connections.add(providerId, connection);
        }
        return connections;
    }

    @Override
    public List<Connection<?>> findConnections(String providerId) {
        List<UserConnection> userConnections = from(collection.find("{userId:#, providerId:# }", userId, providerId).sort("{rank:1}").as(UserConnection.class)).toList();
        return Lists.transform(userConnections, toConnection());
    }


    @Override
    @SuppressWarnings("unchecked")
    public <A> List<Connection<A>> findConnections(Class<A> apiType) {
        List<?> connections = findConnections(getProviderId(apiType));
        return (List<Connection<A>>) connections;
    }

    @Override
    public MultiValueMap<String, Connection<?>> findConnectionsToUsers(MultiValueMap<String, String> providerUsers) {
        if (providerUsers == null || providerUsers.isEmpty()) {
            throw new IllegalArgumentException("Unable to execute find: no providerUsers provided");
        }
        throw new NotImplementedException();
    }

    @Override
    public Connection<?> getConnection(ConnectionKey connectionKey) {
        UserConnection userConnections = getUserConnection(connectionKey);

        if (userConnections != null) {
            return toConnection().apply(userConnections);
        } else {
            throw new NoSuchConnectionException(connectionKey);
        }
    }

    private UserConnection getUserConnection(ConnectionKey connectionKey) {
        return collection.findOne("{userId:#, providerId:#, providerUserId:# }", userId, connectionKey.getProviderId(), connectionKey.getProviderUserId()).as(UserConnection.class);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <A> Connection<A> getConnection(Class<A> apiType, String providerUserId) {
        String providerId = getProviderId(apiType);
        return (Connection<A>) getConnection(new ConnectionKey(providerId, providerUserId));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <A> Connection<A> getPrimaryConnection(Class<A> apiType) {
        String providerId = getProviderId(apiType);
        Connection<A> connection = (Connection<A>) findPrimaryConnection(providerId);
        if (connection == null) {
            throw new NotConnectedException(providerId);
        }
        return connection;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <A> Connection<A> findPrimaryConnection(Class<A> apiType) {
        String providerId = getProviderId(apiType);
        return (Connection<A>) findPrimaryConnection(providerId);
    }

    @Override
    public void addConnection(Connection<?> connection) {
        ConnectionData data = connection.createData();
        int rank = new Long(collection.count("{userId:#, providerId:#}", userId, data.getProviderId())).intValue();
        collection.withWriteConcern(WriteConcern.FSYNC_SAFE).save(new UserConnection(userId, data.getProviderId(), data.getProviderUserId())
        .withRank(rank)
        .withDisplayName(data.getDisplayName())
        .withProfileUrl(data.getProfileUrl())
        .withImageUrl(data.getImageUrl())
        .withAccessToken(encrypt(data.getAccessToken()))
        .withSecret(encrypt(data.getSecret()))
        .withRefreshToken(encrypt(data.getRefreshToken()))
        .withExpireTime(data.getExpireTime()));
    }

    @Override
    public void updateConnection(Connection<?> connection) {
        ConnectionData data = connection.createData();
        UserConnection userConnection = getUserConnection(connection.getKey());
        collection.withWriteConcern(WriteConcern.FSYNC_SAFE).save(userConnection.withDisplayName(data.getDisplayName())
                .withProfileUrl(data.getProfileUrl())
                .withImageUrl(data.getImageUrl())
                .withAccessToken(encrypt(data.getAccessToken()))
                .withSecret(encrypt(data.getSecret()))
                .withRefreshToken(encrypt(data.getRefreshToken()))
                .withExpireTime(data.getExpireTime()));
    }

    @Override
    public void removeConnections(String providerId) {
        collection.remove("{ userId:#, providerId:# }", userId, providerId);
    }

    @Override
    public void removeConnection(ConnectionKey connectionKey) {
        collection.remove("{ userId:#, providerId:#, providerUserId:# }", userId, connectionKey.getProviderId(), connectionKey.getProviderUserId());
    }

    // internal helpers

    Connection<?> findPrimaryConnection(String providerId) {
        List<UserConnection> userConnections = from(
                        collection.find("{userId:#, providerId:#, rank:0 }", userId, providerId)
                        .as(UserConnection.class)).toList();
        if (userConnections.size() > 0) {
            return toConnection().apply(userConnections.get(0));
        } else {
            return null;
        }
    }

    private <A> String getProviderId(Class<A> apiType) {
        return connectionFactoryLocator.getConnectionFactory(apiType).getProviderId();
    }

    private String encrypt(String text) {
        return text != null ? textEncryptor.encrypt(text) : text;
    }

    private Function<? super UserConnection, ? extends Connection<?>> toConnection() {
        return new Function<UserConnection, Connection<?>>() {
            @Override
            public Connection<?> apply(@Nullable UserConnection userConnection) {
                ConnectionData connectionData = new ConnectionData(
                        userConnection.getProviderId(),
                        userConnection.getProviderUserId(),
                        userConnection.getDisplayName(),
                        userConnection.getProfileUrl(),
                        userConnection.getImageUrl(),
                        decrypt(userConnection.getAccessToken()),
                        decrypt(userConnection.getSecret()),
                        decrypt(userConnection.getRefreshToken()),
                        userConnection.getExpireTime());
                ConnectionFactory<?> connectionFactory = connectionFactoryLocator.getConnectionFactory(connectionData.getProviderId());
                return connectionFactory.createConnection(connectionData);
            }

            private String decrypt(String encryptedText) {
                return encryptedText != null ? textEncryptor.decrypt(encryptedText) : encryptedText;
            }
        };
    }
}