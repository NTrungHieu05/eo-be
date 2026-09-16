package exam.service;

import exam.db.entity.User;
import exam.db.enums.ErrorInfo;
import exam.db.repository.user.UserRepository;
import exam.ultis.ExamBaseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Optional;

@Service
public class ExamUserDetailsService implements UserDetailsService {

	@Autowired
	private UserRepository userRepository;

	public void createUser(User user) {
		userRepository.save(user);
	}

	public boolean userExists(String email) {
		return userRepository.findByUserNameOrEmail(email).isPresent();
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Optional<User> byUsername = userRepository.findByUserNameOrEmail(username);
		if (byUsername.isPresent()) {
			return byUsername.get();
		}
		throw new UsernameNotFoundException(ErrorInfo.USER_NOT_FOUND_ERROR.getMessage());
	}

	public void validateUserDetails(User user) throws ExamBaseException {
		if (!StringUtils.hasText(user.getEmail())) {
			throw new ExamBaseException(ErrorInfo.EMAIL_IS_EMPTY_OR_NULL_ERROR);
		}
		validateAuthorities(user.getAuthorities());
	}

	private void validateAuthorities(Collection<? extends GrantedAuthority> authorities) throws ExamBaseException {
		if (CollectionUtils.isEmpty(authorities)) {
			throw new ExamBaseException(ErrorInfo.AUTHORITIES_IS_NULL_ERROR);
		}
		for (GrantedAuthority authority : authorities) {
			if (authority == null) {
				throw new ExamBaseException(ErrorInfo.AUTHORITIES_IS_NULL_ERROR);
			}
			if (!StringUtils.hasText(authority.getAuthority())) {
				throw new ExamBaseException(ErrorInfo.AUTHORITY_CODE_IS_NULL_ERROR);
			}
		}
	}
}
