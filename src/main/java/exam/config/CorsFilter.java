package exam.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorsFilter implements Filter {

    @Value("${access.control.allow.origin}")
    private String origins;
    @Value("${access.control.allow.methods}")
    private String methods;
    @Value("${access.control.allow.headers}")
    private String headers;
    @Value("${access.control.max.age}")
    private String maxAge;

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        final HttpServletResponse response = (HttpServletResponse) res;
        response.setHeader("Access-Control-Allow-Origin", origins);
        response.setHeader("Access-Control-Allow-Methods", methods);
        response.setHeader("Access-Control-Allow-Headers", headers);
        response.setHeader("Access-Control-Max-Age", maxAge);
        if (HttpMethod.OPTIONS.name().equalsIgnoreCase(((HttpServletRequest) req).getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            chain.doFilter(req, res);
        }
    }

    @Override
    public void destroy() {
        // Do nothing because of X and Y.
    }

    @Override
    public void init(FilterConfig config) throws ServletException {
        // Do nothing because of X and Y.
    }
}
