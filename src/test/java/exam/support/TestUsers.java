package exam.support;

import exam.db.entity.Role;
import exam.db.entity.User;
import exam.db.enums.RoleEnum;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

public final class TestUsers {

	private TestUsers() {
	}

	public static User user(String userId, RoleEnum role) {
		User user = new User();
		user.setUserId(userId);
		user.setEmail(userId + "@toeic.local");
		user.setUsername(user.getEmail());
		user.setAccountNonExpired(true);
		user.setAccountNonLocked(true);
		user.setCredentialsNonExpired(true);
		user.setEnabled(true);
		user.getRoles().add(new Role(role.getName()));
		return user;
	}

	public static void authenticate(User user) {
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
	}

	public static void clear() {
		SecurityContextHolder.clearContext();
	}
}
