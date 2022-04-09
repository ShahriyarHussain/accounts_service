package account.Service;

import account.Model.AppUser;
import account.Model.AppUserAdminRepresentation;
import account.Model.LogEvents;
import account.Repository.AppUserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AppUserService {

    private static final Map<String, String> ROLES = Map.of(
            "USER", "ROLE_USER",
            "ADMINISTRATOR", "ROLE_ADMINISTRATOR",
            "ACCOUNTANT", "ROLE_ACCOUNTANT",
            "AUDITOR", "ROLE_AUDITOR"
    );
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final LogService logService;


    public AppUserService(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder, LogService logService) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.logService = logService;
    }

    public AppUserAdminRepresentation registerUser(AppUser appUser) throws ResponseStatusException {
        if (appUserRepository.existsAppUserByEmailIgnoreCase(appUser.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User exist!");
        }
        return new AppUserAdminRepresentation(appUserRepository.save(
                getUserModelWithAddedRoles(appUserRepository.findAll().isEmpty(), appUser)));
    }

    public Map<String, String> changePassword(String newPassword, String email) throws ResponseStatusException {
        AppUser appUser = findUserByEmail(email);
        savePassword(appUser, newPassword);
        return Map.of(
                "status", "The password has been updated successfully",
                "email", appUser.getEmail()
        );
    }

    public Map<String, String> deleteUserByEmail(String userEmail, String adminEmail) throws ResponseStatusException {
        AppUser appUser = appUserRepository.findAppUserByEmailIgnoreCase(userEmail).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found!"));
        if (appUser.getRoles().contains(ROLES.get("ADMINISTRATOR"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't remove ADMINISTRATOR role!");
        }
        appUserRepository.delete(appUser);
        logService.saveLog(LogEvents.DELETE_USER, adminEmail, userEmail, "/api/admin/delete");
        return Map.of(
                "user", userEmail,
                "status", "Deleted successfully!"
        );
    }

    public AppUserAdminRepresentation updateUserRole(Map<String, String> roleMap,
                                                     String adminEmail) throws ResponseStatusException {
        AppUser appUser = appUserRepository.findAppUserByEmailIgnoreCase(roleMap.get("user"))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found!"));

        logIfValidRoleUpdateRequest(appUser, roleMap, adminEmail);
        appUser.modifyRole(roleMap.get("operation"), ROLES.get(roleMap.get("role")));
        appUserRepository.save(appUser);
        return new AppUserAdminRepresentation(appUser);
    }

    public Map<String, String> lockOrUnlockUser(Map<String, String> operationMap, String adminEmail) {
        LogEvents event = null;
        AppUser appUser = findUserByEmail(operationMap.get("user"));
        if (appUser.getRoles().contains(ROLES.get("ADMINISTRATOR"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't lock the ADMINISTRATOR!");
        }
        if (!operationMap.get("operation").equals("LOCK") && !operationMap.get("operation").equals("UNLOCK")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Operation");
        }
        if (operationMap.get("operation").equals("LOCK")) {
            lockUser(appUser);
            event = LogEvents.LOCK_USER;
        }
        if (operationMap.get("operation").equals("UNLOCK")) {
            unlockUser(appUser);
            event = LogEvents.UNLOCK_USER;
        }

        logService.saveLog(event, adminEmail,
                String.format("%s user %s", StringUtils.capitalize(operationMap.get("operation").toLowerCase()),
                        appUser.getEmail()), "/api/admin/access");

        return Map.of("status", String.format("User %s %sed!", appUser.getEmail(),
                        operationMap.get("operation").toLowerCase()));
    }

    public AppUser findUserByEmail(String email) throws ResponseStatusException {
        return appUserRepository
                .findAppUserByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "User doesn't exist"));
    }

    public boolean isCombiningRoles(List<String> assignedRoles, String requestedRole) {
        return isCombiningAdminWithBusinessRole(assignedRoles, requestedRole) ||
                isCombiningBusinessRoleWithAdmin(assignedRoles, requestedRole);
    }

    public boolean isCombiningBusinessRoleWithAdmin(List<String> assignedRoles, String requestedRole) {
        return assignedRoles.contains(ROLES.get("ADMINISTRATOR")) &&
                (requestedRole.equals("ACCOUNTANT") ||
                        requestedRole.equals("USER") ||
                        requestedRole.equals("AUDITOR"));
    }

    public boolean isCombiningAdminWithBusinessRole(List<String> assignedRoles, String requestedRole) {
        return (assignedRoles.contains(ROLES.get("USER")) ||
                assignedRoles.contains(ROLES.get("ACCOUNTANT")) ||
                assignedRoles.contains(ROLES.get("AUDITOR")))
                && requestedRole.equals("ADMINISTRATOR");
    }

    public void lockUser(AppUser appUser) {
        appUser.setAccountNonLocked(false);
        appUserRepository.save(appUser);
    }

    public void unlockUser(AppUser appUser) {
        appUser.setAccountNonLocked(true);
        appUser.setFailedLoginAttempts(0);
        appUserRepository.save(appUser);
    }


    public void savePassword(AppUser appUser, String newPassword) throws ResponseStatusException {
        if (passwordEncoder.matches(newPassword, appUser.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The passwords must be different!");
        }
        appUser.setPassword(passwordEncoder.encode(newPassword));
        appUserRepository.save(appUser);
        logService.saveLog(LogEvents.CHANGE_PASSWORD, appUser.getEmail(), appUser.getEmail(), "/api/auth/changepass");
    }

    public AppUser getUserModelWithAddedRoles(boolean isFirstUser, AppUser appUser) {
        appUser.addRole(isFirstUser ? ROLES.get("ADMINISTRATOR") : ROLES.get("USER"));
        appUser.setEmail(appUser.getEmail().toLowerCase(Locale.ROOT));
        appUser.setPassword(passwordEncoder.encode(appUser.getPassword()));
        logService.saveLog(LogEvents.CREATE_USER, "Anonymous", appUser.getEmail(), "/api/auth/signup");
        return appUser;
    }

    public List<AppUserAdminRepresentation> getAllUsersAndInfo() {
        return appUserRepository
                .findAll()
                .stream()
                .map(AppUserAdminRepresentation::new)
                .collect(Collectors.toCollection(LinkedList::new));
    }

    public void logIfValidRoleUpdateRequest(AppUser appUser, Map<String, String> roleMap, String adminEmail) {
        String role = roleMap.get("role");
        String operation = roleMap.get("operation");
        String invalidReason = null;

        if (!ROLES.containsKey(role)) {
            invalidReason = "Role not found";
        }
        else if (operation.equals("GRANT") && isCombiningRoles(appUser.getRoles(), role)) {
            invalidReason = "The user cannot combine administrative and business roles!";
        }
        else if (operation.equals("GRANT") && appUser.getRoles().contains(ROLES.get(role))) {
            invalidReason = "User already has the role";
        }
        else if (operation.equals("REMOVE") && role.equals("ADMINISTRATOR")) {
            invalidReason = "Can't remove ADMINISTRATOR role!";
        }
        else if (operation.equals("REMOVE") && !appUser.getRoles().contains(ROLES.get(role))) {
            invalidReason = "The user does not have a role!";
        }
        else if (operation.equals("REMOVE") && appUser.getRoles().size() == 1) {
            invalidReason = "The user must have at least one role!";
        }
        if (invalidReason != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, invalidReason);
        }

        LogEvents event = null;
        String fromOrTo = null;
        if (operation.equals("GRANT")) {
            event = LogEvents.GRANT_ROLE;
            fromOrTo = "to";
        } else {
            event = LogEvents.REMOVE_ROLE;
            fromOrTo = "from";
        }

        logService.saveLog(event, adminEmail,
                String.format("%s role %s %s %s", StringUtils.capitalize(roleMap.get("operation").toLowerCase()),
                        roleMap.get("role"), fromOrTo, appUser.getEmail()),"/api/admin/role");
    }
}
