package account.Controller;

import account.Model.AppUser;
import account.Model.AppUserAdminRepresentation;
import account.Model.NewPassword;
import account.Service.AppUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AppUserService appUserService;

    public AuthController(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    @PostMapping("/signup")
    public AppUserAdminRepresentation registerUser(@RequestBody @Valid AppUser appUser) {
        return appUserService.registerUser(appUser);
    }

    @PostMapping("/changepass")
    public ResponseEntity<Map<String, String>> changePassword(@RequestBody @Valid NewPassword newPassword,
                                                              @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok().body(
                appUserService.changePassword(newPassword.getNew_password(), userDetails.getUsername()));
    }
}
