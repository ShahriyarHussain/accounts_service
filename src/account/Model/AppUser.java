package account.Model;

import account.Validator.BreachedPasswordConstraint;
import account.Validator.LengthConstraint;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
@Entity
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @NotBlank(message = "Name cannot be null or blank")
    private String name;

    @NotBlank(message = "Lastname cannot be null or blank")
    private String lastname;

    @Pattern(regexp = ".*@acme.com$")
    @NotNull(message = "Email cannot be null")
    private String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotBlank(message = "Password cannot be null or blank")
    @BreachedPasswordConstraint
    @LengthConstraint
    private String password;

    @JsonIgnore
    private boolean isAccountNonLocked = true;

    @JsonIgnore
    private int failedLoginAttempts = 0;

    @ElementCollection(fetch = FetchType.EAGER)
    @JsonIgnore
    private List<String> roles = new ArrayList<>(4);

    public void addRole(String role) {
        roles.add(role);
    }

    public void removeRole(String role) {
        roles.remove(role);
    }

    public void modifyRole(String operation, String role) {
        if (operation.equals("GRANT")) {
            addRole(role);
        }
        if (operation.equals("REMOVE")) {
            removeRole(role);
        }
    }

    public List<String> getRoles() {
        roles.sort((String::compareToIgnoreCase));
        return roles;
    }
}
