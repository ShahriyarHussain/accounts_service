package account.Service;

import account.Model.AppUser;
import account.Model.LogEvents;
import account.Repository.AppUserRepository;
import org.springframework.stereotype.Service;

@Service
public class LoginAttemptService {

    private final static int MAX_ATTEMPT = 4;
    private final AppUserRepository appUserRepository;
    private final LogService logService;

    public LoginAttemptService(AppUserRepository appUserRepository, LogService logService) {
        super();
        this.appUserRepository = appUserRepository;
        this.logService = logService;
    }

    public void loginSuccess(String key) {
        AppUser appUser = appUserRepository.findAppUserByEmailIgnoreCase(key).orElseGet(AppUser::new);
        appUser.setFailedLoginAttempts(0);
        appUserRepository.save(appUser);
    }

    public void loginFailure(String key, String uri) {
        if (appUserRepository.findAppUserByEmailIgnoreCase(key).isEmpty()) {
            return;
        }
        AppUser appUser = appUserRepository.findAppUserByEmailIgnoreCase(key).get();
        if (appUser.getRoles().contains("ROLE_ADMINISTRATOR")) {
            return;
        }
        appUser.setFailedLoginAttempts(appUser.getFailedLoginAttempts() + 1);

        if (appUser.getFailedLoginAttempts() > MAX_ATTEMPT) {
            appUser.setAccountNonLocked(false);
            logService.saveLog(LogEvents.BRUTE_FORCE, key, uri, uri);
            logService.saveLog(LogEvents.LOCK_USER, key, String.format("Lock user %s", key), uri);
        }
        appUserRepository.save(appUser);
    }
}
