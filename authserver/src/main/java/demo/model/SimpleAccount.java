package demo.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.social.facebook.api.Account;

import java.util.Date;
import java.util.Optional;

@Document
@Data
public class SimpleAccount {

    @Id
    private String id;

    private String email;

    private String password;
    private String firstName;
    private String lastName;

    private boolean locked = true;
    private boolean enabled = false;

    private boolean admin = false;
    private Date lastConnection;
}
