package account.Util;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

import static java.util.Objects.requireNonNull;

public class RequestUtil {
    public static HttpServletRequest getRequest() {
        return ((ServletRequestAttributes) requireNonNull(RequestContextHolder.currentRequestAttributes()))
                .getRequest();
    }
}
