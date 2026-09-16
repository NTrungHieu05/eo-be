package exam.service.notification;

import exam.ultis.ExamBaseException;

public interface NotificationService {
	String generateOTP();

	void sendOTPEmail(String email, String otp);

	void sendMailResetPwd(String email, String pwd);

	Boolean verifyOTP(String email, String otp);

	void clearOTP(String email);
}
