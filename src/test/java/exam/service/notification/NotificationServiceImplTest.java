package exam.service.notification;

import exam.db.entity.User;
import exam.db.enums.RoleEnum;
import exam.db.repository.user.UserRepository;
import exam.support.TestUsers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

	@Mock
	private UserRepository userRepo;

	@InjectMocks
	private NotificationServiceImpl notificationService;

	@Test
	void generateOtpIsSixDigits() {
		String otp = notificationService.generateOTP();

		assertThat(otp).hasSize(6).matches("\\d{6}");
	}

	@Test
	void verifyOtpMatchesStoredCodeIgnoringEmailCase() {
		User user = TestUsers.user("user-1", RoleEnum.ROLE_USER);
		user.setOtp("123456");
		when(userRepo.findByEmailIgnoreCase("user-1@toeic.local")).thenReturn(user);

		assertThat(notificationService.verifyOTP("user-1@toeic.local", "123456")).isTrue();
		assertThat(notificationService.verifyOTP("user-1@toeic.local", "000000")).isFalse();
	}

	@Test
	void verifyOtpFailsWhenUserMissing() {
		when(userRepo.findByEmailIgnoreCase("missing@toeic.local")).thenReturn(null);

		assertThat(notificationService.verifyOTP("missing@toeic.local", "123456")).isFalse();
	}

	@Test
	void clearOtpRemovesCode() {
		User user = TestUsers.user("user-1", RoleEnum.ROLE_USER);
		user.setOtp("123456");
		when(userRepo.findByEmailIgnoreCase("user-1@toeic.local")).thenReturn(user);

		notificationService.clearOTP("user-1@toeic.local");

		assertThat(user.getOtp()).isNull();
		verify(userRepo).save(user);
	}

	@Test
	void clearOtpIgnoresUnknownEmail() {
		when(userRepo.findByEmailIgnoreCase("missing@toeic.local")).thenReturn(null);

		notificationService.clearOTP("missing@toeic.local");

		verify(userRepo, never()).save(org.mockito.ArgumentMatchers.any());
	}
}
