package account.Controller;

import account.Model.AppUserAdminRepresentation;
import account.Service.AppUserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AppUserService appUserService;

    public AdminController(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    @GetMapping("/user")
    public List<AppUserAdminRepresentation> showAllUserInfo() {
        return appUserService.getAllUsersAndInfo();
    }

    @DeleteMapping("/user/{email}")
    public Map<String, String> deleteUser(@PathVariable String email, @AuthenticationPrincipal UserDetails userDetails) {
        return appUserService.deleteUserByEmail(email, userDetails.getUsername());
    }

    @PutMapping("/user/role")
    public AppUserAdminRepresentation updateUserRole(@RequestBody Map<String, String> roleMap,
                                                     @AuthenticationPrincipal UserDetails userDetails) {
        return appUserService.updateUserRole(roleMap, userDetails.getUsername());
    }

    @PutMapping("/user/access")
    public Map<String, String> restrictUserAccess(@RequestBody Map<String, String> operationMap,
                                                  @AuthenticationPrincipal UserDetails userDetails) {
        return appUserService.lockOrUnlockUser(operationMap, userDetails.getUsername());
    }

}
