package exam.config;

import exam.db.dto.ResponseObject;
import exam.db.enums.ErrorInfo;
import org.apache.commons.lang3.text.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.ArrayList;
import java.util.List;

@ControllerAdvice
public class GlobalDefaultExceptionHandler extends ResponseEntityExceptionHandler {

	private final static Logger logger = LoggerFactory.getLogger(GlobalDefaultExceptionHandler.class);

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
	                                                              HttpHeaders headers, HttpStatus status, WebRequest request) {

		// Tạo mới mỗi lần: handler là singleton, dùng chung field sẽ lẫn lỗi giữa các request.
		ResponseObject<Object> responseObject = new ResponseObject<>();
		responseObject.setError(new ErrorInfo(ErrorInfo.FIELD_ERROR_CODE, toArrays(ex.getBindingResult())));
		return new ResponseEntity<>(responseObject, headers, HttpStatus.OK);
	}

	/**
	 * @PreAuthorize ném AccessDeniedException. Trả cùng dạng với các lỗi quyền khác (HTTP 200, mã 1006)
	 * để frontend xử lý một kiểu. Chưa đăng nhập thì vẫn trả 401 để frontend chuyển về trang đăng nhập.
	 */
	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<Object> handleAccessDenied(AccessDeniedException ex) {
		ResponseObject<Object> responseObject = new ResponseObject<>();
		responseObject.setError(ErrorInfo.ACCESS_DENIED_ERROR);
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		boolean signedIn = authentication != null && authentication.isAuthenticated()
				&& !(authentication instanceof AnonymousAuthenticationToken);
		return new ResponseEntity<>(responseObject, new HttpHeaders(), signedIn ? HttpStatus.OK : HttpStatus.UNAUTHORIZED);
	}

	private String[] toArrays(BindingResult bindingResult) {
		List<FieldError> fieldErrors = bindingResult.getFieldErrors();
		List<String> messages = new ArrayList<>();

		for (FieldError fieldError : fieldErrors) {

			String message = String.format("%s %s", WordUtils.capitalize(fieldError.getField()),
					fieldError.getDefaultMessage());

			messages.add(message);
		}
		return messages.toArray(new String[0]);
	}

}
