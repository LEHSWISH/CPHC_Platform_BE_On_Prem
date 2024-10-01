package org.wishfoundation.userservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

@Component
public class CorsFilter extends GenericFilterBean {

  private static final List<String> ALLOWED_IPS = Arrays.asList(
    "http://172.16.5.50",
    "http://172.16.5.53",
    "http://localhost",
    "http://172.16.5.50:*",
    "http://172.16.5.53:*",
    "http://localhost:*",
    "https://117.250.220.22:*",
    "https://eswasthyadham.uk.gov.in:*",
    "https://4.213.6.195:*",
    "http://74.225.175.10:*",
    "http://4.213.117.239:*",
	"http://localhost:5173",
	"http://127.0.0.1"
  );

  @Override
  public void doFilter(
    ServletRequest request,
    ServletResponse response,
    FilterChain filterChain
  ) throws IOException, ServletException {
    System.out.println("call in cors filter------------------------------");
    HttpServletRequest req = (HttpServletRequest) request;
    HttpServletResponse res = (HttpServletResponse) response;

    String requestOrigin = req.getHeader("Origin");
    if (requestOrigin != null && ALLOWED_IPS.contains(requestOrigin)) {
      res.setHeader("Access-Control-Allow-Origin", requestOrigin);
      res.setHeader(
        "Access-Control-Allow-Methods",
        "POST, PUT, GET, OPTIONS, DELETE, PATCH"
      );
      res.setHeader("Access-Control-Max-Age", "3600");
      res.setHeader("X-XSS-Protection", "1; mode=block");
      res.setHeader(
        "Access-Control-Allow-Headers",
        "X-CustomHeader,Keep-Alive,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,Authorization,origin,websiteOrigin,X-Organization-Id,x-session-id,x-internal-request"
      );
      if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
        res.setStatus(HttpServletResponse.SC_OK);
		return;
      } else {
        System.out.println(
          "going to do filter chain------------------------------"
        );
        filterChain.doFilter(request, response);
      }
    } else {
      //			// Return an error response for requests from disallowed origins
      //			res.setStatus(HttpServletResponse.SC_FORBIDDEN);
      //			res.getWriter().println("Access Forbidden");
      // DONE FOR INTERNAL REQUEST.
      res.setHeader("Access-Control-Allow-Origin", "*");
      filterChain.doFilter(request, response);
    }
  }
}
