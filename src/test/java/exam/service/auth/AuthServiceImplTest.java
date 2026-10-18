package exam.service.auth;

import exam.db.dto.LoginRequest;
import exam.db.dto.LoginResponse;
import exam.db.entity.User;
import exam.db.enums.ErrorInfo;
import exam.db.enums.RoleEnum;
import exam.support.TestUsers;
import exam.ultis.ExamBaseException;
import exam.ultis.jwt.JwtUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

	@Mock
	private AuthenticationManager authenticationManager;

	@Mock
	private JwtUtils jwtUtils;

	@InjectMocks
	private AuthServiceImpl authService;

	@AfterEach
	void tearDown() {
		TestUsers.clear();
	}

	@Test
	void loginReturnsTokenWhenAccountIsVerified() throws ExamBaseException {
		User user = TestUsers.user("user-1", RoleEnum.ROLE_USER);
		Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
		when(authenticationManager.authenticate(any())).thenReturn(authentication);
		when(jwtUtils.generateJwtToken(user)).thenReturn("jwt-token");

		LoginResponse response = authService.login(loginRequest());

		assertThat(response.getAccess_token()).isEqualTo("jwt-token");
		assertThat(response.getExamUser().getEmail()).isEqualTo("user-1@toeic.local");
		assertThat(response.getExamUser().isUser()).isTrue();
		assertThat(SecurityContextHolder.getContext().getAuthentication()).isSameAs(authentication);
	}

	@Test
	void loginRejectsAccountThatStillHasOtp() {
		User user = TestUsers.user("user-1", RoleEnum.ROLE_USER);
		user.setOtp("123456");
		when(authenticationManager.authenticate(any()))
				.thenReturn(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));

		assertThatThrownBy(() -> authService.login(loginRequest()))
				.isInstanceOf(ExamBaseException.class)
				.extracting(ex -> ((ExamBaseException) ex).getError().getCode())
				.isEqualTo(ErrorInfo.PLEASE_VERIFY_OTP_CODE);
	}

	@Test
	void loginMapsBadCredentialsToPasswordError() {
		when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));

		assertThatThrownBy(() -> authService.login(loginRequest()))
				.isInstanceOf(ExamBaseException.class)
				.extracting(ex -> ((ExamBaseException) ex).getError().getCode())
				.isEqualTo(ErrorInfo.INCORRECT_PASSWORD_ERROR_CODE);
	}

	@Test
	void logoutClearsSecurityContext() {
		TestUsers.authenticate(TestUsers.user("user-1", RoleEnum.ROLE_USER));

		authService.logout("Bearer token");

		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
	}

	private LoginRequest loginRequest() {
		LoginRequest request = new LoginRequest();
		request.setEmail("user-1@toeic.local");
		request.setPassword("User@123");
		return request;
	}
}
