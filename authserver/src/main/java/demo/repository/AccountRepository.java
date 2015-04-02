package demo.repository;

import demo.model.SimpleAccount;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AccountRepository extends MongoRepository <SimpleAccount, String>{

    SimpleAccount findByEmail(String email);
}
