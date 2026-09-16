package exam.service.user;

import exam.db.dto.user.ChangePasswordRequest;
import exam.db.dto.user.CreateUserRequest;
import exam.db.dto.user.ForgotPasswordRequest;
import exam.db.dto.user.ResetPasswordRequest;
import exam.db.dto.user.UpdateUserRequest;
import exam.db.dto.user.UserResponse;
import exam.db.entity.Role;
import exam.db.entity.User;
import exam.ultis.ExamBaseException;

public interface UserService {
	UserResponse createUser(CreateUserRequest request) throws ExamBaseException;

	User createUser(CreateUserRequest request, Role role) throws ExamBaseException;

	Boolean changePassword(ChangePasswordRequest request) throws ExamBaseException;

	UserResponse updateUserInfo(UpdateUserRequest request) throws ExamBaseException;

	UserResponse getSimpleUsersInfo(String userId) throws ExamBaseException;

	Boolean forgotPassword(ForgotPasswordRequest request) throws ExamBaseException;

	Boolean resetPassword(ResetPasswordRequest request) throws ExamBaseException;
}
