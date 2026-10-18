package exam.service;

import exam.db.entity.Role;
import exam.db.entity.User;
import exam.db.enums.ErrorInfo;
import exam.db.enums.RoleEnum;
import exam.db.repository.user.UserRepository;
import exam.support.TestUsers;
import exam.ultis.ExamBaseException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExamUserDetailsServiceTest {

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private ExamUserDetailsService userDetailsService;

	@Test
	void loadUserByUsernameReturnsStoredUser() {
		User user = TestUsers.user("user-1", RoleEnum.ROLE_USER);
		when(userRepository.findByUserNameOrEmail("user-1@toeic.local")).thenReturn(Optional.of(user));

		assertThat(userDetailsService.loadUserByUsername("user-1@toeic.local")).isSameAs(user);
	}

	@Test
	void loadUserByUsernameRejectsUnknownAccount() {
		when(userRepository.findByUserNameOrEmail("missing@toeic.local")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> userDetailsService.loadUserByUsername("missing@toeic.local"))
				.isInstanceOf(UsernameNotFoundException.class);
	}

	@Test
	void validateUserDetailsRejectsBlankEmail() {
		User user = TestUsers.user("user-1", RoleEnum.ROLE_USER);
		user.setEmail(" ");

		assertThatThrownBy(() -> userDetailsService.validateUserDetails(user))
				.isInstanceOf(ExamBaseException.class)
				.extracting(ex -> ((ExamBaseException) ex).getError().getCode())
				.isEqualTo(ErrorInfo.EMAIL_IS_EMPTY_OR_NULL_ERROR_CODE);
	}

	@Test
	void validateUserDetailsRejectsMissingRole() {
		User user = new User();
		user.setEmail("user@toeic.local");

		assertThatThrownBy(() -> userDetailsService.validateUserDetails(user))
				.isInstanceOf(ExamBaseException.class)
				.extracting(ex -> ((ExamBaseException) ex).getError().getCode())
				.isEqualTo(ErrorInfo.AUTHORITIES_IS_NULL_ERROR_CODE);
	}

	@Test
	void validateUserDetailsRejectsBlankAuthority() {
		User user = new User();
		user.setEmail("user@toeic.local");
		user.getRoles().add(new Role(" "));

		assertThatThrownBy(() -> userDetailsService.validateUserDetails(user))
				.isInstanceOf(ExamBaseException.class)
				.extracting(ex -> ((ExamBaseException) ex).getError().getCode())
				.isEqualTo(ErrorInfo.AUTHORITY_CODE_IS_NULL_ERROR_CODE);
	}
}
