package exam.config;

import exam.db.entity.User;
import exam.service.ExamUserDetailsService;
import exam.ultis.jwt.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

public class AuthenticationFilter extends OncePerRequestFilter {
	@Autowired
	private JwtUtils jwtUtils;

	@Autowired
	private ExamUserDetailsService examUserDetailsService;

	private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String token = getTokenFromRequest(request);
		if (StringUtils.hasText(token) && !jwtUtils.validateJwtToken(token)) {
			SecurityContextHolder.clearContext();
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}
		try {
			if (StringUtils.hasText(token)) {
				String username = jwtUtils.getUserNameFromJwtToken(token);
				UserDetails userDetails = examUserDetailsService.loadUserByUsername(username);
				if (!isActiveAccount(userDetails) || isRevoked(token, userDetails)) {
					SecurityContextHolder.clearContext();
					response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
					return;
				}
				UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
						userDetails, null, userDetails.getAuthorities());
				authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				SecurityContextHolder.getContext().setAuthentication(authentication);
			}
		} catch (Exception e) {
			logger.error("Cannot set user authentication: {}", e);
		}

		filterChain.doFilter(request, response);
	}

	private boolean isRevoked(String token, UserDetails userDetails) {
		if (!(userDetails instanceof User)) {
			return false;
		}
		Date validAfter = ((User) userDetails).getTokensValidAfter();
		if (validAfter == null) {
			return false;
		}
		io.jsonwebtoken.Claims claims = jwtUtils.getClaimsFromJwtToken(token);
		Object issuedMs = claims.get(JwtUtils.ISSUED_AT_MS);
		if (issuedMs instanceof Number) {
			// Token mới có mốc mili giây: token cấp trước hoặc cùng lúc đăng xuất đều bị từ chối.
			return ((Number) issuedMs).longValue() <= validAfter.getTime();
		}
		// Token cũ chỉ có iat theo giây.
		Date issuedAt = claims.getIssuedAt();
		return issuedAt == null || issuedAt.getTime() / 1000 <= validAfter.getTime() / 1000;
	}

	private boolean isActiveAccount(UserDetails userDetails) {
		if (userDetails == null || !userDetails.isEnabled() || !userDetails.isAccountNonLocked()) {
			return false;
		}
		return !(userDetails instanceof User) || !((User) userDetails).isDeleted();
	}

	private String getTokenFromRequest(HttpServletRequest request) {
		String token = request.getHeader("Authorization");

		if (StringUtils.hasText(token) && token.startsWith("Bearer ")) {
			return token.substring(7, token.length());
		}

		return null;
	}
}
