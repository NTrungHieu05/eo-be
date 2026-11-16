package exam.service.auth;

import exam.db.dto.LoginRequest;
import exam.db.dto.LoginResponse;
import exam.db.entity.User;
import exam.db.enums.ErrorInfo;
import exam.db.repository.user.UserRepository;
import exam.ultis.ExamBaseException;
import exam.ultis.jwt.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class AuthServiceImpl implements AuthService {

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private JwtUtils jwtUtils;

	@Autowired
	private UserRepository userRepository;

	@Override
	public LoginResponse login(LoginRequest request) throws ExamBaseException {
		try {
			String password = request.getPassword() == null ? "" : request.getPassword().trim();
			Authentication authentication = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(request.getEmail(), password));
			SecurityContextHolder.getContext().setAuthentication(authentication);
			User user = (User) authentication.getPrincipal();
			if (user.isDeleted()) {
				SecurityContextHolder.clearContext();
				throw new ExamBaseException(ErrorInfo.ACCOUNT_LOCKED);
			}
			String jwt = jwtUtils.generateJwtToken(user);
			return new LoginResponse(jwt, user);
		} catch (LockedException | DisabledException e) {
			throw new ExamBaseException(ErrorInfo.ACCOUNT_LOCKED);
		} catch (BadCredentialsException e) {
			throw new ExamBaseException(ErrorInfo.INCORRECT_PASSWORD_ERROR);
		} catch (AuthenticationException e) {
			throw new ExamBaseException(ErrorInfo.INCORRECT_PASSWORD_ERROR);
		}
	}

	@Override
	public void logout(String authorizationHeader) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.getPrincipal() instanceof User) {
			String userId = ((User) authentication.getPrincipal()).getUserId();
			// Mọi token phát hành trước lúc này của user sẽ bị AuthenticationFilter từ chối.
			userRepository.findById(userId).ifPresent(user -> {
				user.setTokensValidAfter(new Date());
				userRepository.save(user);
			});
		}
		SecurityContextHolder.clearContext();
	}
}
