package demo.security;

import demo.model.SimpleAccount;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import sun.java2d.pipe.SpanShapeRenderer;

import java.util.Collection;
import java.util.LinkedHashSet;


public class AccountWrapper implements UserDetails {

    private static final long serialVersionUID = 1547323806777632528L;
    private Collection<GrantedAuthority> authorities = new LinkedHashSet<>();
    private SimpleAccount account;

    AccountWrapper() {
    }

    public AccountWrapper(SimpleAccount account) {
        this.account = account;
        computeAuthorities(account);
    }

    private void computeAuthorities(SimpleAccount account) {
        authorities.add(SecurityUtils.ROLE_USER);
        if (account !=null && account.isAdmin()) {
            authorities.add(SecurityUtils.ROLE_ADMIN);
        }
    }

    public SimpleAccount getAccount() {
        return account;
    }

    @Override
    public Collection<GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return account.getPassword();
    }

    @Override
    public String getUsername() {
        return account.getEmail().toLowerCase();
    }

    public String getForename() {
        return account.getFirstName();
    }

    public String getSurname() {
        return account.getLastName();
    }

    public String getEmail() {
        return account.getEmail().toLowerCase();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}
