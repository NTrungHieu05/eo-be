package exam.service.registration;

import exam.db.dto.user.CreateUserRequest;
import exam.db.dto.user.UserResponse;
import exam.ultis.ExamBaseException;

public interface RegistrationService {
	UserResponse registerUser(CreateUserRequest request) throws ExamBaseException;
}
