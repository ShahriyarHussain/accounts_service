package account.Service;

import account.Model.AppUser;
import account.Model.AppUserDetails;
import account.Model.LogEvents;
import account.Repository.AppUserRepository;
import account.Util.RequestUtil;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@Service
public class AppUserDetailsService implements UserDetailsService {

    private final AppUserRepository appUserRepository;
    private final LogService logService;

    public AppUserDetailsService(AppUserRepository appUserRepository, LogService logService) {
        this.appUserRepository = appUserRepository;
        this.logService = logService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<AppUser> appUser = appUserRepository.findAppUserByEmailIgnoreCase(username);
        return appUser.map(AppUserDetails::new)
                .orElseThrow(() -> {
                    HttpServletRequest request = RequestUtil.getRequest();
                    logService.saveLog(LogEvents.LOGIN_FAILED, username,
                            request.getRequestURI(), request.getRequestURI());
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User doesn't exist");
                });
    }
}
