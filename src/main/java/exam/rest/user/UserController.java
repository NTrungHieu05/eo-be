package exam.rest.user;

import exam.db.dto.ResponseObject;
import exam.db.dto.user.ChangePasswordRequest;
import exam.db.dto.user.ForgotPasswordRequest;
import exam.db.dto.user.ResetPasswordRequest;
import exam.db.dto.user.UpdateUserRequest;
import exam.db.dto.user.UserResponse;
import exam.db.enums.DBConst;
import exam.rest.endpoint.Endpoints;
import exam.service.user.UserService;
import exam.ultis.ExamBaseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
public class UserController {

	@Autowired
	private UserService userService;

	@PutMapping(Endpoints.USER_INFO_URL)
	public ResponseEntity<Object> updateUserInfo(@RequestBody UpdateUserRequest request) {
		ResponseObject<Object> response = new ResponseObject<>();
		try {
			response.setResponseData(userService.updateUserInfo(request));
		} catch (ExamBaseException e) {
			response.setError(e.getError());
		}
		return new ResponseEntity<>(response, new HttpHeaders(), HttpStatus.OK);
	}

	@PostMapping(Endpoints.CHANGE_PASSWORD_URL)
	public ResponseEntity<Object> changePassword(@RequestBody @Valid ChangePasswordRequest request) {
		ResponseObject<Object> response = new ResponseObject<>();
		try {
			userService.changePassword(request);
			response.setResponseData("Change password success!");
		} catch (ExamBaseException e) {
			response.setError(e.getError());
		}
		return new ResponseEntity<>(response, new HttpHeaders(), HttpStatus.OK);
	}

	@PostMapping(Endpoints.FORGOT_PASSWORD_URL)
	public ResponseEntity<Object> forgotPassword(@RequestBody @Valid ForgotPasswordRequest request) {
		ResponseObject<Boolean> response = new ResponseObject<>();
		try {
			response.setResponseData(userService.forgotPassword(request));
		} catch (ExamBaseException e) {
			response.setError(e.getError());
		}
		return new ResponseEntity<>(response, new HttpHeaders(), HttpStatus.OK);
	}

	@PostMapping(Endpoints.RESET_PASSWORD_URL)
	public ResponseEntity<Object> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
		ResponseObject<Object> response = new ResponseObject<>();
		try {
			userService.resetPassword(request);
			response.setResponseData("Change password success!");
		} catch (ExamBaseException e) {
			response.setError(e.getError());
		}
		return new ResponseEntity<>(response, new HttpHeaders(), HttpStatus.OK);
	}

	@GetMapping(Endpoints.USER_URL + Endpoints.PATH_VARIABLE_URL)
	public ResponseEntity<Object> getDetail(@PathVariable(DBConst.ID) String id) {
		ResponseObject<UserResponse> response = new ResponseObject<>();
		try {
			response.setResponseData(userService.getSimpleUsersInfo(id));
		} catch (ExamBaseException e) {
			response.setError(e.getError());
		}
		return new ResponseEntity<>(response, new HttpHeaders(), HttpStatus.OK);
	}
}
