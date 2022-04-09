package account.Model;

import account.Validator.BreachedPasswordConstraint;
import account.Validator.LengthConstraint;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@RequiredArgsConstructor
public class NewPassword {

    @NotBlank(message = "Password cannot be null or blank")
    @LengthConstraint
    @BreachedPasswordConstraint
    private String new_password;
}
