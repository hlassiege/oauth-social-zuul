package demo;

import java.io.IOException;
import java.security.KeyPair;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import demo.oauth2.MongoDBTokenStore;
import demo.oauth2.OAuth2AccessTokenRepository;
import demo.oauth2.OAuth2AuthenticationReadConverter;
import demo.oauth2.OAuth2RefreshTokenRepository;
import demo.security.DefaultUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.convert.CustomConversions;
import org.springframework.data.mongodb.core.convert.DbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.RememberMeAuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.authentication.encoding.MessageDigestPasswordEncoder;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.authentication.BearerTokenExtractor;
import org.springframework.security.oauth2.provider.authentication.TokenExtractor;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.authentication.rememberme.RememberMeAuthenticationFilter;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
@EnableAutoConfiguration
@ComponentScan
@EnableMongoRepositories
@Controller
@SessionAttributes("authorizationRequest")
public class AuthserverApplication extends WebMvcConfigurerAdapter {


    @Bean
    public TextEncryptor textEncryptor() {
        return Encryptors.noOpText();
    }


    @Inject
    private OAuth2AccessTokenRepository oAuth2AccessTokenRepository;

    @Inject
    private OAuth2RefreshTokenRepository oAuth2RefreshTokenRepository;

    @Bean
    public TokenStore tokenStore() {
        return new MongoDBTokenStore(oAuth2AccessTokenRepository, oAuth2RefreshTokenRepository);
    }

    @RequestMapping("/user")
    @ResponseBody
    public Principal user(Principal user) {
        return user;
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/login").setViewName("login");
        registry.addViewController("/oauth/confirm_access").setViewName("authorize");
    }

    public static void main(String[] args) {
        SpringApplication.run(AuthserverApplication.class, args);
    }

    @Configuration
    @Order(-10)
    protected static class LoginConfig extends WebSecurityConfigurerAdapter {

        public final static String REMEMBER_KEY = "hopwork rocks";

        @Autowired
        private AuthenticationManager authenticationManager;

        @Autowired
        private DefaultUserDetailsService userDetailsService;

        @Override
        protected UserDetailsService userDetailsService() {
            return userDetailsService;
        }

        @Bean
        public RememberMeAuthenticationFilter rememberMeFilter() {
            return new RememberMeAuthenticationFilter(authenticationManager, rememberMeServices());
        }

        @Bean
        public RememberMeServices rememberMeServices() {
            TokenBasedRememberMeServices tokenBasedRememberMeServices = new TokenBasedRememberMeServices(REMEMBER_KEY, userDetailsService);
            tokenBasedRememberMeServices.setAlwaysRemember(true);
            tokenBasedRememberMeServices.setUseSecureCookie(false);
            return tokenBasedRememberMeServices;
        }

        @Bean
        public RememberMeAuthenticationProvider rememberMeAuthenticationProvider() {
            return new RememberMeAuthenticationProvider(REMEMBER_KEY);
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            // @formatter:off
            http
                    .formLogin().loginPage("/login").permitAll()
                    .and()
                        .requestMatchers().antMatchers("/signin/google", "/signin/facebook", "/login", "/oauth/authorize", "/oauth/confirm_access")
                    .and()
                        .authorizeRequests().antMatchers("/oauth/token", "/signin/google", "/signin/facebook").permitAll()
                    .and()
                        .authorizeRequests().anyRequest().authenticated()
                    .and()
                    .csrf().disable();
            // @formatter:on
        }

        @Bean
        public DaoAuthenticationProvider daoAuthenticationProvider() {
            DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
            daoAuthenticationProvider.setUserDetailsService(userDetailsService);
            daoAuthenticationProvider.setSaltSource(userDetailsService);
            daoAuthenticationProvider.setPasswordEncoder(new MessageDigestPasswordEncoder("sha-256"));
            return daoAuthenticationProvider;
        }

        @Override
        protected void configure(AuthenticationManagerBuilder auth) throws Exception {
            auth.authenticationProvider(daoAuthenticationProvider());
            auth.parentAuthenticationManager(authenticationManager);
        }
    }

    @Configuration
    @EnableResourceServer
    protected static class OAuth2ResourceConfig extends ResourceServerConfigurerAdapter {

        @Autowired
        private TokenStore tokenStore;

        @Override
        public void configure(HttpSecurity http) throws Exception {
            http
                .csrf()
                .requireCsrfProtectionMatcher(new AntPathRequestMatcher("/oauth/authorize"))
                .disable()
                .headers()
                .frameOptions().disable()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.NEVER)
            .and()
                    .authorizeRequests()
                        .anyRequest().authenticated();
        }

        @Override
        public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
            resources.tokenStore(tokenStore)
            ;
        }
    }

    @Configuration
    @EnableAuthorizationServer
    protected static class OAuth2AuthorizationConfig extends
            AuthorizationServerConfigurerAdapter {

        @Autowired
        private AuthenticationManager authenticationManager;

        @Autowired
        private TokenStore tokenStore;

        @Override
        public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
            clients.inMemory()
                    .withClient("acme")
                    .secret("acmesecret")
                    .authorizedGrantTypes("password", "authorization_code", "refresh_token", "implicit")
                    .scopes("openid")
                    .autoApprove(true)
            ;
        }

        @Override
        public void configure(AuthorizationServerEndpointsConfigurer endpoints)
                throws Exception {
            endpoints
                    .tokenStore(tokenStore)
                    .authenticationManager(authenticationManager)
                    .approvalStoreDisabled()
            ;
        }

    }
}
