package exam.service.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

	@InjectMocks
	private NotificationServiceImpl notificationService;

	@Test
	void resetMailDoesNotThrowWhenMailIsNotConfigured() {
		notificationService.sendMailResetPwd("user@toeic.local", "abcd1234");
	}
}
