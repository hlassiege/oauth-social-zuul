package demo.security;

import demo.model.SimpleAccount;
import demo.repository.AccountRepository;
import org.springframework.security.authentication.dao.SaltSource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service("userDetailsService")
public class DefaultUserDetailsService implements UserDetailsService, SaltSource {

    private AccountRepository service;

    @Inject
    public DefaultUserDetailsService(AccountRepository service) {
        this.service = service;
    }

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        SimpleAccount account = service.findByEmail(email);
        if (account == null) {
            throw new UsernameNotFoundException("No account exists with this _id : " + email);
        }
        return new AccountWrapper(account);
    }

    @Override
    public Object getSalt(UserDetails user) {
        if (user instanceof AccountWrapper) {
            return ((AccountWrapper) user).getEmail();
        } else throw new IllegalArgumentException("Must be an AccountWrapper");
    }
}
