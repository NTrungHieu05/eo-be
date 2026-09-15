package exam.db.changelog;

import com.github.cloudyrock.mongock.ChangeLog;
import com.github.cloudyrock.mongock.ChangeSet;
import exam.db.entity.Role;
import exam.db.entity.User;
import exam.db.enums.RoleEnum;
import exam.db.repository.role.RoleRepository;
import exam.db.repository.user.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@ChangeLog(order = "004")
@Slf4j
public class DatabaseChangelogAdmin {

	public static final String ADMIN_EMAIL = "admin@toeic.local";
	public static final String ADMIN_PASSWORD = "Admin@123";

	@ChangeSet(id = "admin_default", author = "system", order = "001")
	public void seedAdmin(UserRepository userRepo, RoleRepository roleRepo) {
		if (userRepo.findByEmailIgnoreCase(ADMIN_EMAIL) != null) {
			log.info("Admin user already exists, skip seed");
			return;
		}

		Role adminRole = roleRepo.findByName(RoleEnum.ROLE_ADMIN.getName());
		if (adminRole == null) {
			throw new IllegalStateException("ROLE_ADMIN must be seeded before admin user");
		}

		User admin = new User();
		admin.setEmail(ADMIN_EMAIL);
		admin.setUsername(ADMIN_EMAIL);
		admin.setPassword(new BCryptPasswordEncoder().encode(ADMIN_PASSWORD));
		admin.setDisplayName("Admin");
		admin.setFirstName("Admin");
		admin.setLastName("TOEIC");
		admin.setEnabled(true);
		admin.setAccountNonExpired(true);
		admin.setAccountNonLocked(true);
		admin.setCredentialsNonExpired(true);
		admin.setOtp(null);
		admin.getRoles().add(adminRole);
		userRepo.save(admin);
		log.info("Seeded admin user {}", ADMIN_EMAIL);
	}
}
