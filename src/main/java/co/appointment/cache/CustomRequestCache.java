package co.appointment.cache;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

/**
 * Helpful <a href="https://medium.com/@kasunharitha55/unlocking-secure-authentication-building-an-oauth2-flow-with-spring-boot-angular-pkce-167bae9a26df">blog</a> on overcoming common pitfalls
 */
@Component
@Slf4j
public class CustomRequestCache extends HttpSessionRequestCache {

    //Overcoming Common Pitfalls
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final List<String> ignoredPatterns = List.of(
            "/.well-known/appspecific/**",
            "/error/**",
            "/default-ui.css",
            "/favicon.ico",
            "/favicon.ico*",
            "/assets*"
    );
    // Helper to check if a URI matches any of our ignored patterns
    private boolean matchesIgnoredPattern(final String uri) {
        for(String pattern : ignoredPatterns) {
            if(pathMatcher.match(pattern, uri)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void saveRequest(HttpServletRequest request, HttpServletResponse response) {
        String requestURI =  request.getRequestURI();
        //If the request URI matches an ignored pattern, don't save it!
        if(matchesIgnoredPattern(requestURI)) {
            return;
        }
        super.saveRequest(request, response);
    }

    @Override
    public SavedRequest getRequest(HttpServletRequest currentRequest, HttpServletResponse response) {
        SavedRequest savedRequest =  super.getRequest(currentRequest, response);
        if(savedRequest != null) {
            try {
                String fullUrl = savedRequest.getRedirectUrl();
                URI uri = new URI(fullUrl);
                String savedRequestPath = uri.getPath();

                // If the saved request path matches an ignored pattern, remove it from the cache
                // and return null, effectively ignoring it.
                if(matchesIgnoredPattern(savedRequestPath)) {
                    removeRequest(currentRequest, response);
                    return null;
                }
            } catch (URISyntaxException exception) {
                log.error(exception.getMessage(), exception);
            }
        }
        return savedRequest;
    }
}
