package exam.ultis.jwt;

import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@PropertySource({"classpath:${spring.profiles.active:dev}_jwt.properties"})
public class JwtUtils {
	private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

	public static final String ISSUED_AT_MS = "iatMs";

	@Value("${jwt.app.jwtSecret}")
	private String jwtSecret;

	@Value("${jwt.app.jwtExpirationMs}")
	private int jwtExpirationMs;

	public String generateJwtToken(UserDetails userDetails) {
		Date now = new Date();
		return Jwts.builder()
				.setSubject((userDetails.getUsername()))
				// iat của JWT chỉ tính theo giây; iatMs giúp thu hồi đúng token cấp cùng giây với lúc đăng xuất.
				.claim(ISSUED_AT_MS, now.getTime())
				.setIssuedAt(now)
				.setExpiration(generateExpirationDate())
				.signWith(SignatureAlgorithm.HS512, jwtSecret)
				.compact();
	}

	public Date generateExpirationDate() {
		return new Date(System.currentTimeMillis() + jwtExpirationMs);
	}

	public Claims getClaimsFromJwtToken(String token) {
		return Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody();
	}

	private boolean isTokenExpired(Claims claims) {
		return claims.getExpiration().before(new Date());
	}

	public String getUserNameFromJwtToken(String token) {
		Claims claims = getClaimsFromJwtToken(token);
		if (claims != null && !isTokenExpired(claims)) {
			return claims.getSubject();
		}
		return null;
	}

	public boolean validateJwtToken(String authToken) {
		try {
			Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(authToken);
			return true;
		} catch (SignatureException e) {
			logger.error("Invalid JWT signature: {}", e.getMessage());
		} catch (MalformedJwtException e) {
			logger.error("Invalid JWT token: {}", e.getMessage());
		} catch (ExpiredJwtException e) {
			logger.error("JWT token is expired: {}", e.getMessage());
		} catch (UnsupportedJwtException e) {
			logger.error("JWT token is unsupported: {}", e.getMessage());
		} catch (IllegalArgumentException e) {
			logger.error("JWT claims string is empty: {}", e.getMessage());
		}

		return false;
	}
}
