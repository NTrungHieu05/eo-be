package exam.config;

import exam.db.dto.ResponseObject;
import exam.db.enums.ErrorInfo;
import org.apache.commons.lang3.text.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.ArrayList;
import java.util.List;

@ControllerAdvice
public class GlobalDefaultExceptionHandler extends ResponseEntityExceptionHandler {

	private final static Logger logger = LoggerFactory.getLogger(GlobalDefaultExceptionHandler.class);

	private ErrorInfo errorInfo;
	private ResponseObject<Object> responseObject = new ResponseObject<>();

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
	                                                              HttpHeaders headers, HttpStatus status, WebRequest request) {

		errorInfo = new ErrorInfo(ErrorInfo.FIELD_ERROR_CODE, toArrays(ex.getBindingResult()));

		responseObject.setError(errorInfo);
		return new ResponseEntity<>(responseObject, headers, HttpStatus.OK);
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
