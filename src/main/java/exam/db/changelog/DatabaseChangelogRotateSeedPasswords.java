package exam.db.changelog;

import com.github.cloudyrock.mongock.ChangeLog;
import com.github.cloudyrock.mongock.ChangeSet;
import exam.db.entity.User;
import exam.db.repository.user.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Date;

/**
 * Các bản cũ nạp tài khoản mẫu với mật khẩu viết cứng trong repo công khai.
 * Changelog này đổi sang SEED_ADMIN_PASSWORD / SEED_USER_PASSWORD trên database đang chạy,
 * nhưng chỉ khi tài khoản vẫn còn dùng mật khẩu cũ và biến môi trường đã được đặt.
 */
@ChangeLog(order = "011")
@Slf4j
public class DatabaseChangelogRotateSeedPasswords {

	// Mật khẩu cũ đã công khai trên GitHub; chỉ dùng để nhận ra tài khoản chưa đổi.
	private static final String LEGACY_ADMIN_PASSWORD = "Admin@123";
	private static final String LEGACY_USER_PASSWORD = "User@123";

	@ChangeSet(id = "rotate_seed_passwords", author = "system", order = "001")
	public void rotate(UserRepository userRepo) {
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		rotate(userRepo, encoder, DatabaseChangelogAdmin.ADMIN_EMAIL, LEGACY_ADMIN_PASSWORD, SeedPasswords.ADMIN_ENV);
		rotate(userRepo, encoder, DatabaseChangelogSample.SAMPLE_USER_EMAIL, LEGACY_USER_PASSWORD, SeedPasswords.USER_ENV);
	}

	private void rotate(UserRepository userRepo, BCryptPasswordEncoder encoder, String email, String legacy, String envName) {
		User user = userRepo.findByEmailIgnoreCase(email);
		if (user == null || user.getPassword() == null || !encoder.matches(legacy, user.getPassword())) {
			return;
		}
		String next = SeedPasswords.fromEnv(envName);
		if (next == null) {
			log.warn("{} vẫn dùng mật khẩu mẫu công khai. Đặt {} rồi chạy lại, hoặc đổi mật khẩu trong trang Hồ sơ.",
					email, envName);
			return;
		}
		user.setPassword(encoder.encode(next));
		// Token cấp bằng mật khẩu cũ không còn dùng được.
		user.setTokensValidAfter(new Date());
		userRepo.save(user);
		log.info("Đã đổi mật khẩu mẫu của {} theo {}", email, envName);
	}
}
