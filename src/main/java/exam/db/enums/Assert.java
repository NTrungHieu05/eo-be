package exam.db.enums;

import exam.ultis.ExamBaseException;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collection;

public class Assert {
	private Assert() {
	}

	public static void notNull(Object obj, ErrorInfo errorInfo) throws ExamBaseException {
		if (obj == null) {
			throw new ExamBaseException(errorInfo);
		}
	}

	public static void notNull(Object obj, String errorInfo) throws ExamBaseException {
		if (obj == null) {
			throwMsg(errorInfo);
		}
	}

	public static void notEmpty(Object obj, ErrorInfo errorInfo) throws ExamBaseException {
		if (obj == null
				|| (obj instanceof String && StringUtils.isEmpty(obj)
				|| (obj instanceof Collection && CollectionUtils.isEmpty((Collection<?>) obj)))) {
			throw new ExamBaseException(errorInfo);
		}
	}

	public static void notEmpty(Object obj, String errorInfo) throws ExamBaseException {
		if (obj == null
				|| (obj instanceof String && StringUtils.isEmpty(obj)
				|| (obj instanceof Collection && CollectionUtils.isEmpty((Collection<?>) obj)))) {
			throwMsg(errorInfo);
		}
	}

	private static void throwMsg(String errorInfo) throws ExamBaseException {
		throw new ExamBaseException(new ErrorInfo(ErrorInfo.UNKNOWN_ERROR_CODE, errorInfo));
	}

	public static void isTrue(boolean b, String errMessage) throws ExamBaseException {
		if (!b) {
			throwMsg(errMessage);
		}
	}

	public static void isTrue(boolean b, ErrorInfo errorInfo) throws ExamBaseException {
		if (!b) {
			throw new ExamBaseException(errorInfo);
		}
	}

	public static void isFalse(boolean b, String errMessage) throws ExamBaseException {
		if (b) {
			throwMsg(errMessage);
		}
	}

	public static void isFalse(boolean b, ErrorInfo errorInfo) throws ExamBaseException {
		if (b) {
			throw new ExamBaseException(errorInfo);
		}
	}
}
