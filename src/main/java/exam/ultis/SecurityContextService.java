package exam.ultis;

import exam.db.dto.user.ExamUser;
import exam.db.entity.User;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityContextService {

	public Authentication getAuthentication() {
		return SecurityContextHolder.getContext().getAuthentication();
	}

	public void setAuthentication(final Authentication authentication) {
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	public static ExamUser getUser() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null && auth.getPrincipal() instanceof User) {
			User user = (User) auth.getPrincipal();
			ExamUser examUser = new ExamUser();
			BeanUtils.copyProperties(user, examUser);
			examUser.setRoles(user.getRoleStrings());
			return examUser;
		}
		return null;
	}
}
