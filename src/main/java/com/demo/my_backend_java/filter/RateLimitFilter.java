package com.demo.my_backend_java.filter;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.demo.my_backend_java.service.RateLimitingService;

import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RateLimitFilter extends OncePerRequestFilter{

    private final RateLimitingService rateLimitingService;

    public RateLimitFilter(RateLimitingService rateLimitingService) {
        this.rateLimitingService = rateLimitingService;
    }
    
    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response, 
        FilterChain filterChain) throws ServletException, IOException {

        String clientIp = getClientIp(request);

        Bucket tokenBucket  = rateLimitingService.resolveBucket(clientIp);

        var probe = tokenBucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            response.addHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
            filterChain.doFilter(request, response);
        } else {
            var waitForRefill = probe.getNanosToWaitForRefill()  / 1_000_000_000;
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.addHeader("X-Rate-Limit-Retry-After-Seconds", String.valueOf(waitForRefill));
            response.setContentType("application/json");

            String jsonRespond  = """
                {
                    "status": %s,
                    "error": "Too Many Request", 
                    "message": "Exhausted API Request Quota",
                    "retryAfterSecond": %s
                }
            """.formatted(HttpStatus.TOO_MANY_REQUESTS.value(), waitForRefill);

            response.getWriter().write(jsonRespond);
        }

    }
    
    //check for x-forward-for: get the user real ip behindn proxy   
    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");

        if(xfHeader == null || xfHeader.isEmpty()) {
            return request.getRemoteAddr();
        }  

        return xfHeader.split(",")[0].trim();  
    }
}
     