package exam.service.auth;

import exam.db.dto.LoginRequest;
import exam.db.dto.LoginResponse;
import exam.db.entity.User;
import exam.db.enums.ErrorInfo;
import exam.ultis.ExamBaseException;
import exam.ultis.jwt.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private JwtUtils jwtUtils;

	@Override
	public LoginResponse login(LoginRequest request) throws ExamBaseException {
		try {
			Authentication authentication = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
			SecurityContextHolder.getContext().setAuthentication(authentication);
			User user = (User) authentication.getPrincipal();
			if (user.getOtp() != null) {
				throw new ExamBaseException(ErrorInfo.PLEASE_VERIFY_OTP);
			}
			String jwt = jwtUtils.generateJwtToken(user);
			return new LoginResponse(jwt, user);
		} catch (ExamBaseException e) {
			throw e;
		} catch (BadCredentialsException e) {
			throw new ExamBaseException(ErrorInfo.INCORRECT_PASSWORD_ERROR);
		} catch (AuthenticationException e) {
			throw new ExamBaseException(ErrorInfo.INCORRECT_PASSWORD_ERROR);
		}
	}

	@Override
	public void logout(String authorizationHeader) {
		SecurityContextHolder.clearContext();
	}
}
