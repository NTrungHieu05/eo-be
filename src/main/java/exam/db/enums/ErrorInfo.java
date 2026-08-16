package exam.db.enums;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ErrorInfo implements Serializable {

	private static final long serialVersionUID = 0L;

	private static final Logger logger = LoggerFactory.getLogger(ErrorInfo.class);

	public static Properties properties;

	static {
		try {
			properties = new Properties();
			InputStream is = ErrorInfo.class.getResourceAsStream("/error_info.properties");
			properties.load(is);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}

	public static final int BAD_REQUEST_ERROR_CODE = 1010;
	public static final int FIELD_ERROR_CODE = 1020;

	public static final int UNKNOWN_ERROR_CODE = 1001;
	public static final int USER_NOT_FOUND_ERROR_CODE = 1002;
	public static final int RESET_PASSWORD_ERROR_CODE = 1003;
	public static final int INCORRECT_PASSWORD_ERROR_CODE = 1004;
	public static final int INCORRECT_CONFIRM_PASSWORD_ERROR_CODE = 1005;
	public static final int ACCESS_DENIED_ERROR_CODE = 1006;
	public static final int EMAIL_ALREADY_EXISTS_ERROR_CODE = 1007;
	public static final int PHONE_ALREADY_EXISTS_ERROR_CODE = 1008;
	public static final int EMAIL_IS_EMPTY_OR_NULL_ERROR_CODE = 1009;
	public static final int AUTHORITIES_IS_NULL_ERROR_CODE = 1010;
	public static final int AUTHORITY_CODE_IS_NULL_ERROR_CODE = 1011;
	public static final int SKILL_NOT_FOUND_ERROR_CODE = 1012;
	public static final int VERIFY_RECAPTCHA_ERROR_CODE = 1013;
	public static final int CARD_IS_NULL_CODE = 1014;
	public static final int TOPIC_NOT_FOUND_ERROR_CODE = 1015;
	public static final int EXAM_NOT_FOUND_ERROR_CODE = 1016;
	public static final int PLEASE_VERIFY_OTP_CODE = 1017;

	private int code;
	private List<String> messages = new ArrayList<>();

	public ErrorInfo() {
	}

	public static ErrorInfo newInstance(int code, String message) {
		return new ErrorInfo(code, message);
	}

	public ErrorInfo(int code, String... messages) {
		this.code = code;
		for (String message : messages) {
			this.messages.add(message);
		}
	}

	public int getCode() {
		return code;
	}

	public String getMessage() {
		return messages.size() > 0 ? messages.get(0) : null;
	}

	public void setMessage(String message) {
		this.messages.add(message);
	}

	public static final ErrorInfo INTERNAL_SERVER_ERROR = new ErrorInfo(UNKNOWN_ERROR_CODE,
			properties.getProperty("server.error"));

	public static final ErrorInfo USER_NOT_FOUND_ERROR = new ErrorInfo(USER_NOT_FOUND_ERROR_CODE,
			properties.getProperty("user.not.found.error"));

	public static final ErrorInfo BAD_REQUEST = new ErrorInfo(BAD_REQUEST_ERROR_CODE,
			properties.getProperty("request.error"));

	public static final ErrorInfo RESET_PASSWORD_CODE_ERROR = new ErrorInfo(RESET_PASSWORD_ERROR_CODE,
			properties.getProperty("reset.password.code.error"));

	public static final ErrorInfo INCORRECT_PASSWORD_ERROR = new ErrorInfo(INCORRECT_PASSWORD_ERROR_CODE,
			properties.getProperty("incorrect.password"));

	public static final ErrorInfo INCORRECT_CONFIRM_PASSWORD_ERROR = new ErrorInfo(INCORRECT_CONFIRM_PASSWORD_ERROR_CODE,
			properties.getProperty("incorrect.confirm.password"));

	public static final ErrorInfo ACCESS_DENIED_ERROR = new ErrorInfo(ACCESS_DENIED_ERROR_CODE,
			properties.getProperty("access.denied.error"));

	public static final ErrorInfo EMAIL_ALREADY_EXISTS_ERROR = new ErrorInfo(EMAIL_ALREADY_EXISTS_ERROR_CODE,
			properties.getProperty("email.already.exists"));

	public static final ErrorInfo PHONE_ALREADY_EXISTS_ERROR = new ErrorInfo(PHONE_ALREADY_EXISTS_ERROR_CODE,
			properties.getProperty("phone.already.exists"));

	public static final ErrorInfo EMAIL_IS_EMPTY_OR_NULL_ERROR = new ErrorInfo(EMAIL_IS_EMPTY_OR_NULL_ERROR_CODE,
			properties.getProperty("email.is.empty.or.null"));

	public static final ErrorInfo AUTHORITIES_IS_NULL_ERROR = new ErrorInfo(AUTHORITIES_IS_NULL_ERROR_CODE,
			properties.getProperty("authorities.is.null"));

	public static final ErrorInfo AUTHORITY_CODE_IS_NULL_ERROR = new ErrorInfo(AUTHORITY_CODE_IS_NULL_ERROR_CODE,
			properties.getProperty("authority.code.is.null"));

	public static final ErrorInfo SKILL_NOT_FOUND_ERROR = new ErrorInfo(SKILL_NOT_FOUND_ERROR_CODE,
			properties.getProperty("skill.not.found"));

	public static final ErrorInfo VERIFY_RECAPTCHA_ERROR = new ErrorInfo(VERIFY_RECAPTCHA_ERROR_CODE,
			properties.getProperty("verify.recaptcha.error"));

	public static final ErrorInfo CARD_IS_NULL = new ErrorInfo(CARD_IS_NULL_CODE,
			properties.getProperty("card.is.null"));

	public static final ErrorInfo TOPIC_NOT_FOUND_ERROR = new ErrorInfo(TOPIC_NOT_FOUND_ERROR_CODE,
			properties.getProperty("topic.not.found"));

	public static final ErrorInfo EXAM_NOT_FOUND_ERROR = new ErrorInfo(EXAM_NOT_FOUND_ERROR_CODE,
			properties.getProperty("exam.not.found"));

	public static final ErrorInfo PLEASE_VERIFY_OTP = new ErrorInfo(PLEASE_VERIFY_OTP_CODE,
			properties.getProperty("please.verify.otp"));
}
