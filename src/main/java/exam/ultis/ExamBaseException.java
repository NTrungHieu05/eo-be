package exam.ultis;

import exam.db.enums.ErrorInfo;

import java.io.Serializable;

public class ExamBaseException extends Exception implements Serializable {

	private static final long serialVersionUID = 1L;

	private ErrorInfo error;

	public ExamBaseException(Throwable cause) {
		super(cause);
		this.setError(ErrorInfo.INTERNAL_SERVER_ERROR);
	}

	public ExamBaseException(ErrorInfo error) {
		this.setError(error);
	}

	public ExamBaseException(int errorCode, String error) {
		ErrorInfo errorInfo = new ErrorInfo(errorCode, error);
		this.setError(errorInfo);
	}

	public ErrorInfo getError() {
		return error;
	}

	public void setError(ErrorInfo error) {
		this.error = error;
	}

	@Override
	public String getMessage() {
		if (error != null) {
			return error.getMessage();
		}
		return super.getMessage();
	}
}
