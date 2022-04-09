package account.Config;

import account.Model.LogEvents;
import account.Service.LogService;
import account.Service.LoginAttemptService;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;

import javax.servlet.http.HttpServletRequest;

@Configuration
public class AuthenticationBadCredentialListener implements ApplicationListener<AuthenticationFailureBadCredentialsEvent> {

    private final LoginAttemptService loginAttemptService;
    private final LogService logService;
    private final HttpServletRequest request;


    public AuthenticationBadCredentialListener(LoginAttemptService loginAttemptService, LogService logService,
                                               HttpServletRequest request) {
        this.loginAttemptService = loginAttemptService;
        this.logService = logService;
        this.request = request;
    }

    @Override
    public void onApplicationEvent(AuthenticationFailureBadCredentialsEvent event) {
        logService.saveLog(LogEvents.LOGIN_FAILED, event.getAuthentication().getName(),
                request.getRequestURI(), request.getRequestURI());
        final String username = event.getAuthentication().getName();
        if (username != null) {
            loginAttemptService.loginFailure(username, request.getRequestURI());
        }
    }
}
