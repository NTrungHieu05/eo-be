package exam.db.dto;

import exam.db.enums.ErrorInfo;

public class ResponseObject<T> {
	public ResponseObject() {

	}

	public ResponseObject(T responseData) {
		this.responseData = responseData;
	}

	private ErrorInfo error;
	private T responseData;

	public ErrorInfo getError() {
		return error;
	}

	public void setError(ErrorInfo error) {
		this.error = error;
	}

	public T getResponseData() {
		return responseData;
	}

	public void setResponseData(T responseData) {
		this.responseData = responseData;
	}
}
