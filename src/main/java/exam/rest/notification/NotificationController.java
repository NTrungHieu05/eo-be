package exam.rest.notification;

import exam.rest.endpoint.Endpoints;
import exam.service.notification.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(Endpoints.NOTIFICATION)
public class NotificationController {

	@Autowired
	private NotificationService notificationService;

	@PostMapping(Endpoints.VERIFY_OTP)
	public Boolean verifyOTP(@RequestParam String email, @RequestParam String otp) {
		if (Boolean.TRUE.equals(notificationService.verifyOTP(email, otp))) {
			notificationService.clearOTP(email);
			return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}
}
