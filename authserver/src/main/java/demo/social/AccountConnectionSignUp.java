package demo.social;

import demo.model.SimpleAccount;
import demo.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionSignUp;
import org.springframework.social.connect.UserProfile;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/*
    Remove this bean if you want to ask for password or others things on social signup
 */
@Component
public class AccountConnectionSignUp implements ConnectionSignUp {

    private static final Logger LOG = LoggerFactory.getLogger(AccountConnectionSignUp.class);

    @Inject
    private AccountRepository accountRepository;

    @Override
    public String execute(Connection<?> connection) {
        UserProfile profile = connection.fetchUserProfile();
        if(!checkEmail(profile)) {
            return null;
        } else {
            return execute(connection, profile);
        }
    }

    private String execute(Connection<?> connection, UserProfile profile) {
        SimpleAccount account = new SimpleAccount();
        account.setEmail(profile.getEmail());
        account.setFirstName(profile.getFirstName());
        account.setLastName(profile.getLastName());
        account.setEnabled(true);
        mergeAccountIfAlreadyExists(account);
        SimpleAccount savedAccount = accountRepository.save(account);

        return savedAccount.getId().toString();
    }

    private boolean checkEmail(UserProfile profile) {
        if (profile.getEmail() == null) {
            LOG.warn("Email should not be null.");
            return false;
        } else {
            return true;
        }
    }

    private void mergeAccountIfAlreadyExists(SimpleAccount account) {
        SimpleAccount existingAccount = accountRepository.findByEmail(account.getEmail());
        if (existingAccount != null) {
            account.setId(existingAccount.getId());
            account.setPassword(existingAccount.getPassword());
            account.setAdmin(existingAccount.isAdmin());
        }
    }
}
