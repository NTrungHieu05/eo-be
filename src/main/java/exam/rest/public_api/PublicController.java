package exam.rest.public_api;

import exam.db.dto.LoginRequest;
import exam.db.dto.ResponseObject;
import exam.db.dto.user.CreateUserRequest;
import exam.rest.endpoint.Endpoints;
import exam.service.auth.AuthService;
import exam.service.registration.RegistrationService;
import exam.ultis.ExamBaseException;
import exam.ultis.SecurityConst;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
public class PublicController {

	@Autowired
	private RegistrationService registrationSv;

	@Autowired
	private AuthService authService;

	@PostMapping(Endpoints.LOGIN_URL)
	public ResponseEntity<Object> appLogin(@RequestBody @Valid LoginRequest request) {
		ResponseObject<Object> response = new ResponseObject<>();
		try {
			response.setResponseData(authService.login(request));
		} catch (ExamBaseException e) {
			response.setError(e.getError());
		}
		return new ResponseEntity<>(response, new HttpHeaders(), HttpStatus.OK);
	}

	@PostMapping(Endpoints.LOGOUT_URL)
	public ResponseEntity<Object> appLogout(HttpServletRequest request) {
		ResponseObject<Object> response = new ResponseObject<>();
		try {
			authService.logout(request.getHeader(SecurityConst.AUTHORIZATION_HEADER));
			response.setResponseData("Logout Successfully!!!");
		} catch (ExamBaseException e) {
			response.setError(e.getError());
		}
		return new ResponseEntity<>(response, new HttpHeaders(), HttpStatus.OK);
	}

	@PostMapping(Endpoints.REGISTER_USER_URL)
	public ResponseEntity<Object> registerUser(@RequestBody @Valid CreateUserRequest request) {
		ResponseObject<Object> response = new ResponseObject<>();
		try {
			response.setResponseData(registrationSv.registerUser(request));
		} catch (ExamBaseException e) {
			response.setError(e.getError());
		}
		return new ResponseEntity<>(response, new HttpHeaders(), HttpStatus.OK);
	}
}
