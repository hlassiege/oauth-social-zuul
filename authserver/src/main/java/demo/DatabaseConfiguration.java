package demo;


import com.mongodb.DB;
import com.mongodb.Mongo;
import demo.oauth2.OAuth2AuthenticationReadConverter;
import org.jongo.Jongo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.core.convert.CustomConversions;
import org.springframework.data.mongodb.core.mapping.event.ValidatingMongoEventListener;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import javax.inject.Inject;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

@Configuration
@Import(value = MongoAutoConfiguration.class)
public class DatabaseConfiguration extends AbstractMongoConfiguration  {

    @Inject
    private Mongo mongo;

    @Value("${spring.data.mongodb.database}")
    private String databaseName;

    @Inject
    private MongoProperties mongoProperties;

    @Bean
    public ValidatingMongoEventListener validatingMongoEventListener() {
        return new ValidatingMongoEventListener(validator());
    }

    @Bean
    public LocalValidatorFactoryBean validator() {
        return new LocalValidatorFactoryBean();
    }

    @Override
    protected String getDatabaseName() {
        return mongoProperties.getDatabase();
    }

    @Override
    public Mongo mongo() throws Exception {
        return mongo;
    }

    @Bean
    public Jongo jongo(Mongo mongo) throws UnknownHostException {
        return new Jongo(mongo.getDB(databaseName));
    }

    @Bean
    public CustomConversions customConversions() {
        List<Converter<?, ?>> converterList = new ArrayList<>();
        OAuth2AuthenticationReadConverter converter = new OAuth2AuthenticationReadConverter();
        converterList.add(converter);
        return new CustomConversions(converterList);
    }
}
