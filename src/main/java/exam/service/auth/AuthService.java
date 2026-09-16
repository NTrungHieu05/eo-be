package exam.service.auth;

import exam.db.dto.LoginRequest;
import exam.db.dto.LoginResponse;
import exam.ultis.ExamBaseException;

public interface AuthService {
	LoginResponse login(LoginRequest request) throws ExamBaseException;

	void logout(String header) throws ExamBaseException;
}
