package exam.service.notification;

import exam.db.entity.User;
import exam.db.repository.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class NotificationServiceImpl implements NotificationService {
	private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);
	private static final int OTP_LENGTH = 6;

	@Autowired
	private UserRepository userRepo;

	@Override
	public String generateOTP() {
		Random random = new Random();
		StringBuilder otp = new StringBuilder();
		for (int i = 0; i < OTP_LENGTH; i++) {
			otp.append(random.nextInt(10));
		}
		return otp.toString();
	}

	@Override
	public void sendOTPEmail(String email, String otp) {
		log.info("OTP for {} is {}", email, otp);
	}

	@Override
	public void sendMailResetPwd(String email, String pwd) {
		log.info("Temporary password for {} is {}", email, pwd);
	}

	@Override
	public Boolean verifyOTP(String email, String otp) {
		User user = userRepo.findByEmailIgnoreCase(email);
		return user != null && user.getOtp() != null && user.getOtp().equals(otp);
	}

	@Override
	public void clearOTP(String email) {
		User user = userRepo.findByEmailIgnoreCase(email);
		if (user != null) {
			user.setOtp(null);
			userRepo.save(user);
		}
	}
}
