package exam.db.changelog;

import lombok.extern.slf4j.Slf4j;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Mật khẩu cho tài khoản mẫu lấy từ biến môi trường, không viết cứng trong code.
 * Thiếu biến thì sinh ngẫu nhiên và chỉ ghi vào log server lúc nạp dữ liệu lần đầu.
 */
@Slf4j
final class SeedPasswords {

	static final String ADMIN_ENV = "SEED_ADMIN_PASSWORD";
	static final String USER_ENV = "SEED_USER_PASSWORD";

	private static final SecureRandom RANDOM = new SecureRandom();

	private SeedPasswords() {
	}

	/** Giá trị biến môi trường, hoặc null nếu chưa đặt. */
	static String fromEnv(String name) {
		String value = System.getenv(name);
		return value == null || value.trim().isEmpty() ? null : value.trim();
	}

	/** Dùng khi tạo tài khoản mới: lấy từ biến môi trường, thiếu thì sinh ngẫu nhiên. */
	static String forNewAccount(String envName, String email) {
		String value = fromEnv(envName);
		if (value != null) {
			return value;
		}
		byte[] bytes = new byte[12];
		RANDOM.nextBytes(bytes);
		String generated = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
		log.warn("Chưa đặt {}. Mật khẩu khởi tạo của {} là: {} (chỉ hiện một lần, hãy đổi sau khi đăng nhập)",
				envName, email, generated);
		return generated;
	}
}
