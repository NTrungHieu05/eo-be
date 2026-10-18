package exam.ultis.jwt;

import exam.db.enums.RoleEnum;
import exam.support.TestUsers;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtUtilsTest {

	private static final String SECRET = "unit-test-jwt-secret";

	private final JwtUtils jwtUtils = new JwtUtils();

	@BeforeEach
	void setUp() {
		ReflectionTestUtils.setField(jwtUtils, "jwtSecret", SECRET);
		ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", 60_000);
	}

	@Test
	void generatedTokenRoundTripsUsername() {
		UserDetails user = TestUsers.user("user-1", RoleEnum.ROLE_USER);

		String token = jwtUtils.generateJwtToken(user);

		assertThat(jwtUtils.validateJwtToken(token)).isTrue();
		assertThat(jwtUtils.getUserNameFromJwtToken(token)).isEqualTo("user-1@toeic.local");
	}

	@Test
	void invalidAndBlankTokensAreRejected() {
		assertThat(jwtUtils.validateJwtToken("not-a-jwt")).isFalse();
		assertThat(jwtUtils.validateJwtToken("")).isFalse();
		assertThat(jwtUtils.validateJwtToken(null)).isFalse();
	}

	@Test
	void expiredTokenFailsValidation() {
		ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", -1_000);
		String token = jwtUtils.generateJwtToken(TestUsers.user("user-1", RoleEnum.ROLE_USER));

		assertThat(jwtUtils.validateJwtToken(token)).isFalse();
		assertThatThrownBy(() -> jwtUtils.getUserNameFromJwtToken(token))
				.isInstanceOf(ExpiredJwtException.class);
	}
}
